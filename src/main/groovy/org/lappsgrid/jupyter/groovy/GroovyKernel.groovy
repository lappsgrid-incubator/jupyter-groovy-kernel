/*
 * Copyright (c) 2016 The Language Application Grid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.lappsgrid.jupyter.groovy

import org.lappsgrid.jupyter.groovy.context.DefaultGroovyContext
import org.lappsgrid.jupyter.groovy.context.GroovyContext
import org.lappsgrid.jupyter.groovy.handler.CompleteHandler
import org.lappsgrid.jupyter.groovy.handler.ExecuteHandler
import org.lappsgrid.jupyter.groovy.handler.HistoryHandler
import org.lappsgrid.jupyter.groovy.handler.IHandler
import org.lappsgrid.jupyter.groovy.handler.KernelInfoHandler
import org.lappsgrid.jupyter.groovy.json.Serializer
import org.lappsgrid.jupyter.groovy.msg.Header
import org.lappsgrid.jupyter.groovy.msg.Message
import org.lappsgrid.jupyter.groovy.security.HmacSigner
import org.lappsgrid.jupyter.groovy.threads.ControlThread
import org.lappsgrid.jupyter.groovy.threads.HeartbeatThread
import org.lappsgrid.jupyter.groovy.threads.ShellThread
import org.lappsgrid.jupyter.groovy.threads.StdinThread
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ

import java.security.InvalidKeyException
import java.text.DateFormat
import java.text.SimpleDateFormat

import static Message.Type.*

/**
 * The entry point for the Jupyter kernel.
 *
 * @author Keith Suderman
 */
class GroovyKernel {
    private static final Logger logger = LoggerFactory.getLogger(GroovyKernel)

    /** The timezone to use when generating time stamps. */
    static final TimeZone UTC = TimeZone.getTimeZone('UTC')

    private volatile boolean running = false
    boolean stdinEnabled = false

    static final String DELIM = "<IDS|MSG>"

    /** Used to generate the HMAC signatures for messages */
    HmacSigner hmac

    /** The UUID for this session. */
    String id

    /** Information from the connection file from Jupyter. */
    File connectionFile
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

    // Thread objects that manage sockets and socket handlers.
//    ControlThread controlThread
//    HeartbeatThread heartbeatThread
//    StdinThread shellThread

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

    void allowStdin(boolean allow) {
        stdinEnabled = allow
    }

    /** Sends a Message to the iopub socket. */
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

    /**
     * Reads a Jupyter message from a ZMQ socket.
     *
     * Each message consists of at least six blobs of bytes:
     * <ul>
     * <li>zero or more identities</li>
     * <li>'&lt;IDS|MSG&gt;'</li>
     * <li>HMAC signature</li>
     * <li>header</li>
     * <li>parent header</li>
     * <li>metadata</li>
     * <li>content</li>
     * </ul>
     *
     * @param socket The ZMQ.Socket object to read from.
     * @return a newly initialized Message object.
     */
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

    IHandler getHandler(String type) {
        return handlers[type]
    }

    public void run() {
        logger.info("Groovy Jupyter kernel starting.")
        running = true

        logger.debug("Parsing the connection file.")
        configuration = Serializer.parse(connectionFile.text, Config)

        logger.debug("Creating signing hmac with: {}", configuration.key)
        hmac = new HmacSigner(configuration.key)

        String connection = configuration.transport + '://' + configuration.host
        ZMQ.Context context = ZMQ.context(1)

        // A factory "method" for creating sockets.
        def newSocket = { int type, int port ->
            ZMQ.Socket socket = context.socket(type)
            socket.bind("$connection:$port")
            return socket
        }

        // Create all the sockets we need to listen to.
        hearbeatSocket = newSocket(ZMQ.REP, configuration.heartbeat)
        iopubSocket = newSocket(ZMQ.PUB, configuration.iopub)
        controlSocket = newSocket(ZMQ.ROUTER, configuration.control)
        stdinSocket = newSocket(ZMQ.ROUTER, configuration.stdin)
        shellSocket = newSocket(ZMQ.ROUTER, configuration.shell)

        // Create all the threads that respond to ZMQ messages.
        Thread heartbeatThread = new HeartbeatThread(hearbeatSocket, this)
        Thread controlThread = new ControlThread(controlSocket, this)
        Thread shellThread = new ShellThread(shellSocket, this)

        // Start all the socket handler threads
        List threads = [ heartbeatThread, controlThread, shellThread ]
        threads*.start()

        while (running) {
            // Nothing to do but navel gaze until another thread sets
            // running == false
            Thread.sleep(1000)
        }

        // Signal all threads that it is time to stop and then wait for
        // them to finish.
        logger.info("Shutting down")
        threads*.halt()
        threads*.join()
        logger.info("Done")
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            println "Invalid parameters passed to the Groovy kernel."
            println "Expected one parameter, found ${args.length}"
            args.each { println it }
            System.exit(1)
        }
        File config = new File(args[0])
        if (!config.exists()) {
            println "Kernel configuration not found."
            System.exit(1)
        }

        GroovyKernel kernel = new GroovyKernel()
        kernel.connectionFile = config
        kernel.run()
    }
}
