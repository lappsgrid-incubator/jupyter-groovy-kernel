package org.lappgrid.jupyter

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance
import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory
import com.github.jmchilton.blend4j.galaxy.HistoriesClient
import com.github.jmchilton.blend4j.galaxy.beans.History
import com.github.jmchilton.blend4j.galaxy.beans.HistoryContents
import com.github.jmchilton.blend4j.galaxy.beans.HistoryDetails
import com.github.jmchilton.blend4j.galaxy.beans.Library
import org.lappsgrid.jupyter.GalaxyClient
import org.lappsgrid.serialization.Serializer

/**
 * @author Keith Suderman
 */
class GalaxyTest {
//    static final String GALAXY_URL = "http://localhost:8000"
//    static final String GALAXY_USER = "suderman@cs.vassar.edu"
//    static final String API_KEY = System.getenv('GALAXY_KEY')

    GalaxyInstance galaxy;
    HistoriesClient histories;
    History history

    String HOST
    String KEY
    void setup() {
//        Properties p = new Properties()
//        p.load(new File('groovy-kernel.properties').newInputStream())
//        HOST = p.GALAXY_HOST
//        KEY = p.GALAXY_KEY
        HOST = 'http://galaxy.lappsgrid.org'
        KEY = '6f716395c326f6eda8bc4cec030f307f'
        galaxy = GalaxyInstanceFactory.get(HOST, KEY)
        histories = galaxy.historiesClient
        history = histories.histories.get(0)
    }

    HistoryContents findHistory(int hid) {
        return histories.showHistoryContents(history.id).find {
            it.hid == hid
        }
    }

    void library() {
        setup()
        List<Library> libs = galaxy.librariesClient.libraries
        println "There are ${libs.size()} libraries available."
        libs.each { l ->
            println "${l.name}: ${l.description}"
        }
    }

    /*
    payload = {
        'KEY'           : api_key,
        'tool_id'       : 'upload1',
        'history_id'    : history_id,
    }
    inputs = {
        'files_0|NAME'  : kwargs.get( 'filename', os.path.basename( filepath ) ),
        'files_0|type'  : 'upload_dataset',
        #TODO: the following doesn't work with tools.py
        #'dbkey'         : kwargs.get( 'dbkey', '?' ),
        'dbkey'         : '?',
        'file_type'     : kwargs.get( 'file_type', 'auto' ),
        'ajax_upload'   : u'true',
        #'async_datasets': '1',
    }
    payload[ 'inputs' ] = json.dumps( inputs )

    response = None
    with open( filepath, 'rb' ) as file_to_upload:
        files = { 'files_0|file_data' : file_to_upload }
        response = requests.post( full_url, data=payload, files=files )
    return response.json()
     */
    void run() {

        setup()

        HistoryContents contents = findHistory(2)

        if (contents) {
            println contents.url
            URL url = new URL(GALAXY_URL + contents.url + "?KEY=${API_KEY}")
            Map data = Serializer.parse(url.text, LinkedHashMap)
            File file = new File(data.file_name)
            if (!file.exists()) {
                println "File not found."
            }
            else {
                println file.text
            }
        }
        else {
            println "History item not found."
        }
    }

    void histories() {
        setup()
        histories.histories.each { History history ->
            println "Name: ${history.name}"
            println "ID  : ${history.id}"
            println "URL : ${history.url}"
//            HistoryDetails details = histories.showHistory(history.id)
//            println "Details Name : ${details.name}"
//            println "Details Id   : ${details.name}"
//            println "Details URL  : ${details.url}"
//            println "Details State: ${details.state}"
//            histories.showHistoryContents(history.id).each { HistoryContents contents ->
//                if (!contents.deleted) {
//                    println "   id     : ${contents.id}"
//                    println "   state  : ${contents.state}"
//                    println "   url    : ${contents.url}"
////                    println "   deleted: ${contents.deleted}"
//                    println "   hid    : ${contents.hid}"
//                    println "   type   : ${contents.historyContentType}"
//                    println "   name   : ${contents.name}"
////                    println "   purged : ${contents.purged}"
//                    println "   ----------"
//                }
//            }
            println()
        }
    }

    void client() {
        setup()
        GalaxyClient client = new GalaxyClient(HOST, KEY)
//        setup()
        File file = client.get(64)
        println file.text
    }

    static void main(String[] args) {
        new GalaxyTest().histories()
    }
}
