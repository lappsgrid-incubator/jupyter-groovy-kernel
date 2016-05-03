package org.lappsgrid.jupyter.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

/**
 * Serializes objects to/from JSON.
 * <p>
 * The Serializer class is a thin wrapper around the Jackson ObjectMapper class.
 * The Jackson ObjectMapper class is thread-safe as long as it is initialized in a
 * single thread and not modified later. Once initialized the ObjectMapper can be used
 * by multiple threads without further synchronization.
 * <p>
 * We provide two static instances, one instance that pretty prints JSON and one
 * instance that strips whitespace.
 *
 * @author Keith Suderman
 */
class Serializer {
    private static ObjectMapper mapper;
    private static ObjectMapper prettyPrinter;

    static {
        mapper = new ObjectMapper()
        mapper.disable(SerializationFeature.INDENT_OUTPUT)
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        prettyPrinter = new ObjectMapper()
        prettyPrinter.enable(SerializationFeature.INDENT_OUTPUT)
        prettyPrinter.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
    private Serializer() {}

    /**
     * Parses a JSON string and creates an instance of the specified class.
     */
    public static <T> T parse(String json, Class<T> theClass) {
        T result = null
        try {
            result = (T) mapper.readValue(json, theClass)
        }
        catch(Exception e)
        {
            // Ignored. We return null to indicate an error.
        }
        return result;
    }

    /**
     * Returns a JSON representation of the object.
     */
    public static String toJson(Object object)
    {
        try {
            return mapper.writeValueAsString(object)
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /** Returns a pretty-printed JSON representation of the object. */
    public static String toPrettyJson(Object object)
    {
        try {
            return prettyPrinter.writeValueAsString(object)
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
