package org.lappsgrid.ipython.kernel

import org.lappsgrid.jupyter.Header
import org.lappsgrid.jupyter.Message
import org.lappsgrid.ipython.kernel.zmq.RouterSocket
import org.lappsgrid.serialization.Serializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Keith Suderman
 */
class ShellSocket extends RouterSocket {
    private static final Logger logger = LoggerFactory.getLogger(ShellSocket)

    private IOPubSocket iopub

    ShellSocket(String address, IOPubSocket iopub) {
        super('shell', address)
        this.iopub = iopub
    }

    @Override
    void handle(byte[] buffer) {
        String input = new String(buffer)
        logger.debug(input)
        Message message = Serializer.parse(input, Message)
        Message reply = new Message()
        reply.header = new Header(message.header)
        reply.header.id = UUID.randomUUID()
        switch (message.type()) {
            case Message.Type.kernel_info_request:
                logger.debug("Received a kernel info request")
                reply.header.type = Message.Type.kernel_info_reply.name()
                reply.content = [:]
                reply.content.with {
                    protocol_version = '1.0.0'
                    implementation = 'compiler'
                    implementation_version = '1.0.0'
                    language_info = [
                            name          : 'Groovy',
                            version       : '2.4.6',
                            mimetype      : 'text/x-compiler',
                            file_extension: 'compiler',
                    ]
                    banner = 'Apache Groovy 2.4.6'
                }
                break
            case Message.Type.execute_request:
                reply.header.type = Message.Type.execute_reply.name()
                break
        }
        socket.send(Serializer.toJson(reply).bytes, 0)
    }
}
