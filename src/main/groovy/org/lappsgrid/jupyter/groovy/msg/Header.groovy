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
 * The ZMQ header included with each message.
 *
 * @author Keith Suderman
 */
@JsonPropertyOrder(['id', 'username', 'session', 'date', 'type', 'version'])
class Header {
    String date
    @JsonProperty('msg_id')
    String id
    String username
    String session
    @JsonProperty('msg_type')
    String type
    String version

    public Header() { }
    public Header(Header header) {
        this.id = header.id
        this.date = header.date
        this.username = header.username
        this.session = header.session
        this.type = header.type
        this.version = header.version
    }

    public Header(Map header) {
        this.id = header.id
        this.date = header.date
        this.username = header.username
        this.session = header.session
        this.type = header.type
        this.version = header.verion ?: '5.0'
    }
    @Deprecated
    public Header(String type, Message message) {
        this(type, message.header.session)
    }

    public Header(String type, String session) {
        date = GroovyKernel.timestamp()
        id = GroovyKernel.uuid()
        username = 'kernel'
        this.type = type
        this.session = session
        this.version = '5.0'
    }

    String asJson() { return Serializer.toJson(this) }
    String prettyPrint() { return Serializer.toPrettyJson(this) }
}
