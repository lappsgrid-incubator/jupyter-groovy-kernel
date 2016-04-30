package org.lappsgrid.ipython.kernel.tcp


/**
 * @author Keith Suderman
 */

class Server {
    static final int DEFAULT_PORT = 1999

    volatile boolean running = false;

    ServerSocket server;
    Closure handler

    Server() {
        this(DEFAULT_PORT)
    }

    Server(String port) {
        this(port as Integer)
    }

    Server(int port) {
        server = new ServerSocket(port)
    }

    Server(String port, Closure handler) {
        this(port as Integer, handler)
    }

    Server(int port, Closure handler) {
        server = new ServerSocket(port)
//        start(handler)
        this.handler = handler
    }

    void close() {
        // Closing the server socket will cause the accept() to exit.
        server.close();
    }

    void start(Closure handler) {
        this.handler = handler
        this.start()
    }

    void start() {
        Thread.start {
            println "Starting the server."
            running = true
            while (running) {
                println "AbstractServer looping"
                try
                {
                    // The server.accept closure will be run in its own thread.
                    server.accept { Socket socket ->
                        handler(socket)
                    }
                }
                catch (SocketException e)
                {
                    // The server socket was closed.
                    running = false
                }
            }
            println "AbstractServer shutting down."
        }
    }
}

