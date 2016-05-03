package org.lappsgrid.jupyter

import com.sun.jersey.api.client.ClientResponse

/**
 * @author Keith Suderman
 */
abstract class BaseScript extends Script {

    static final String KEY = System.getenv('GALAXY_KEY')
    GalaxyClient galaxy = new GalaxyClient('http://localhost:8000', KEY)

    File get(Integer hid) {
        println "Getting history item $hid"
        File file = galaxy.get(hid)
        if (!file.exists()) {
            println "File not found"
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
