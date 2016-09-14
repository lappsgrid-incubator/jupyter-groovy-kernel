package org.lappsgrid.jupyter.context

import org.codehaus.groovy.control.CompilerConfiguration

/**
 * @author Keith Suderman
 */
class DefaultGroovyContext implements GroovyContext {
    @Override
    CompilerConfiguration getCompilerConfiguration() {
        return new CompilerConfiguration()
    }

    @Override
    MetaClass getMetaClass(Class aClass) {
        MetaClass mc = new ExpandoMetaClass()
        mc.initialize()
        return mc
    }
}
