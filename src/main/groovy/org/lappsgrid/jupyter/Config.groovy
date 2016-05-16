package org.lappsgrid.jupyter

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * The Config class is used to parse the connection information passed to the
 * kernel from Jupyter.
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
