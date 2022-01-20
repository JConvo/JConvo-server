package com.grivera.jconvo.server;

import com.grivera.jconvo.commons.user.User;
import com.grivera.jconvo.commons.user.message.Message;
import com.grivera.jconvo.commons.user.message.MessageIntent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * Tracks and handles all user interactions.
 *
 * @author grivera
 * @version 1.0
 *
 */
public class UserHandler {

    private final List<User> userList = new ArrayList<>();
    private final ServerSocket serverSocket;

    /**
     *
     * Creates a handler for a specified server socket
     *
     * @param serverSocket a socket the server is hosted on.
     * @throws IOException if the server socket is not open.
     *
     */
    public UserHandler(ServerSocket serverSocket) throws IOException {

        if (serverSocket == null || serverSocket.isClosed()) throw new IOException("Server socket is close");
        this.serverSocket = serverSocket;

    }

    /**
     *
     * Starts connecting users to the server.
     *
     * @see User
     *
     */
    public void start() {

        while (!this.serverSocket.isClosed()) {

            this.loadNewUser();

        }

    }

    public void loadNewUser() {

        try {

            /* Load in a new user */
            Socket userSocket = this.serverSocket.accept();
            User newUser = new User(userSocket);
            newUser.setOnReceived(this::broadcast);
            newUser.setOnDie(this::cleanupUser);

            if (this.userList.contains(newUser)) {

                newUser.sendMessage(new Message("SYSTEM", MessageIntent.STATUS_FAILURE, "Invalid Username"));
                return;

            }

            newUser.sendMessage(new Message("SYSTEM", MessageIntent.STATUS_SUCCESS, "OK"));
            this.updateUserList(newUser);

            Thread thread = new Thread(newUser);
            thread.start();


        } catch (IOException e) {

            e.printStackTrace();
            this.close();

        }

    }

    private void cleanupUser(User user) {

        this.userList.remove(user);
        this.broadcast(
                new Message(
                        "SYSTEM", MessageIntent.SEND,
                        user.getUsername() + " has disconnected!")
        );

    }

    private void updateUserList(User newUser) {

        this.userList.add(newUser);
        this.broadcast(
                new Message("SYSTEM", MessageIntent.SEND, newUser.getUsername() + " has joined!"),
                user -> !user.getUsername().equals(newUser.getUsername())
        );

    }

    /**
     *
     * Broadcasts a message to all users.
     *
     * @param message the message to broadcast.
     *
     * @see Message
     *
     */
    public void broadcast(Message message) {

        this.broadcast(message, user -> true);

    }

    /**
     *
     * Broadcasts a message to all users.
     *
     * @param message the message to broadcast.
     * @param filter a filter to apply when selecting users to send to.
     *
     * @see Message
     * @see Predicate
     *
     */
    public void broadcast(Message message, Predicate<User> filter) {

        this.userList.parallelStream().filter(filter).forEach((user) -> user.sendMessage(message));
        System.out.println(message.getRaw());

    }

    /**
     *
     * Closes all connected user's connections
     *
     * @see User#close()
     *
     */
    public void close() {

        try {

            Predicate<User> connectedPredicate = User::isConnected;
            this.userList.parallelStream().filter(connectedPredicate.negate()).forEach(User::close);

            if (!this.serverSocket.isClosed()) this.serverSocket.close();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

}
