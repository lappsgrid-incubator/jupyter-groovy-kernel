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
import org.lappsgrid.jupyter.groovy.msg.Message
import org.slf4j.Logger
import org.zeromq.ZMQ

/**
 * The AbstractHandler class is the base class for all the other socket handlers
 * and provides some default helper methods.
 *
 * @author Keith Suderman
 */
abstract class AbstractHandler implements IHandler {
    protected Logger logger
    protected GroovyKernel kernel

    public AbstractHandler(GroovyKernel kernel) {
        this.kernel = kernel
    }

    /** Sends to the shell socket by default. */
    void send(Message message) {
        kernel.send(message)
    }

    /** Sends a message to the specified socket. */
    void send(ZMQ.Socket socket, Message message) {
        kernel.send(socket, message)
    }

    /** Sends the message to the IOPub socket. */
    void publish(Message message) {
        kernel.publish(message)
    }
}