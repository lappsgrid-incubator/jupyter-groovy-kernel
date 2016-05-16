package org.lappsgrid.jupyter.handler

import org.lappsgrid.jupyter.msg.Message

/**
 * Objects that can respond to messages from a ZMQ.Socket.
 *
 * @author Keith Suderman
 */
interface IHandler {
    void handle(Message message)
}