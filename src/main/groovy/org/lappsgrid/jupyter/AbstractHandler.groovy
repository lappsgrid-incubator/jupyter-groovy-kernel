package org.lappsgrid.jupyter

import org.slf4j.Logger
import org.zeromq.ZMQ

/**
 * @author Keith Suderman
 */
abstract class AbstractHandler {
    protected Logger logger
    protected GroovyKernel kernel

    public AbstractHandler(GroovyKernel kernel) {
        this.kernel = kernel
    }

    abstract void handle(Message message)

}