package org.lappsgrid.jupyter

import org.slf4j.LoggerFactory
import org.zeromq.ZMQ

import static org.lappsgrid.jupyter.Message.Type.getEXECUTE_INPUT
import static org.lappsgrid.jupyter.Message.Type.getEXECUTE_REPLY
import static org.lappsgrid.jupyter.Message.Type.getEXECUTE_RESULT
import static org.lappsgrid.jupyter.Message.Type.getSTATUS
import static org.lappsgrid.jupyter.Message.Type.getSTATUS
import static org.lappsgrid.jupyter.Message.Type.getSTREAM

/**
 * @author Keith Suderman
 */
class ExecuteHandler extends AbstractHandler {

    int executionCount

    public ExecuteHandler(GroovyKernel kernel) {
        super(kernel)
        logger = LoggerFactory.getLogger(org.lappsgrid.jupyter.ExecuteHandler)
        executionCount = 0
    }

    void handle(Message message) {
        logger.info("Processing execute request")
        Message reply = new Message()
        reply.content = [ execution_state: 'busy' ]
        reply.header = new Header(STATUS, message)
        reply.parentHeader = message.parentHeader
        kernel.publish(reply)

        reply.content = [
                execution_count: executionCount,
                code: message.content.code
        ]
        reply.header = new Header(EXECUTE_INPUT, message)
        kernel.publish(reply)

        reply.content = [
                name: 'stdout',
                text: 'hello world!'
        ]
        reply.header = new Header(STREAM, message)
        kernel.publish(reply)

        reply.content = [
                execution_count: executionCount,
                data: [ 'text/plain': 'result!' ],
                metadata: [:]
        ]
        reply.header = new Header(EXECUTE_RESULT, message)
        kernel.publish(reply)

        reply.content = [ execution_state: 'idle']
        reply.header = new Header(STATUS, message)
        kernel.publish(reply)

        reply.metadata = [
                dependencies_met: true,
                engine: id,
                status: 'ok',
                started: kernel.timestamp()
        ]
        reply.content = [
                status: 'ok',
                execution_count: executionCount,
                user_variables: [:],
                payload: [],
                user_expressions: [:]
        ]
        reply.header = new Header(EXECUTE_REPLY, message)
        kernel.send(reply)
        ++executionCount
    }
}
