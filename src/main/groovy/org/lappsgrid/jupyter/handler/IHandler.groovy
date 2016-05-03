package org.lappsgrid.jupyter.handler

import org.lappsgrid.jupyter.Message

/**
 * @author Keith Suderman
 */
interface IHandler {
    void handle(Message message)
}