package org.lappsgrid.jupyter.handler

import org.codehaus.groovy.control.CompilationFailedException
import org.lappsgrid.jupyter.GroovyKernel
import org.lappsgrid.jupyter.msg.Header
import org.lappsgrid.jupyter.msg.Message
import org.slf4j.LoggerFactory

import static Message.Type.*

/**
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
//                status = 'invalid'
            }
        }
        Message reply = new Message()
        reply.header = new Header(COMPLETE_REPLY, message)
        reply.identities = message.identities
        reply.parentHeader = message.header
        reply.content = [
                status: status,
//                indent: '    '
        ]
        send(reply)
    }

    void _handle(Message message) {
        String code = message.content.code.trim()
        String status = 'unknown'
        try {
            compiler.parseClass(code)
            status = 'complete'
        }
        catch (CompilationFailedException e) {
            status = 'incomplete'
        }
        Message reply = new Message()
        reply.header = new Header(COMPLETE_REPLY, message.header.session)
        reply.identities = message.identities
        reply.parentHeader = message.header
        reply.content = [ status: status ]
        send(reply)
    }
}
