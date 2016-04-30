package org.lappsgrid.ipython.kernel

import org.lappsgrid.ipython.kernel.zmq.PublishSocket

/**
 * @author Keith Suderman
 */
class IOPubSocket extends PublishSocket {
    public IOPubSocket(String address) {
        super('iopub', address)
    }

    void publish(String message) {
        handle(message.bytes)
    }

    void publish(byte[] buffer) {
        handle(buffer)
    }

    void handle(byte[] buffer) {
        socket.send(buffer, 0)
    }
}
