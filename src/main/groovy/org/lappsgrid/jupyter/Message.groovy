package org.lappsgrid.jupyter

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.lappsgrid.jupyter.json.Serializer

/**
 * @author Keith Suderman
 */
@JsonPropertyOrder(['identities', 'header', 'parentHeader', 'metadata', 'content'])
class Message {
    public static class Type {
        // Shell messages.
        public static final String KERNEL_INFO_REQUEST = 'kernel_info_request'
        public static final String KERNEL_INFO_REPLY = 'kernel_info_reply'
        public static final String EXECUTE_REQUEST = 'execute_request'
        public static final String EXECUTE_INPUT = 'execute_input'
        public static final String EXECUTE_RESULT = 'execute_result'
        public static final String EXECUTE_REPLY = 'execute_reply'
        public static final String COMPLETE_REQUEST = 'is_complete_request'
        public static final String COMPLETE_REPLY = 'is_complete_reply'
        public static final String HISTORY_REQUEST = 'history_request'
        public static final String HISTORY_REPLY = 'history_reply'
        public static final String STATUS = 'status'
        public static final String STREAM = 'stream'

        // Control messages
        public static final String SHUTDOWN_REQUEST = 'shutdown_request'
        public static final String SHUTDOWN_REPLY = 'shutdown_reply'

        // Misc
        public static final String UNDEFINED = 'undefined'
    }

    //@JsonIgnore
    List identities = []
    Header header
    @JsonProperty('parent_header')
    Header parentHeader
    Map metadata
    Map content

    public Message(String type) {
        header = new Header()
        header.type = type
        header.date = GroovyKernel.timestamp()
    }
    String type() { return header.type }
    String asJson() { return Serializer.toJson(this) }
}
