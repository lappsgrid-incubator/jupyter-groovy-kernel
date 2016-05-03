package org.lappsgrid.jupyter.handler

import org.lappsgrid.jupyter.GroovyKernel
import org.lappsgrid.jupyter.Message
import org.slf4j.Logger
import org.zeromq.ZMQ

/**
 * @author Keith Suderman
 */
abstract class AbstractHandler implements IHandler {
    protected Logger logger
    protected GroovyKernel kernel

    public AbstractHandler(GroovyKernel kernel) {
        this.kernel = kernel
    }

    /** Sends to the shell socket by default. */
    void send(Message message) {
        kernel.send(message)
    }

    /** Sends a message to any socket. */
    void send(ZMQ.Socket socket, Message message) {
        kernel.send(socket, message)
    }

    /** Sends the message to the IOPub socket. */
    void publish(Message message) {
        kernel.publish(message)
    }
}