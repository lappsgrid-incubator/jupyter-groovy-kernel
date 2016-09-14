package org.lappsgrid.jupyter.security

import org.apache.commons.codec.binary.Hex
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.InvalidKeyException

/**
 * Use this class to generate all HMAC (Hashed Message Authentication Codes) for
 * messages with this class for consistency and simplicity.
 *
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
