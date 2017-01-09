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
import org.lappsgrid.jupyter.groovy.handler.IHandler
import org.lappsgrid.jupyter.groovy.msg.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ

/**
 * @author Keith Suderman
 */
class ShellThread extends AbstractThread {
    ShellThread(ZMQ.Socket socket, GroovyKernel kernel) {
        super(socket, kernel, ShellThread)
    }

    public void run() {
        logger.info('ShellThread starting.')
        ZMQ.Poller poller = new ZMQ.Poller(1)
        poller.register(socket, ZMQ.Poller.POLLIN)
        while (running) {
            try {
                if (poller.poll(0)) {
                    Message message = readMessage()
                    IHandler handler = kernel.getHandler(message.type())
                    if (handler) {
                        logger.debug("Handling message type {}", message.type())
                        handler.handle(message)
                    }
                    else {
                        logger.warn("Unhandled message type: {}", message.type())
                    }
                }
                else {
                    logger.trace("zzzz")
                    sleep(1000) {
                        true
                    }
                }
            }
            catch (Throwable t) {
                running = false
                logger.warn("Exception in ShellThread.", t)
            }
        }
        logger.info("ShellThread shutdown.")
    }
}
