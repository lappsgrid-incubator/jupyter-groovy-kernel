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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ

/**
 * @author Keith Suderman
 */
class HeartbeatThread extends AbstractThread {

    HeartbeatThread(ZMQ.Socket socket, GroovyKernel kernel) {
        super(socket, kernel, org.lappsgrid.jupyter.groovy.threads.HeartbeatThread.class)
    }

    void run() {
        logger.info("Heartbeat thread starting.")
        ZMQ.Poller poller = new ZMQ.Poller(1)
        poller.register(socket, ZMQ.Poller.POLLIN)
        while (running) {
                if (poller.poll(0)) {
                    try {
                        byte[] buffer = socket.recv(0)
                        socket.send(buffer)
                    }
                    catch (Throwable t) {
                        // This likley means socket.close() has been called due to the kernel
                        // receiving a SHUTDOWN message from the notebook.
                        logger.warn("Error handling heartbeat socket. {}", t)
                    }
                }
                else {
                    sleep(1000) { true }
                }
        }
        logger.info("HearbeatThread shutdown.")
    }
}
