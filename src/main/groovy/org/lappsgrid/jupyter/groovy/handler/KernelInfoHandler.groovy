/*
 * Copyright (c) 2016 The Language Application Grid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.lappsgrid.jupyter.groovy.handler

import org.lappsgrid.jupyter.groovy.GroovyKernel
import org.lappsgrid.jupyter.groovy.msg.Header
import org.lappsgrid.jupyter.groovy.msg.Message
import org.slf4j.LoggerFactory

import static org.lappsgrid.jupyter.groovy.msg.Message.Type.KERNEL_INFO_REPLY

/**
 * Provides Jupyter with information about this kernel.
 *
 * TODO: For some reason Jupyter always complains about timeouts
 * while waiting for the kernel_info_reply message...
 *
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
        reply.header = new Header(KERNEL_INFO_REPLY, message.header.session)
        reply.parentHeader = message.header
        reply.identities = message.identities
        logger.debug("Sending kernel info reply.")
        send(reply)
    }
}
