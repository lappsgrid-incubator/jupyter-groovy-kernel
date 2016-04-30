package org.lappsgrid.ipython.kernel

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ

/**
 * @author Keith Suderman
 */
class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client)

    ZMQ.Context context
    ZMQ.Socket socket

    public Client(String address) {
        logger.debug("Client binding to {}", address)
        context = ZMQ.context(1)
        socket = context.socket(ZMQ.REQ)
        socket.connect(address)
//        this.address = address
    }

    String send(String message) {
        logger.debug("Sending message: {}", message)
        socket.send(message.getBytes(), 0)
        return new String(socket.recv(0))
    }

    void close() {
        socket.close()
        context.term()
    }

//    public void test() {
//        ZMQ.Context context = ZMQ.context(1);
//
//        //  Socket to talk to server
//        System.out.println("Connecting to hello world server…");
//
//        ZMQ.Socket requester = context.socket(ZMQ.REQ);
//        requester.connect(address);
//
//        for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
//            String request = "Hello";
//            System.out.println("Sending Hello " + requestNbr);
//            requester.send(request.getBytes(), 0);
//
//            byte[] reply = requester.recv(0);
//            System.out.println("Received " + new String(reply) + " " + requestNbr);
//        }
//        requester.close();
//        context.term();
//    }

//    static void main(String... args) {
//        AbstractServer server = new AbstractServer(10080)
//        server.start { Socket socket ->
//            socket.withStreams { input, output ->
//                BufferedReader reader = input.newReader()
//                String line = reader.readLine()
//                while (line) {
//                    output << line << '\n'
//                    line = reader.readLine()
//                }
//            }
//        }
//
//        Socket socket = new Socket("localhost", 10080)
//        socket.withStreams { input,output ->
//            def reader = input.newReader()
//            5.times { i ->
//                println "Sending message to the server socket"
//                output << "$i) Hello world\n"
//                println reader.readLine()
//            }
//        }
//        println "Client finished, killing the server"
//        server.close()
//        println "Waiting for the server to terminate"
//        println "Terminating."
//    }
}
