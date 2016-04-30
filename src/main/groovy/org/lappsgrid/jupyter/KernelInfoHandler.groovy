package org.lappsgrid.jupyter

import org.slf4j.LoggerFactory
import org.zeromq.ZMQ

import static org.lappsgrid.jupyter.Message.Type.getKERNEL_INFO_REPLY

/**
 * @author Keith Suderman
 */
class KernelInfoHandler extends AbstractHandler {

    public KernelInfoHandler(GroovyKernel kernel) {
        super(kernel)
        logger = LoggerFactory.getLogger(org.lappsgrid.jupyter.KernelInfoHandler)
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
        kernel.send(reply)
    }
}
