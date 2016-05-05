package org.lappsgrid.jupyter.handler

import org.lappsgrid.jupyter.GroovyKernel
import org.lappsgrid.jupyter.msg.Header
import org.lappsgrid.jupyter.msg.Message
import org.slf4j.LoggerFactory

import static org.lappsgrid.jupyter.msg.Message.Type.KERNEL_INFO_REPLY

/**
 * @author Keith Suderman
 */
class KernelInfoHandler extends AbstractHandler {

    public KernelInfoHandler(GroovyKernel kernel) {
        super(kernel)
        logger = LoggerFactory.getLogger(KernelInfoHandler)
    }

    void handle(Message message) {
        logger.info("Processing kernel info request")
        Message reply = new Message()
        reply.content = [
                protocol_version: '5.0',
                implementation: 'groovy',
                implementation_version: '1.0.0',
                language_info: [
                        name: 'Groovy',
                        version: '2.4.6',
                        mimetype: '',
                        file_extension: '.groovy',
                        pygments_lexer: '',
                        codemirror_mode: '',
                        nbconverter_exporter: ''
                ],
                banner: 'Apache Groovy',
                help_links: []

        ]
        reply.header = new Header(KERNEL_INFO_REPLY, message)
        reply.parentHeader = message.header
        reply.identities = message.identities
        send(reply)
    }
}
