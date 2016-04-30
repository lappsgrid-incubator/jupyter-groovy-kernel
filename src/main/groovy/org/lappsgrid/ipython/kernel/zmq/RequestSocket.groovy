package org.lappsgrid.ipython.kernel.zmq

import org.zeromq.ZMQ

/**
 * @author Keith Suderman
 */
abstract class RequestSocket extends AbstractServer {
    public RequestSocket(String name, String address) {
        super(name, address, ZMQ.REQ)
    }

    void bind() {
        socket.connect(address)
    }
}
