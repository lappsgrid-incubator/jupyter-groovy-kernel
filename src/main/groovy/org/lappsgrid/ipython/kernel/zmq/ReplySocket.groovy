package org.lappsgrid.ipython.kernel.zmq

import org.zeromq.ZMQ

/**
 * @author Keith Suderman
 */
abstract class ReplySocket extends AbstractServer {
    public ReplySocket(String name, String address) {
        super(name, address, ZMQ.REP)
    }

    void bind() {
        socket.bind(address)
    }
}
