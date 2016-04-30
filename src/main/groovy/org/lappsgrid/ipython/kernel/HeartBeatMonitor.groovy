package org.lappsgrid.ipython.kernel

import org.lappsgrid.ipython.kernel.zmq.ReplySocket

/**
 * @author Keith Suderman
 */
class HeartBeatMonitor extends ReplySocket {

    public HeartBeatMonitor(String address) {
        super("HBM", address)
    }

    @Override
    void handle(byte[] buffer) {
        socket.send(buffer)
    }

}
