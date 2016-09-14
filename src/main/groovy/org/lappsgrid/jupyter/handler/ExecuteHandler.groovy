package org.lappsgrid.jupyter.handler

import org.codehaus.groovy.control.CompilerConfiguration
import org.lappsgrid.jupyter.GroovyKernel
import org.lappsgrid.jupyter.msg.Header
import org.lappsgrid.jupyter.msg.Message
import org.slf4j.LoggerFactory

import static Message.Type.*

/**
 * @author Keith Suderman
 */
class ExecuteHandler extends AbstractHandler {

    int executionCount
    Binding binding
    GroovyShell compiler
    Set<String> included

    public ExecuteHandler(GroovyKernel kernel) {
        super(kernel)
        logger = LoggerFactory.getLogger(ExecuteHandler)
        executionCount = 0
        binding = new Binding()
//        CompilerConfiguration configuration = getCompilerConfiguration()
//        configuration.scriptBaseClass = BaseScript.class.name
        CompilerConfiguration configuration = kernel.context.getCompilerConfiguration()
        compiler = new GroovyShell(this.class.classLoader, binding, configuration)
    }

    void handle(Message message) {
        logger.info("Processing execute request")
        Message reply = new Message()
        reply.content = [ execution_state: 'busy' ]
        reply.header = new Header(STATUS, message)
        reply.parentHeader = message.header
        reply.identities = message.identities
        publish(reply)

        // Get the code to be executed from the message.
        String code = message.content.code.trim()

        // Announce that we have the code.
        reply.header = new Header(EXECUTE_INPUT, message)
        reply.content = [
                execution_count: executionCount,
                code: message.content.code
        ]
        publish(reply)

        // Now try compiling and running the code.
        Exception error = null
        try {
            logger.debug("Running: {}", code)
            Script script = compiler.parse(code)
            script.metaClass = kernel.context.getMetaClass(script.class)
            logger.trace("code compiled")
            Object result = script.run()
            logger.trace("Ran script")
            if (!result) {
                result = 'Cell returned null.'
            }
            ++executionCount
            logger.debug("Result is {}", result)

//            reply.header = new Header(STREAM, message)
//            reply.content = [
//                    name: 'stdout',
//                    text: result.toString()
//            ]
//            publish(reply)

            // Publish the result of the execution.
            reply.header = new Header(EXECUTE_RESULT, message)
            reply.content = [
                    execution_count: executionCount,
                    data: [ 'text/plain': result.toString() ],
                    metadata: [:]
            ]
            publish(reply)
        }
        catch (Exception e) {
            e.printStackTrace()
            error = e
            reply.header = new Header(STREAM, message)
            reply.content = [
                    name: 'stderr',
                    text: e.message
            ]
            publish(reply)
        }

        // Tell Jupyter that this kernel is idle again.
        reply.header = new Header(STATUS, message)
        reply.content = [ execution_state: 'idle']
        publish(reply)

        // Send the REPLY to the original message. This is NOT the result of
        // executing the cell.  This is the equivalent of 'exit 0' or 'exit 1'
        // at the end of a shell script.
        reply.header = new Header(EXECUTE_REPLY, message)
        reply.metadata = [
                dependencies_met: true,
                engine: kernel.id,
                started: kernel.timestamp()
        ]
        reply.content = [
                execution_count: executionCount,
        ]
        if (error) {
            reply.metadata.status = 'error'
            reply.content.status = 'error'
            reply.content.ename = error.class.name
            reply.content.evalue = error.message
        }
        else {
            reply.metadata.status = 'ok'
            reply.content.status = 'ok'
            reply.content.user_expressions = [:]
        }
        send(reply)
    }
}
