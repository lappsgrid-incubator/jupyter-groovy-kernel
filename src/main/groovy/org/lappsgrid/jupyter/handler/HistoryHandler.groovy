package org.lappsgrid.jupyter.handler

import org.lappsgrid.jupyter.GroovyKernel
import org.lappsgrid.jupyter.Message

/**
 * @author Keith Suderman
 */
class HistoryHandler extends AbstractHandler {
    public HistoryHandler(GroovyKernel kernel) {
        super(kernel)
    }

    @Override
    void handle(Message message) {

    }
}
