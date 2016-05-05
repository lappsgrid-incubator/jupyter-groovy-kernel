package org.lappsgrid.jupyter.handler

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.lappsgrid.jupyter.BaseScript
import org.lappsgrid.jupyter.GroovyKernel
import org.lappsgrid.jupyter.msg.Header
import org.lappsgrid.jupyter.msg.Message
import org.slf4j.LoggerFactory

import static Message.Type.*

/**
 * @author Keith Suderman
 */
class ExecuteHandler extends AbstractHandler {

    int executionCount
    Binding binding
    GroovyShell compiler

    public ExecuteHandler(GroovyKernel kernel) {
        super(kernel)
        logger = LoggerFactory.getLogger(ExecuteHandler)
        executionCount = 0
        binding = new Binding()
        CompilerConfiguration configuration = getCompilerConfiguration()
        configuration.scriptBaseClass = BaseScript.class.name
        compiler = new GroovyShell(this.class.classLoader, binding, configuration)
    }

    void handle(Message message) {
        logger.info("Processing execute request")
        Message reply = new Message()
        reply.content = [ execution_state: 'busy' ]
        reply.header = new Header(STATUS, message)
        reply.parentHeader = message.header
        reply.identities = message.identities
        publish(reply)

        String code = message.content.code.trim()

        reply.header = new Header(EXECUTE_INPUT, message)
        reply.content = [
                execution_count: executionCount,
                code: message.content.code
        ]
        publish(reply)

        Exception error = null
        try {
            logger.debug("Running: {}", code)
            Script script = compiler.parse(code)
            logger.trace("code compiled")
            Object result = script.run()
            logger.trace("Ran script")
            if (!result) {
                result = 'Ok'
            }
            ++executionCount
            logger.debug("Result is {}", result)

//            reply.header = new Header(STREAM, message)
//            reply.content = [
//                    name: 'stdout',
//                    text: result.toString()
//            ]
//            publish(reply)

            reply.header = new Header(EXECUTE_RESULT, message)
            reply.content = [
                    execution_count: executionCount,
                    data: [ 'text/plain': result.toString() ],
                    metadata: [:]
            ]
            publish(reply)
        }
        catch (Exception e) {
            e.printStackTrace()
            error = e
            reply.header = new Header(STREAM, message)
            reply.content = [
                    name: 'stderr',
                    text: e.message
            ]
            publish(reply)
        }


        reply.header = new Header(STATUS, message)
        reply.content = [ execution_state: 'idle']
        publish(reply)

        reply.header = new Header(EXECUTE_REPLY, message)
        reply.metadata = [
                dependencies_met: true,
                engine: kernel.id,
                started: kernel.timestamp()
        ]
        reply.content = [
                execution_count: executionCount,
        ]
        if (error) {
            reply.metadata.status = 'error'
            reply.content.status = 'error'
            reply.content.ename = error.class.name
            reply.content.evalue = error.message
        }
        else {
            reply.metadata.status = 'ok'
            reply.content.status = 'ok'
            reply.content.user_expressions = [:]
        }
        send(reply)
    }

//    ClassLoader getLoader() {
//        ClassLoader loader = Thread.currentThread().contextClassLoader;
//        if (loader == null) {
//            loader = this.class.classLoader
//        }
//        return loader
//    }

    CompilerConfiguration getCompilerConfiguration() {
        ImportCustomizer customizer = new ImportCustomizer()
        /*
         * Custom imports can be defined in the ImportCustomizer.
         * For example:
         *   customizer.addImport("org.anc.xml.Parser")
         *   customizer.addStarImports("org.anc.util")
         *
         * The jar files for any packages imported this way must be
         * declared as Maven dependencies so they will be available
         * at runtime.
         */
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
        return configuration
    }

}
