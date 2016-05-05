package org.lappsgrid.jupyter.security

import org.apache.commons.codec.binary.Hex
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.InvalidKeyException

/**
 * @author Keith Suderman
 */
class Key {
    private static Logger logger = LoggerFactory.getLogger(Key)

    private static final String TYPE = "HmacSHA256"
    private SecretKeySpec spec

    public Key(String key) {
        if (key == null) {
            throw new NullPointerException("No key specified.")
        }

        logger.info("Using signing key: {}", key)
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
            throw new RuntimeException("Invalid key exception while converting to HmacSHA256")
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
            throw new RuntimeException("Invalid key exception while converting to HmacSHA256")
        }
    }

    String asHex(byte[] buffer) {
        return Hex.encodeHexString(buffer)
    }
}
