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

package org.lappsgrid.jupyter.groovy.threads

import org.lappsgrid.jupyter.groovy.GroovyKernel
import org.lappsgrid.jupyter.groovy.msg.Header
import org.lappsgrid.jupyter.groovy.msg.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ

import static org.lappsgrid.jupyter.groovy.msg.Message.Type.SHUTDOWN_REPLY
import static org.lappsgrid.jupyter.groovy.msg.Message.Type.SHUTDOWN_REQUEST

/**
 * @author Keith Suderman
 */
class ControlThread extends AbstractThread {

    public ControlThread(ZMQ.Socket socket, GroovyKernel kernel) {
        super(socket, kernel, ControlThread.class)
    }

    void run() {
        logger.info("ControlThread starting.")
        while (running) {
            Message message = readMessage()
            String type = message.header.type
            if (type == SHUTDOWN_REQUEST) {
                logger.info("Control handler received a shutdown request")
                running = false
                Message reply = new Message()
                reply.header = new Header(SHUTDOWN_REPLY, message)
                reply.parentHeader = message.header
                reply.content = message.content
                kernel.shutdown()
                send(reply)
            }
            else {
                logger.warn("Unhandled control message: {}", type)
            }
        }
        logger.info("ControlThread shutdown.")
    }
}
