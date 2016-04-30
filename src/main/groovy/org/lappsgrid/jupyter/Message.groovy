package org.lappsgrid.jupyter

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.lappsgrid.annotation.processing.Serializer

/**
 * @author Keith Suderman
 */
class Message {
    static class Type {
        // Shell messages.
        static final String KERNEL_INFO_REQUEST = 'kernel_info_request'
        static final String KERNEL_INFO_REPLY = 'kernel_info_reply'
        static final String EXECUTE_REQUEST = 'execute_request'
        static final String EXECUTE_INPUT = 'execute_input'
        static final String EXECUTE_RESULT = 'execute_result'
        static final String EXECUTE_REPLY = 'execute_reply'
        static final String COMPLETE_REQUEST = 'complete_request'
        static final String COMPLETE_REPLY = 'complete_reply'
        static final String HISTORY_REQUEST = 'history_request'
        static final String HISTORY_REPLY = 'history_reply'
        static final String STATUS = 'status'
        static final String STREAM = 'stream'

        // Control messages
        static final String SHUTDOWN_REQUEST = 'shutdown_request'

        // Misc
        static final String UNDEFINED = 'undefined'
    }

    @JsonIgnore
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
