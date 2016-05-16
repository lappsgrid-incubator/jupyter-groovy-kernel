package org.lappsgrid.jupyter

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance
import com.github.jmchilton.blend4j.galaxy.HistoriesClient
import com.github.jmchilton.blend4j.galaxy.ToolsClient
import com.github.jmchilton.blend4j.galaxy.beans.History
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer

/**
 * @author Keith Suderman
 */
abstract class BaseScript extends Script {

//    String GALAXY_HOST
//    String GALAXY_KEY

    GalaxyClient galaxy = new GalaxyClient(GroovyKernel.GALAXY_HOST, GroovyKernel.GALAXY_KEY)

//    private void init() {
//        if (galaxy) return
//        println "Initializing Galaxy host ${GALAXY_HOST}"
//        galaxy = new GalaxyClient(GALAXY_HOST, GALAXY_KEY)
//    }

    File get(Integer hid) {
//        init()
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

//    void host(String host) {
//        GALAXY_HOST = host
//    }
//    void key(String key) {
//        GALAXY_KEY = key
//    }

    void put(String path) {
        put(new File(path))
    }

    void put(File file) {
        println "Adding ${file.path} to the current history."
//        init()
        galaxy.put(file)
    }

    Object parse(String json) {
        return parse(json, Data)
    }

    Object parse(String json, Class theClass) {
        return Serializer.parse(json, theClass)
    }

    String toJson(Object o) {
        return Serializer.toJson(o)
    }
    String toPrettyJson(Object o) {
        return Serializer.toPrettyJson(o)
    }
    
    GalaxyInstance galaxy() { return galaxy.galaxy }
    HistoriesClient histories() { return galaxy.histories }
    ToolsClient tools() { return galaxy.tools }
    History history() { return galaxy.history }

    void exit() {
        System.exit(0)
    }
}
