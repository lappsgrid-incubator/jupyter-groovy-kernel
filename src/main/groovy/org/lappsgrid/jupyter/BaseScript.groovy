package org.lappsgrid.jupyter

/**
 * @author Keith Suderman
 */
abstract class BaseScript extends Script {

    GalaxyClient galaxy = new GalaxyClient(GroovyKernel.GALAXY_HOST, GroovyKernel.GALAXY_KEY)

    File get(Integer hid) {
        println "Getting history item $hid"
        File file = galaxy.get(hid)
        if (file == null) {
            println "Galaxy client returned a null object."
        }
        else if (!file.exists()) {
            println "File not found: ${file.path}"
        }
        return file
    }

    void put(String path) {
        put(new File(path))
    }

    void put(File file) {
        println "Adding ${file.path} to the current history."
        galaxy.put(file)
    }

    void exit() {
        System.exit(0)
    }
}
