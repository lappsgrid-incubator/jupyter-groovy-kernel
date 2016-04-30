package org.lappsgrid.ipython.kernel

import org.lappsgrid.jupyter.Message
import org.lappsgrid.ipython.kernel.zmq.RouterSocket
import org.lappsgrid.serialization.Serializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Keith Suderman
 */
class ControlSocket extends RouterSocket {
    private static Logger logger = LoggerFactory.getLogger(ControlSocket)

    public ControlSocket(String address) {
        super('Control', address)
    }

    @Override
    void handle(byte[] buffer) {
        String input = new String(buffer)
        logger.debug(input)
        Message message = Serializer.parse(input, Message)
        socket.send(buffer)
    }
}
