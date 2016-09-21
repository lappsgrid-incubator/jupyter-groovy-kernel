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

import org.codehaus.groovy.control.CompilerConfiguration
import org.lappsgrid.jupyter.groovy.GroovyKernel
import org.lappsgrid.jupyter.groovy.msg.Header
import org.lappsgrid.jupyter.groovy.msg.Message
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ

import static Message.Type.*

/**
 * Does the actual work of executing user code.
 *
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
        CompilerConfiguration configuration = kernel.context.getCompilerConfiguration()
        compiler = new GroovyShell(this.class.classLoader, binding, configuration)
    }

    //TODO some security would be nice!
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

        if (message.content.allow_stdin) {
            kernel.allowStdin(true)
        }
        else {
            kernel.allowStdin(false)
        }

        // TODO Should check message.content.store_history here as well.

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
            ExpandoMetaClass meta = kernel.context.getMetaClass(script.class)
            meta.readline = { String prompt ->
                if (!kernel.stdinEnabled) {
                    return "STDIN is not enabled for this request."
                }
                Message stdinMsg = new Message()
                stdinMsg.content = [
                        prompt : prompt,
                        password: false
                ]
                stdinMsg.header = new Header(STDIN_REQUEST, message.header.session)
                stdinMsg.identities  = message.identities
                stdinMsg.parentHeader = message.header
                ZMQ.Socket socket = kernel.stdinSocket
                kernel.send(socket, stdinMsg)
                logger.trace("Send message on stdin socket.")
                stdinMsg = kernel.readMessage(socket)
                logger.trace ("Received response on stdin socket.")
                //println stdinMsg.asJson()
                return stdinMsg.content.value
            }
            meta.initialize()
            script.metaClass = meta
            logger.trace("code compiled")
            Object result = script.run()
            logger.trace("Ran script")
            if (!result) {
                result = 'Cell returned null.'
            }
            ++executionCount
            logger.debug("Result is {}", result)

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
            //e.printStackTrace()
            logger.error('Unable to execute code block.', e)
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
