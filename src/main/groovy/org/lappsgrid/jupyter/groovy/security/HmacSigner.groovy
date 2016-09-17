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

package org.lappsgrid.jupyter.groovy.security

import org.apache.commons.codec.binary.Hex
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.InvalidKeyException

/**
 * Generates HMACs (Hashed Message Authentication Codes) for
 * messages exchanged with Jupyter.  Jupyter supplies us with
 * the signing key in the connection information file that is
 * passed in as a parameter when a kernel is started.
 *
 * @author Keith Suderman
 */
class HmacSigner {
    private static Logger logger = LoggerFactory.getLogger(HmacSigner)

    private static final String TYPE = "HmacSHA256"
    private SecretKeySpec spec

    public HmacSigner(String key) {
        if (key == null) {
            throw new NullPointerException("No hmac specified.")
        }

        logger.info("Using signing hmac: {}", key)
        spec = new SecretKeySpec(key.bytes, TYPE)
    }

    String sign(List<String> msg) {
        try {
            Mac mac = Mac.getInstance(TYPE)
            mac.init(spec)
            msg.each {
                mac.update(it.bytes)
            }
            byte[] digest = mac.doFinal()
            return asHex(digest)
        } catch (InvalidKeyException e) {
            logger.error("Unable to sign message", e)
            throw new RuntimeException("Invalid hmac exception while converting to HmacSHA256")
        }
    }

    String signBytes(List<byte[]> msg) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256")
            mac.init(spec)
            msg.each {
                mac.update(it)
            }
            byte[] digest = mac.doFinal()
            return asHex(digest)
        } catch (InvalidKeyException e) {
            logger.error("Unable to sign message", e)
            throw new RuntimeException("Invalid hmac exception while converting to HmacSHA256")
        }
    }

    String asHex(byte[] buffer) {
        return Hex.encodeHexString(buffer)
    }
}
