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
import org.lappsgrid.jupyter.groovy.msg.Message
import org.zeromq.ZMQ

/**
 * @author Keith Suderman
 */
abstract class AbstractThread extends Thread {
    boolean running = false
    ZMQ.Socket socket
    GroovyKernel kernel

    public AbstractThread(ZMQ.Socket socket, GroovyKernel kernel) {
        this.socket = socket
        this.kernel = kernel
    }

    void start() {
        running = true
        super.start()
    }

    void halt() { running = false }

    Message readMessage() {
        return kernel.readMessage(socket)
    }

    void send(Message message) {
        kernel.send(socket, message)
    }
}
