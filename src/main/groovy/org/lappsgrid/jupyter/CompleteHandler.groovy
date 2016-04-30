package org.lappsgrid.jupyter

import org.slf4j.LoggerFactory

/**
 * @author Keith Suderman
 */
class CompleteHandler extends AbstractHandler {

    public CompleteHandler(GroovyKernel kernel) {
        super(kernel)
        logger = LoggerFactory.getLogger(org.lappsgrid.jupyter.CompleteHandler)
    }

    void handle(Message message) {
        
    }
}
