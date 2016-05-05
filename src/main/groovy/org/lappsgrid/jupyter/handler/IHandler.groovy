package org.lappsgrid.jupyter.handler

import org.lappsgrid.jupyter.msg.Message

/**
 * @author Keith Suderman
 */
interface IHandler {
    void handle(Message message)
}