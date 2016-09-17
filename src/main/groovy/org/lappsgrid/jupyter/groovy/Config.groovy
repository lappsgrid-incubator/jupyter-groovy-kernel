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

package org.lappsgrid.jupyter.groovy

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * The Config class is used to parse the connection information that is passed to the
 * kernel by Jupyter.
 *
 * @author Keith Suderman
 */
class Config {
    String transport
    String key
    @JsonProperty('ip')
    String host
    @JsonProperty('signature_scheme')
    String scheme
    @JsonProperty('kernel_name')
    String name
    @JsonProperty('control_port')
    int control
    @JsonProperty('shell_port')
    int shell
    @JsonProperty('stdin_port')
    int stdin
    @JsonProperty('hb_port')
    int heartbeat
    @JsonProperty('iopub_port')
    int iopub
}
