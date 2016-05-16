package org.lappsgrid.jupyter.context

import org.codehaus.groovy.control.CompilerConfiguration

/**
 * @author Keith Suderman
 */
interface GroovyContext {
    CompilerConfiguration getCompilerConfiguration();
    MetaClass getMetaClass(Class aClass);
}