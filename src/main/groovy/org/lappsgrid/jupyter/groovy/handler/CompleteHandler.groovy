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

import org.codehaus.groovy.control.CompilationFailedException
import org.lappsgrid.jupyter.groovy.GroovyKernel
import org.lappsgrid.jupyter.groovy.msg.Header
import org.lappsgrid.jupyter.groovy.msg.Message
import org.slf4j.LoggerFactory

import static Message.Type.*

/**
 * The code completion handler.  The CompleteHandler is called by Jupyter to determine
 * if the line of code just entered should be executed immediately or if more input is required.
 * It also compiles the code to ensure that it is valid.
 *
 * @author Keith Suderman
 */
class CompleteHandler extends AbstractHandler {

    GroovyClassLoader compiler

    static final String COMPLETE_CHARS = '"\'};])'
    static final String INCOMPLETE_CHARS = '([:='

    String waitingFor = null

    public CompleteHandler(GroovyKernel kernel) {
        super(kernel)
        logger = LoggerFactory.getLogger(CompleteHandler)
        compiler = new GroovyClassLoader()
    }

    void handle(Message message) {
        String code = message.content.code.trim()
        logger.debug("Checking code: {}", code)

        // One of 'complete', 'incomplete', 'invalid', 'unknown'
        String status = 'unknown'
        String ch = code[-1]
        if (ch == '{') {
            waitingFor = '}'
            status = 'incomplete'
        }
        else if (waitingFor) {
            if (ch == waitingFor) {
                status = 'complete'
                waitingFor = null
            }
            else {
                status = 'incomplete'
            }
        }
        else if (INCOMPLETE_CHARS.contains(ch)) {
            logger.trace("Incomplete due to char {}", ch)
            status = 'incomplete'
        }
        else if (COMPLETE_CHARS.contains(ch)) {
            logger.trace("Complete due to char {}", ch)
            status = 'complete'
        }
        else {
            try {
                logger.trace("Attempting to compile code.")
                compiler.parseClass(code)
                logger.trace("Complete")
                status = 'complete'
            }
            catch (Exception e)
            {
                logger.debug("Invalid: {}", e.message)
            }
        }
        Message reply = new Message()
        reply.header = new Header(COMPLETE_REPLY, message)
        reply.identities = message.identities
        reply.parentHeader = message.header
        reply.content = [
                status: status,
        ]
        send(reply)
    }
}
