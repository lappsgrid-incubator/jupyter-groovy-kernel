package org.lappsgrid.jupyter

import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.WebResource
import org.lappsgrid.serialization.Serializer

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.NewCookie

/**
 * @author Keith Suderman
 */
class HttpClient {
    String url
    String key
    Client client

    public HttpClient(String url, String key) {
        this.url = url
        this.key = key
        client = new Client()
    }

    public String get(String path) {
        if (path.startsWith('api')) {
            path = "$path?key=$key"
        }
        String session = 'b9d4310a735245889e1c61b4947ad7302ae3d3175abebec075d74c38e48f29014b273970e9da0403'
        NewCookie cookie = new NewCookie('galaxysession', session)
        return client.resource("$url/$path")
                .accept(MediaType.APPLICATION_JSON)
                .get(String)
    }

    public <T> String get(String path, Class<T> returnType) {
        String json = get(path)
        if (returnType == String) {
            return json
        }
        return Serializer.parse(json, returnType)
    }

    public String post(String path, Object entity) {
        if (path.startsWith('api')) {
            path = "$path?key=$key"
        }
        return client.resource("$url/$path")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(String)
    }

    public <T> String post(String path, Object entity, Class<T> returnType) {
        String json = post(path, entity)
        return Serializer.parse(json, returnType)
    }
}
