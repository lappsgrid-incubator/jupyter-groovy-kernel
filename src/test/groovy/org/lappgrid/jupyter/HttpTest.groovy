package org.lappgrid.jupyter

import com.github.jmchilton.blend4j.galaxy.ToolsClient
import com.sun.jersey.api.client.ClientResponse
import org.lappsgrid.jupyter.lapps.GalaxyClient

/**
 * @author Keith Suderman
 */
class HttpTest {

    static final String HOST = "http://localhost:8000"
    String key
    GalaxyClient galaxy

    void setup() {
        key = System.getenv('GALAXY_KEY')
        galaxy = new GalaxyClient(HOST, key)
    }

    void run() {
        setup()
        ToolsClient client = galaxy.galaxy.toolsClient
        ToolsClient.FileUploadRequest request = new ToolsClient.FileUploadRequest(galaxy.history.id, new File("/tmp/hello.txt"))
        ClientResponse response = client.uploadRequest(request)
        println response.toString()
//        Map inputs = [
//                'files_0|NAME'  : 'groovy-ipython.txt',
//                'files_0|type'  : 'upload_dataset',
//                'dbkey'         : '?',
//                'file_type'     : 'txt',
//                'ajax_upload'   : 'true',
//        ]
//        Map payload = [
//                hmac           : hmac,
//                tool_id       : 'upload1',
//                history_id    : galaxy.history.id,
//                inputs: Serializer.toJson(inputs)
//        ]
    }

    static void main(String[] args) {
        new HttpTest().run()
    }
}
