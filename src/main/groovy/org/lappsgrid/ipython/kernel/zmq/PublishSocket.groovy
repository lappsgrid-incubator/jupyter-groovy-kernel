package org.lappsgrid.ipython.kernel.zmq

import org.zeromq.ZMQ

/**
 * @author Keith Suderman
 */
abstract class PublishSocket extends AbstractServer {
    public PublishSocket(String name, String address) {
        super(name, address, ZMQ.PUB)
    }

    void bind() {
        socket.connect(address)
    }

}
