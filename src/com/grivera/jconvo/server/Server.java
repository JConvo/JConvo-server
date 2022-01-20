package com.grivera.jconvo.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Represents a JConvo Server.
 *
 * @author grivera
 * @version 1.0
 */
public class Server {

    private final ServerSocket serverSocket;

    /**
     *
     * Creates a Server instance.
     *
     * @param serverSocket the socket the server is hosted on.
     * @throws IOException if the server sock is invalid.
     *
     * @see ServerSocket
     *
     */
    public Server(ServerSocket serverSocket) throws IOException {

        if (serverSocket == null || serverSocket.isClosed()) throw new IOException("ServerSocket must be open");
        this.serverSocket = serverSocket;

    }

    /**
     *
     * Starts the server.
     *
     * @throws IOException if the server socket isn't open.
     * @see UserHandler
     *
     */
    public void start() throws IOException {

        UserHandler handler = new UserHandler(this.serverSocket);
        handler.start();

    }

}
