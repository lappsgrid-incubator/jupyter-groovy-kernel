package org.lappsgrid.ipython.kernel

import org.lappsgrid.ipython.kernel.zmq.AbstractServer
import org.lappsgrid.ipython.kernel.zmq.PublishSocket
import org.lappsgrid.serialization.Serializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.InvalidKeyException

/**
 * @author Keith Suderman
 */
class Kernel {
    public static final String HASH_METHOD = "HmacSHA256"
    private static final Logger logger = LoggerFactory.getLogger(Kernel)

    volatile boolean running = false;
    String uuid = UUID.randomUUID()


    String example_json = """
    {
        "stdin_port": 48691,
        "ip": "127.0.0.1",
        "control_port": 44808,
        "hb_port": 49691,
        "signature_scheme": "hmac-sha256",
        "key": "",
        "shell_port": 40544,
        "transport": "tcp",
        "iopub_port": 43462
    }
"""

    public void run(File file) {
        KernelConfig config = Serializer.parse(file.text, KernelConfig)

        AbstractServer hbm = new HeartBeatMonitor(config.heartbeat())
        IOPubSocket iopub = new IOPubSocket(config.iopub())
        AbstractServer shell = new ShellSocket(config.shell())
        AbstractServer control = new ControlSocket(config.control())
        List<AbstractServer> servers = [ hbm, shell, control ]
        servers.each { it.start() }

        running = true
        while (running) {
            Thread.sleep(1000)
        }

    }

    static String sign(String key, String data) {
        try {
            Mac mac = Mac.getInstance(HASH_METHOD)
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HASH_METHOD)
            mac.init(secretKeySpec)
            byte[] digest = mac.doFinal(data.getBytes())
            return digest.encodeBase64().toString()
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception while converting to " + HASH_METHOD)
        }
    }

    static void main(String[] args) {
        File config = new File("src/test/resources/kernel.json") //new File(args[0])
        if (!config.exists()) {
            println "Config file not found."
            System.exit(1)
        }
        new Kernel().run(config)
//        Thread server = Thread.start {
//            new AbstractServer().test()
//        }
//        Thread client = Thread.start {
//            new Client().test()
//        }
//        server.interrupt()
//        server.join()
//        client.join()
    }
}
