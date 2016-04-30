package org.lappsgrid.ipython.kernel

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Keith Suderman
 */
class KernelConfig {
    String transport
    String key
    @JsonProperty('ip')
    String host
    @JsonProperty('signature_scheme')
    String hmac

    @JsonProperty('control_port')
    String control
    @JsonProperty('shell_port')
    String shell
    @JsonProperty('stdin_port')
    String stdin
    @JsonProperty('hb_port')
    String heartbeat
    @JsonProperty('iopub_port')
    String iopub

    String control() {
        addressFor(control)
    }
    String shell() { addressFor(shell) }
    String stdin() { addressFor(stdin) }
    String heartbeat()    { addressFor(heartbeat) }
    String iopub() { addressFor(iopub) }

    private String addressFor(String port) {
        "$host://*:$port"
    }
}
