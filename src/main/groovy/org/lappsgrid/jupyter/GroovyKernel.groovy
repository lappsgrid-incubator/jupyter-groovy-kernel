package org.lappsgrid.jupyter

import org.lappsgrid.jupyter.context.DefaultGroovyContext
import org.lappsgrid.jupyter.context.GroovyContext
import org.lappsgrid.jupyter.handler.CompleteHandler
import org.lappsgrid.jupyter.handler.ExecuteHandler
import org.lappsgrid.jupyter.handler.HistoryHandler
import org.lappsgrid.jupyter.handler.IHandler
import org.lappsgrid.jupyter.handler.KernelInfoHandler
import org.lappsgrid.jupyter.json.Serializer
import org.lappsgrid.jupyter.msg.Header
import org.lappsgrid.jupyter.msg.Message
import org.lappsgrid.jupyter.security.HmacSigner
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ

import java.security.InvalidKeyException
import java.text.DateFormat
import java.text.SimpleDateFormat

import static org.lappsgrid.jupyter.msg.Message.Type.*

/**
 * @author Keith Suderman
 */
class GroovyKernel {
    private static final Logger logger = LoggerFactory.getLogger(GroovyKernel)
    static final TimeZone UTC = TimeZone.getTimeZone('UTC')

    public static String GALAXY_KEY = null
    public static String GALAXY_HOST = null

    private volatile boolean running = false

    static final String DELIM = "<IDS|MSG>"

    /** Used to generate the HMAC signatures for messages */
    HmacSigner hmac

    /** The UUID for this session. */
    String id

    /** Information from the connection file from Jupyter. */
    Config configuration

    /** Message handlers. All sockets listeners will dispatch to these handlers. */
    Map<String, IHandler> handlers

    /** Used to configure the Groovy compiler when code needs to be compiled. */
    GroovyContext context

    // The sockets the kernel listens to.
    ZMQ.Socket hearbeatSocket
    ZMQ.Socket controlSocket
    ZMQ.Socket shellSocket
    ZMQ.Socket iopubSocket
    ZMQ.Socket stdinSocket

    public GroovyKernel() {
        this(new DefaultGroovyContext())
    }

    public GroovyKernel(GroovyContext context) {
        id = uuid()
        this.context = context
        installHandlers()
    }

    static String timestamp() {
        // SimpleDateFormat is not thread-safe so we need to create a new one for each
        // timestamp that is generated.
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")
        df.setTimeZone(UTC)
        return df.format(new Date())
    }

    static String encode(Object object) {
        return Serializer.toJson(object)
    }

    static Map decode(String json) {
        return Serializer.parse(json, LinkedHashMap)
    }

    static <T> T decode(byte[] bytes) {
        return decode(new String(bytes))
    }
    static <T> T decode(String json, Class<T> theClass) {
        return Serializer.parse(json, theClass)
    }

    void shutdown() { running = false }

    static String uuid() {
        return UUID.randomUUID()
    }

    private void installHandlers() {
        handlers = [
                execute_request: new ExecuteHandler(this),
                kernel_info_request: new KernelInfoHandler(this),
                is_complete_request: new CompleteHandler(this),
                history_request: new HistoryHandler(this),
        ]
    }

    void _send(ZMQ.Socket socket, Message message) {
        logger.info("Sending message: {}", message.asJson())
        String jsonHeader = encode(message.header)
        String jsonParent = encode(message.parentHeader)
        String jsonMetadata = encode(message.metadata)
        String jsonContent = encode(message.content)
        List parts = [ jsonHeader, jsonParent, jsonMetadata, jsonContent ]
        String signature = sign(parts)

        message.identities.each { socket.sendMore(it) }
        socket.sendMore(DELIM)
        socket.sendMore(signature)
        socket.sendMore(jsonHeader)
        socket.sendMore(jsonParent)
        socket.sendMore(jsonMetadata)
        socket.send(jsonContent)
        logger.debug("Message sent.")
    }

    void publish(Message message) {
        send(iopubSocket, message)
    }

    // Most things go to the shell socket so that is the default.
    void send(Message message) {
        send(shellSocket, message)
    }

    void send(ZMQ.Socket socket, Message message) {
        logger.trace("Sending message: {}", message.asJson())
        // Encode the message parts (blobs) and calculate the signature.
        List parts = [
                encode(message.header),
                encode(message.parentHeader),
                encode(message.metadata),
                encode(message.content)
        ]
        String signature = hmac.sign(parts)
        logger.trace("Signature is {}", signature)

        // Now send the message down the wire.
        message.identities.each { socket.sendMore(it) }
        socket.sendMore(DELIM)
        socket.sendMore(signature)
        3.times { i -> socket.sendMore(parts[i]) }
        socket.send(parts[3])
        logger.trace("Message sent")
    }

    String read(ZMQ.Socket socket) {
        return new String(socket.recv())
    }

    public <T> T read(ZMQ.Socket socket, Class<T> theClass) {
        return Serializer.parse(read(socket), theClass)
    }

    public <T> T parse(byte[] bytes, Class<T> theClass) {
        return Serializer.parse(new String(bytes), theClass)
    }

    Message readMessage(ZMQ.Socket socket) {
        Message message = new Message()
        try {
            // Read socket identities until we encounter the delimiter
            String identity = read(socket)
            while (DELIM != identity) {
                message.identities << identity
                identity = read(socket)
            }
            // Read the signature and the four blobs
            String expectedSig = read(socket)
            byte[] header = socket.recv()
            byte[] parent = socket.recv()
            byte[] metadata = socket.recv()
            byte[] content = socket.recv()

            // Make sure that the signatures match before proceeding.
            String actualSig = hmac.signBytes([header, parent, metadata, content])
            if (expectedSig != actualSig) {
                logger.error("Message signatures do not match")
                logger.error("Expected: []", expectedSig)
                logger.error("Actual  : []", actualSig)
                throw new RuntimeException("Signatures do not match.")
            }

            // Parse the byte buffers into the appropriate types
            message.header = parse(header, Header)
            message.parentHeader = parse(parent, Header)
            message.metadata = parse(metadata, LinkedHashMap)
            message.content = parse(content, LinkedHashMap)

        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid hmac exception while converting to HmacSHA256")
        }
        return message
    }

    void shellHandler() {
        Thread.start {
            while (running) {
                Message message = readMessage(shellSocket)
                IHandler handler = handlers[message.type()]
                if (handler) {
                    handler.handle(message)
                }
                else {
                    logger.warn("Unhandled message type: {}", message.type())
                }

            }
        }
    }

    void controlHandler() {
        Thread.start {
            while (running) {
                Message message = readMessage(controlSocket)
                String type = message.header.type
                if (type == SHUTDOWN_REQUEST) {
                    logger.info("Control handler received a shutdown request")
                    shutdown()
                    Message reply = new Message()
                    reply.header = new Header(SHUTDOWN_REPLY, message)
                    reply.parentHeader = message.header
                    reply.content = message.content
                    send(controlSocket, reply)
                }
                else {
                    logger.warn("Unhandled control message: {}", type)
                }
            }
        }
    }

    //TODO Not sure what to do with this yet.
    void stdinHandler() {
        Thread.start {
            while (running) {
                byte[] buffer = stdinSocket.recv()
                logger.info("Stdin: {}", new String(buffer))
            }
        }
    }

    void heartbeat() {
        Thread.start {
            while (running) {
                byte[] buffer = hearbeatSocket.recv(0)
                hearbeatSocket.send(buffer)
            }
        }
    }

    int bind(ZMQ.Socket socket, String connection, int port) {
        if (port <= 0) {
            port = socket.bindToRandomPort(connection)
        }
        else {
            socket.bind("${connection}:${port}")
        }
        return port
    }

    ZMQ.Socket newSocket(ZMQ.Context context, int type, String connection, int port) {
        ZMQ.Socket socket = context.socket(type)
        bind(socket, connection, port)
        return socket
    }

    public void run() {
        logger.info("Groovy Jupyter kernel starting.")
//        logger.debug("Galaxy host is {}", GALAXY_HOST)
//        logger.debug("Galaxy API hmac is {}", GALAXY_KEY)
        running = true

        logger.debug("Creating signing hmac with: {}", configuration.key)
        hmac = new HmacSigner(configuration.key)

        String connection = configuration.transport + '://' + configuration.host
        ZMQ.Context context = ZMQ.context(1)

        hearbeatSocket = newSocket(context, ZMQ.REP, connection, configuration.heartbeat)
        iopubSocket = newSocket(context, ZMQ.PUB, connection, configuration.iopub)
        controlSocket = newSocket(context, ZMQ.ROUTER, connection, configuration.control)
        stdinSocket = newSocket(context, ZMQ.ROUTER, connection, configuration.stdin)
        shellSocket = newSocket(context, ZMQ.ROUTER, connection, configuration.shell)

//        hearbeatSocket = context.socket(ZMQ.REP)
//        bind(hearbeatSocket, connection, configuration.heartbeat)

//        iopubSocket = context.socket(ZMQ.PUB)
//        bind(iopubSocket, connection, configuration.iopub)

//        controlSocket = context.socket(ZMQ.ROUTER)
//        bind(controlSocket, connection, configuration.control)

//        stdinSocket = context.socket(ZMQ.ROUTER)
//        bind(stdinSocket, connection, configuration.stdin)

//        shellSocket = context.socket(ZMQ.ROUTER)
//        bind(shellSocket, connection, configuration.shell)

        // Now start all the handler threads
        heartbeat()
        controlHandler()
        shellHandler()
        stdinHandler()

        while (running) {
            // Nothing to do but navel gaze until another thread sets
            // running == false
            Thread.sleep(2000)
        }
        logger.info("Shutting down")
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            println "Invalid parameters passed to the Groovy kernel."
            System.exit(1)
        }
        File config = new File(args[0])
        if (!config.exists()) {
            println "Kernel configuration not found."
            System.exit(1)
        }

        File propFile = new File(args[1])
        if (!propFile) {
            println "Groovy kernel properties not found."
            System.exit(1)
        }
        GroovyKernel kernel = new GroovyKernel()
        kernel.configuration = Serializer.parse(config.text, Config)

        Properties props = new Properties()
        props.load(propFile.newInputStream())
        GALAXY_KEY = props['GALAXY_KEY']
        GALAXY_HOST = props['GALAXY_HOST']

        kernel.run()
    }
}
