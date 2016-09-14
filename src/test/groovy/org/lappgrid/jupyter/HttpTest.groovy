package org.lappgrid.jupyter

import com.github.jmchilton.blend4j.galaxy.ToolsClient
import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import org.lappsgrid.jupyter.lapps.GalaxyClient

/**
 * @author Keith Suderman
 */
class HttpTest {

    static final String HOST = "http://galaxy.lappsgrid.org"
    static final String KEY = '6f716395c326f6eda8bc4cec030f307f'
//    static final String HOST = "http://localhost:8000"
//    static final String KEY = "5dbf0496443f17859085b8cd47fff10e"

    GalaxyClient galaxy

    void setup() {
//        KEY = System.getenv('GALAXY_KEY')
        galaxy = new GalaxyClient(HOST, KEY)
    }
//
//    void run() {
//        setup()
//        ToolsClient client = galaxy.galaxy.toolsClient
//        ToolsClient.FileUploadRequest request = new ToolsClient.FileUploadRequest(galaxy.history.id, new File("/tmp/hello.txt"))
//        ClientResponse response = client.uploadRequest(request)
//        println response.toString()
////        Map inputs = [
////                'files_0|NAME'  : 'groovy-ipython.txt',
////                'files_0|type'  : 'upload_dataset',
////                'dbkey'         : '?',
////                'file_type'     : 'txt',
////                'ajax_upload'   : 'true',
////        ]
////        Map payload = [
////                KEY           : KEY,
////                tool_id       : 'upload1',
////                history_id    : galaxy.history.id,
////                inputs: Serializer.toJson(inputs)
////        ]
//    }

//    void prettyPrint(String json) {
//        println JsonOutput.prettyPrint(json)
//    }

    void prettyPrint(Object object) {
        if (object instanceof String) {
            println JsonOutput.prettyPrint((String) object)
        }
        else {
            println Serializer.toPrettyJson(object)
        }
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
