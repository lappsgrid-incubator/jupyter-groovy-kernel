package org.lappgrid.jupyter

import com.github.jmchilton.blend4j.galaxy.ToolsClient
import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.WebResource
import groovy.json.JsonOutput
import org.lappsgrid.jupyter.GalaxyClient
import org.lappsgrid.jupyter.HttpClient
import org.lappsgrid.serialization.Serializer

import javax.ws.rs.core.MediaType

/**
 * @author Keith Suderman
 */
class HttpTest {

    static final String HOST = "http://galaxy.lappsgrid.org"
    static final String KEY = '6f716395c326f6eda8bc4cec030f307f'
//    static final String HOST = "http://localhost:8000"
//    static final String KEY = "5dbf0496443f17859085b8cd47fff10e"

    GalaxyClient galaxy

//    void setup() {
//        KEY = System.getenv('GALAXY_KEY')
//        galaxy = new GalaxyClient(HOST, KEY)
//    }
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
//        String url = "$HOST/api/histories?key=$KEY"
//        println groovy.json.JsonOutput.prettyPrint(new URL(url).text)
//        Client client = new Client()
//        WebResource resource = client.resource("$HOST/api/histories?key=$KEY")
//        String response = resource.accept(MediaType.APPLICATION_JSON).get(String.class)
//        println groovy.json.JsonOutput.prettyPrint(response)
        HttpClient client = new HttpClient(HOST, KEY)
        String json = client.get('api/histories/1b8cbc421a647113/contents/dataset_collections/ce93e6cde93e8163')
        prettyPrint(json)
//        List histories = Serializer.parse(json, List)
//        histories.each { Map history ->
//            println "${history.id} ${history.name}"
//        }
//        println "\nMRU"
//        json = client.get("/api/histories/most_recently_used?key=$KEY&id=most_recently_used")
//        json = client.get("history/current_history_json?key=$KEY")
//        Map history = Serializer.parse(json, Map)
//        println "${history.id} ${history.name}"

//        prettyPrint(json)
    }

    static void main(String[] args) {
        new HttpTest().run()
    }
}
