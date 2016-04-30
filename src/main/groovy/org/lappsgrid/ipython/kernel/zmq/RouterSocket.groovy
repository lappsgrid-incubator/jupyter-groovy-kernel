package org.lappsgrid.ipython.kernel.zmq

import org.zeromq.ZMQ

/**
 * @author Keith Suderman
 */
abstract class RouterSocket extends AbstractServer {
    public RouterSocket(String name, String address) {
        super(name, address, ZMQ.ROUTER)
    }

    @Override
    void bind() {
        socket.bind(address)
    }
}
