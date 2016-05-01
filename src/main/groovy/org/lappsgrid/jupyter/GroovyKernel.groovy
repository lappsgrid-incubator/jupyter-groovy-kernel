package org.lappsgrid.jupyter

import groovy.json.JsonOutput
import org.apache.commons.codec.binary.Hex
import org.lappsgrid.jupyter.handler.CompleteHandler
import org.lappsgrid.jupyter.handler.ExecuteHandler
import org.lappsgrid.jupyter.handler.HistoryHandler
import org.lappsgrid.jupyter.handler.IHandler
import org.lappsgrid.jupyter.handler.KernelInfoHandler
import org.lappsgrid.serialization.Serializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.InvalidKeyException
import java.text.DateFormat
import java.text.SimpleDateFormat

import static org.lappsgrid.jupyter.Message.Type.*

/**
 * @author Keith Suderman
 */
class GroovyKernel {
    private static final Logger logger = LoggerFactory.getLogger(GroovyKernel)

    private volatile boolean running = false

    static final String DELIM = "<IDS|MSG>"

    private static final DateFormat df

    String key
    String id
    Config configuration
    Map<String, IHandler> handlers

    ZMQ.Socket hearbeatSocket
    ZMQ.Socket controlSocket
    ZMQ.Socket shellSocket
    ZMQ.Socket iopubSocket
    ZMQ.Socket stdinSocket

    static {
        TimeZone zone = TimeZone.getTimeZone('UTC')
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")
        df.setTimeZone(zone)
    }

    public GroovyKernel() {
        id = uuid()
        installHandlers()
    }

    static String timestamp() {
        df.format(new Date())
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

    String asHex(byte[] buffer) {
        return Hex.encodeHexString(buffer)
    }

    private void installHandlers() {
        handlers = [
                execute_request: new ExecuteHandler(this),
                kernel_info_request: new KernelInfoHandler(this),
                is_complete_request: new CompleteHandler(this),
                history_request: new HistoryHandler(this),
        ]
//        handlers = new HashMap<String,IHandler>()
//        handlers[EXECUTE_REQUEST] = new ExecuteHandler(this)
//        handlers[KERNEL_INFO_REQUEST] = new KernelInfoHandler(this)
//        handlers[COMPLETE_REQUEST] = new CompleteHandler(this)
//        handlers[HISTORY_REQUEST] = new HistoryHandler(this)
    }

    String sign(List<String> msg) {
        if (!key || key == '') return ''
        logger.trace("Signing message with key {}", key)
        try {
            Mac mac = Mac.getInstance("HmacSHA256")
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256")
            mac.init(secretKeySpec)
            msg.each {
                mac.update(it.bytes)
            }
            byte[] digest = mac.doFinal()
            return asHex(digest)
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception while converting to HmacSHA256")
        }
    }

    String sign(Message message) {
        if (!key || key == '') return ''
        logger.trace("Signing message with key {}", key)
        try {
            Mac mac = Mac.getInstance("HmacSHA256")
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256")
            mac.init(secretKeySpec)
            mac.update(encode(message.header).bytes)
            mac.update(encode(message.parentHeader).bytes)
            mac.update(encode(message.metadata).bytes)
            byte[] digest = mac.doFinal(encode(message.content).bytes)
            return asHex(digest)
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception while converting to HmacSHA256")
        }
    }

    String sign(String msg) {
        if (!key || key == '') return ''
        logger.trace("Signing message with key {}", key)
        try {
            Mac mac = Mac.getInstance("HmacSHA256")
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256")
            mac.init(secretKeySpec)
            byte[] digest = mac.doFinal(msg.bytes)
            return asHex(digest) //digest.encodeBase64().toString()
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception while converting to HmacSHA256")
        }
    }

//    void send(Map params, ZMQ.Socket socket) { //, Map content=[:], Map parent = [:], Map metadata = [:], List<String> identities=[]) {
//        List identities = params.identities ?: []
//        Map content = params.content
//        Header header = params.header
//        Map metadata = params.metadata ?: [:]
//        Header parent = params.parent ?: [:]
//        logger.info("Sending message: {}", id)
//        String jsonContent = encode(content)
//        String jsonHeader = encode(header)
//        String jsonParent = encode(parent)
//        String jsonMetadata = encode(metadata)
//        logger.debug("Header: {}", jsonHeader )
//        logger.debug("Content: {}", jsonContent)
//        List parts = [ jsonHeader, jsonParent, jsonMetadata, jsonContent ]
//        String signature = sign(parts)
//
//        identities.each { socket.sendMore(it) }
//        socket.sendMore(DELIM)
//        socket.sendMore(signature)
//        socket.sendMore(jsonHeader)
//        socket.sendMore(jsonParent)
//        socket.sendMore(jsonMetadata)
//        socket.send(jsonContent)
//        logger.debug("Message sent.")
//    }

    void send(ZMQ.Socket socket, Message message) {
        logger.info("Sending message: {}", message.asJson())
        String jsonContent = encode(message.content)
        String jsonHeader = encode(message.header)
        String jsonParent = encode(message.parentHeader)
        String jsonMetadata = encode(message.metadata)
//        logger.debug("Header: {}", jsonHeader )
//        logger.debug("Content: {}", jsonContent)
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

    void send(Message message) {
        send(shellSocket, message)
    }

    void heartbeat() {
        Thread.start {
            while (running) {
                byte[] buffer = hearbeatSocket.recv(0)
                hearbeatSocket.send(buffer)
            }
        }
    }

    String read(ZMQ.Socket socket) {
        byte[] buffer = socket.recv()
        String s = new String(buffer)
        logger.debug("Socket read: {}", s)
        return s
    }

    public <T> T read(ZMQ.Socket socket, Class<T> theClass) {
        return Serializer.parse(read(socket), theClass)
    }

    Message readMessage(ZMQ.Socket socket) {
        Message message = new Message()
        try {
            Mac mac = Mac.getInstance("HmacSHA256")
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256")
            mac.init(secretKeySpec)
            String identity = read(socket)
            while (DELIM != identity) {
                message.identities << identity
                identity = read(socket)
            }
            String signature = read(socket)

            // header
            byte[] buffer = socket.recv()
            mac.update(buffer)
            String json = new String(buffer)
            logger.debug("Header: {}", json)
            message.header = Serializer.parse(json, Header)

            // parent header
            buffer = socket.recv()
            mac.update(buffer)
            json = new String(buffer)
            logger.debug("Parent: {}", json)
            message.parentHeader = Serializer.parse(json, Header)

            // metadata
            buffer = socket.recv()
            mac.update(buffer)
            json = new String(buffer)
            logger.debug("Metadata: {}", json)
            message.metadata = Serializer.parse(json, LinkedHashMap)

            // content
            buffer = socket.recv()
            mac.update(buffer)
            json = new String(buffer)
            logger.debug("Content: {}", json)
            message.content = Serializer.parse(json, LinkedHashMap)

            byte[] digest = mac.doFinal()
            String sig = asHex(digest)
            logger.trace("Expected Sig: {}", signature)
            logger.trace("Actual Sig  : {}", sig)
            if (sig != signature) {
                logger.error("Invalid signature on message.")
                throw new RuntimeException("Invalid signature on message.")
            }
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception while converting to HmacSHA256")
        }
        return message
    }

    Message _readMessage(ZMQ.Socket socket) {
        Message message = new Message()
        String input = read(socket)
        while (DELIM != input) {
            if (input == null || input.length() == 0) {
                logger.debug("Empty read")
            }
            else {
                logger.debug("read: {}", input)
                message.identities << input
            }
            input = read(socket)
        }
        logger.trace('Found socket identities.')
        String signature = read(socket)
        logger.trace('Signature: {}', signature)
        message.header = read(socket, Header)
        message.parentHeader = read(socket, Header)
        message.metadata = read(socket, LinkedHashMap)
        message.content = read(socket, LinkedHashMap)
        logger.trace("Read message: {}", message.asJson())

//        List parts = [ encode([:]), encode([:]), encode([:]), message.payload ]
        String sig = sign(message)
        if (sig != signature) {
            logger.warn("Signatures do not match: {}", sig)
            logger.warn("Expected {}", signature)
        }
        else {
            logger.debug("Message signature is valid.")
        }
        return message
    }

//    void shellHandler() {
//        Thread.start {
//            while (running) {
//                Message message = readMessage(shellSocket)
//                String id = message.header.id
//                String session = message.header.session
//                String type = message.header.type
//                logger.debug("SHELL MSG: {}", id)
//                if (type == EXECUTE_REQUEST) {
//                    logger.info("Processing execute request")
//                    Message reply = new Message()
//                    reply.content = [ execution_state: 'busy' ]
//                    reply.header = new Header(STATUS, message)
//                    reply.parentHeader = message.parentHeader
//                    send(iopubSocket, reply)
//
//                    reply.content = [
//                            execution_count: executionCount,
//                            code: message.content.code
//                    ]
//                    reply.header = newHeader(EXECUTE_INPUT, message)
//                    send(iopubSocket, reply)
//
//                    reply.content = [
//                            name: 'stdout',
//                            text: 'hello world!'
//                    ]
//                    reply.header = newHeader(STREAM, message)
//                    send(iopubSocket, reply)
//
//                    reply.content = [
//                            execution_count: executionCount,
//                            data: [ 'text/plain': 'result!' ],
//                            metadata: [:]
//                    ]
//                    reply.header = newHeader(EXECUTE_RESULT, message)
//                    send(iopubSocket, reply)
//
//                    reply.content = [ execution_state: 'idle']
//                    reply.header = newHeader(STATUS, message)
//                    send(iopubSocket, reply)
//
//                    reply.metadata = [
//                            dependencies_met: true,
//                            engine: id,
//                            status: 'ok',
//                            started: timestamp()
//                    ]
//                    reply.content = [
//                            status: 'ok',
//                            execution_count: executionCount,
//                            user_variables: [:],
//                            payload: [],
//                            user_expressions: [:]
//                    ]
//                    reply.header = newHeader(EXECUTE_REPLY, message)
//                    send(shellSocket, reply)
//                    ++executionCount
//                }
//                else if (type == KERNEL_INFO_REQUEST) {
//                    logger.info("Processing kernel info request")
//                    Map content = [
//                            protocol_version: '5.0',
//                            implementation: 'compiler',
//                            implementation_version: '1.0.0',
//                            language_info: [
//                                    name: 'Groovy',
//                                    version: '2.4.6',
//                                    mimetype: '',
//                                    file_extension: '.compiler',
//                                    pygments_lexer: '',
//                                    codemirror_mode: '',
//                                    nbconverter_exporter: ''
//                            ],
//                            banner: 'Apache Groovy',
//                            help_links: []
//
//                    ]
//                    Header header = newHeader(KERNEL_INFO_REPLY, message)
//                    send(shellSocket, content:content, header:header, parent:message.header, identities:message.identities)
//                }
//                else if (type == COMPLETE_REQUEST) {
//                    logger.info("Handling complete request")
//                }
//                else if (type == HISTORY_REQUEST) {
//                    logger.info("Handling history request")
//                    Header header = new Header(HISTORY_REPLY, message)
//                    Map content = [ history: [session, 1, ''] ]
//                    send(shellSocket, content:content, header:header, identities:message.identities)
//                }
//                else {
//                    logger.warn("Unknown message type: {}", type)
//                }
//
//            }
//        }
//    }

    void shellHandler() {
        Thread.start {
            while (running) {
                Message message = readMessage(shellSocket)
                String id = message.header.id
                logger.debug("SHELL MSG: {}", id)
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
                String type = message.header.id
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

    void stdinHandler() {
        Thread.start {
            while (running) {
                byte[] buffer = stdinSocket.recv()
                logger.debug("Stdin: {}", new String(buffer))
            }
        }
    }

    int bind(ZMQ.Socket socket, String connection, int port) {
        if (port <= 0) {
            return socket.bindToRandomPort(connection)
        }
        else {
            socket.bind("${connection}:${port}")
        }
        return port
    }

    public void run() {
        running = true
        String connection = configuration.transport + '://' + configuration.host
        key = configuration.key

        ZMQ.Context context = ZMQ.context(1)
        hearbeatSocket = context.socket(ZMQ.REP)
        bind(hearbeatSocket, connection, configuration.heartbeat)

        iopubSocket = context.socket(ZMQ.PUB)
        bind(iopubSocket, connection, configuration.iopub)

        controlSocket = context.socket(ZMQ.ROUTER)
        bind(controlSocket, connection, configuration.control)

        stdinSocket = context.socket(ZMQ.ROUTER)
        bind(stdinSocket, connection, configuration.stdin)

        shellSocket = context.socket(ZMQ.ROUTER)
        bind(shellSocket, connection, configuration.shell)

        // Now start all the handler threads
        heartbeat()
        controlHandler()
        shellHandler()
        stdinHandler()
//        iopubHander()

        while (running) {
            // Nothing to do but navel gaze until running == false
            Thread.sleep(1000)
        }
        logger.info("Shutting down")
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            println "No kernel configuration file provided"
            System.exit(1)
        }
        File file = new File(args[0])
        if (!file.exists()) {
            println "Kernel configuration not found"
            System.exit(1)
        }

        GroovyKernel kernel = new GroovyKernel()
        String json = file.text
        println JsonOutput.prettyPrint(json)
        kernel.configuration = Serializer.parse(file.text, Config)
        kernel.run()
    }
}
