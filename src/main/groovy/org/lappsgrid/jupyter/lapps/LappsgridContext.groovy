package org.lappsgrid.jupyter.lapps

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.lappsgrid.jupyter.context.DefaultGroovyContext

/**
 * @author Keith Suderman
 */
class LappsgridContext extends DefaultGroovyContext {

    @Override
    CompilerConfiguration getCompilerConfiguration() {
        ImportCustomizer customizer = new ImportCustomizer()
        def packages = [
                'org.lappsgrid.api',
                'org.lappsgrid.core',
                'org.lappsgrid.client',
                'org.lappsgrid.discriminator',
                'org.lappsgrid.serialization',
                'org.lappsgrid.serialization.lif',
                'org.lappsgrid.serialization.datasource',
                'org.lappsgrid.metadata'
        ]
        packages.each {
            customizer.addStarImports(it)
        }
        customizer.addStaticImport('org.lappsgrid.discriminator.Discriminators', 'Uri')

        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers(customizer)
        configuration.scriptBaseClass = BaseScript.class.name
        return configuration
    }

}
