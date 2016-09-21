/*
 * Copyright (c) 2016 The Language Application Grid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.lappsgrid.jupyter.groovy.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.lappsgrid.jupyter.groovy.GroovyKernel
import org.lappsgrid.jupyter.groovy.json.Serializer

/**
 * Defines the wire protocol used for messages exchanged with Jupyter.
 *
 * @author Keith Suderman
 */
@JsonPropertyOrder(['identities', 'header', 'parentHeader', 'metadata', 'content'])
class Message {
    /**
     * Definitions of the strings used as message type values.
     */
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

        // Stdin messages
        public static final String STDIN_REQUEST = "input_request"
        public static final String STDIN_REPLY = "input_reply"

        // Control messages
        public static final String SHUTDOWN_REQUEST = 'shutdown_request'
        public static final String SHUTDOWN_REPLY = 'shutdown_reply'

        // Misc
        public static final String UNDEFINED = 'undefined'
    }

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
