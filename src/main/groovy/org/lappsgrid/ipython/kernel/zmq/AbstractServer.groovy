package org.lappsgrid.ipython.kernel.zmq

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import org.zeromq.ZMQException

/**
 * @author Keith Suderman
 */

abstract class AbstractServer {
    static final Logger logger = LoggerFactory.getLogger(AbstractServer)

    ZMQ.Context context
    ZMQ.Socket socket
    Thread worker
    String address
    String name
    int type

    public volatile boolean running

    public AbstractServer(String name, String address, int type) {
        this.name = name
        this.address = address
        this.type = type
    }

    void close() {
        logger.debug("{} is closing", name)
        socket.close()
        context.term()
        worker.interrupt()
    }

    void start() {
        logger.info("{} is starting", name)
        context = ZMQ.context(1)
        socket = context.socket(type)
//        socket.bind(address)
        bind()
        worker = Thread.start {
            running = true
            while(running) {
                try {
                    logger.debug("{}: Waiting for input.")
                    byte[] buffer = socket.recv(0)
                    logger.trace("{} received: {}", name, new String(buffer))
                    handle(buffer)
                }
                catch (ZMQException e) {
                    running = false
                }
            }
            logger.info("{} is shutting down.", name)
        }
    }

    abstract void handle(byte[] buffer)
    abstract void bind()

    public void test() {
        ZMQ.Context context = ZMQ.context(1);

        //  Socket to talk to clients
        ZMQ.Socket responder = context.socket(ZMQ.REP);
        try {
            responder.bind("tcp://*:5555");
        }
        catch (ZMQException e) {
            println e.getMessage()
        }

        while (!Thread.currentThread().isInterrupted()) {
            // Wait for next request from the client
            byte[] request = responder.recv(0);
            System.out.println("Received Hello");

            // Do some 'work'
            Thread.sleep(1000);

            // Send reply back to client
            String reply = "World";
            responder.send(reply.getBytes(), 0);
        }
        responder.close();
        context.term();
    }
}

