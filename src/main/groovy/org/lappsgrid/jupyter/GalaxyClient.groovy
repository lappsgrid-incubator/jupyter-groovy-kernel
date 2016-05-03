package org.lappsgrid.jupyter

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance
import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory
import com.github.jmchilton.blend4j.galaxy.HistoriesClient
import com.github.jmchilton.blend4j.galaxy.ToolsClient
import com.github.jmchilton.blend4j.galaxy.beans.History
import com.github.jmchilton.blend4j.galaxy.beans.HistoryContents
import com.sun.jersey.api.client.ClientResponse
import org.lappsgrid.serialization.Serializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Keith Suderman
 */
class GalaxyClient {
    static final Logger logger = LoggerFactory.getLogger(GalaxyClient)

    GalaxyInstance galaxy
    HistoriesClient histories
    ToolsClient tools
    History history

    public GalaxyClient(String url, String key) {
        galaxy = GalaxyInstanceFactory.get(url, key)
        if (galaxy) {
            histories = galaxy.historiesClient
            tools = galaxy.toolsClient
            history = histories.histories.get(0)
        }
    }

    void put(String path) {
        put(new File(path))
    }

    void put(File file) {
        logger.info("Attempting to put file {} to history {}", file.path, history.id)
        ToolsClient.FileUploadRequest request = new ToolsClient.FileUploadRequest(history.id, file)
        tools.uploadRequest(request)
    }

    File get(Integer hid) {
        logger.info("Getting history item {}", hid)
        HistoryContents contents = histories.showHistoryContents(history.id).find { it.hid == hid }
        if (contents) {
            URL url = new URL(galaxy.galaxyUrl + contents.url + "?key=${galaxy.apiKey}")
            Map data = Serializer.parse(url.text, LinkedHashMap)
            File file = new File(data.file_name)
            if (!file.exists()) {
                println "File not found."
                file = null
            }
            logger.info("Found file {}", file.path)
            return file
        }
        else {
            println "No such history item: ${hid}"
            return null
        }
    }

}
