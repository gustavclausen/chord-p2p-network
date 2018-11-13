package main.java;

import main.java.messages.GetMessage;
import main.java.messages.PutMessage;
import main.java.utilities.Logging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class Main {
    /**
     NEW run configuration
        - Arg 1: Command (must be NEW) to start new network
        - Arg 2: Port for this process to bind to locally

     JOIN run configuration
        - Arg 1: Command (must be JOIN)
        - Arg 2: IP-address of existing peer in network
        - Arg 3: Port       of existing peer in network
        - Arg 4: Port for this process to bind to locally

     PUT run configuration
        - Arg 1: Command (must be PUT)
        - Arg 2: IP-address of existing peer in network
        - Arg 3: Port       of existing peer in network
        - Arg 4: Key (integer)
        - Arg 5: Value (string)

     GET run configuration
        - Arg 1: Command (must be GET)
        - Arg 2: IP-address of existing peer in network
        - Arg 3: Port       of existing peer in network
        - Arg 4: Port for this process to bind to locally
        - Arg 5: Key (integer)
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            Command c = null;

            try {
                c = Command.valueOf(args[0]);
            } catch (IllegalArgumentException e) {
                System.err.println(String.format("The command (%s) is not recognized. " +
                                                 "Please look at the given run configuration.", args[0]));
                System.exit(-1);
            }

            try {
                switch (c) {
                    case NEW:
                        if (args.length != 2)
                            throw new IllegalArgumentException("You must provide exactly two arguments " +
                                                               "(NEW <PORT TO BIND TO>) to use this command.");

                        createNewNetwork(args);
                        break;
                    case JOIN:
                        if (args.length != 4)
                            throw new IllegalArgumentException("You must provide exactly four arguments " +
                                                               "(JOIN <IP ADDRESS OF EXISTING PEER IN NETWORK> " +
                                                               "<PORT OF EXISTING PEER IN NETWORK> " +
                                                               "<OWN PORT TO BIND TO>) to use this command.");

                        joinExistingNetwork(args);
                        break;
                    case PUT:
                        if (args.length != 5)
                            throw new IllegalArgumentException("You must provide exactly five arguments " +
                                                               "(PUT <IP ADDRESS OF EXISTING PEER IN NETWORK> " +
                                                               "<PORT OF EXISTING PEER IN NETWORK> " +
                                                               "<OWN PORT TO BIND TO> <KEY (INTEGER)>" +
                                                               "<VALUE (STRING)> to use this command.");

                        put(args);
                        break;
                    case GET:
                        if (args.length != 5)
                            throw new IllegalArgumentException("You must provide exactly five arguments " +
                                                               "(PUT <IP ADDRESS OF EXISTING PEER IN NETWORK> " +
                                                               "<PORT OF EXISTING PEER IN NETWORK> " +
                                                               "<OWN PORT TO BIND TO> <KEY (INTEGER)>) to use" +
                                                               "this command.");

                        get(args);
                        break;
                }
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
        } else {
            throw new IllegalArgumentException("You must provide some arguments to run this program. Please look " +
                                               "at the given run configuration.");
        }
    }

    // New peer (the first one) starts a new network
    private static void createNewNetwork(String[] programArguments) {
        String ownIp = getOwnIp();
        int ownPort = parseInteger(programArguments[1]);

        System.out.println("Trying to start a new network ...");

        new Peer(new PeerAddress(ownIp, ownPort));
    }

    // New peer joins network by an existing peer in that network
    private static void joinExistingNetwork(String[] programArguments) {
        String ownIp = getOwnIp();
        int ownPort = parseInteger(programArguments[3]);

        String existingPeerIp = programArguments[1];
        int existingPeerPort = parseInteger(programArguments[2]);

        try {
            Peer peer = new Peer(new PeerAddress(ownIp, ownPort));

            System.out.println(String.format("Trying to join network by peer %s:%d ...",
                                             existingPeerIp,
                                             existingPeerPort));

            peer.joinNetworkByExistingPeer(new PeerAddress(existingPeerIp, existingPeerPort));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("The given address of the peer (IP: %s PORT: %s)" +
                                                             "is not valid. Please look at the given run " +
                                                             "configuration.",
                                                             existingPeerIp,
                                                             existingPeerPort));
        }
    }

    /**
     * Sends a 'PutMessage' to the peer which address is taken as argument to program.
     * Terminates the process afterwards.
     */
    private static void put(String[] programArguments) {
        String peerAddress = programArguments[1];
        int peerPort = parseInteger(programArguments[2]);

        int key = parseInteger(programArguments[3]);
        String value = programArguments[4];

        try (
             Socket socket = new Socket(peerAddress, peerPort);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())
        ) {
            outputStream.writeObject(new PutMessage(key, value));
        } catch (SocketException e) {
            Logging.debugLog("Could not connect to given peer. Full error details: " + e.getMessage(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a 'GetMessage' to the peer which address is taken as argument to program.
     * Afterwards listen on the port - also taken as argument to program - for any response.
     */
    private static void get(String[] programArguments) {
        String peerAddress = programArguments[1];
        int peerPort = parseInteger(programArguments[2]);

        String ownIp = getOwnIp();
        int ownPort = parseInteger(programArguments[3]);

        int key = parseInteger(programArguments[4]);

        try {
            ServerSocket listenSocket = new ServerSocket(ownPort); // Socket to listen for response to 'GetMessage'

            Socket requestSocket = new Socket(peerAddress, peerPort); // Socket for sending request to peer
            ObjectOutputStream requestOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());

            requestOutputStream.writeObject(new GetMessage(key, new PeerAddress(ownIp, ownPort)));

            // Waits for incoming connection with response
            Socket responseSocket = listenSocket.accept();
            ObjectInputStream responseInputStream = new ObjectInputStream(responseSocket.getInputStream());

            Object input = responseInputStream.readObject();

            // Print the content of the response
            if (input instanceof PutMessage) {
                PutMessage message = (PutMessage) input;

                System.out.println(String.format("Key: %d, value: %s", message.getKey(), message.getValue()));
            }
        } catch (SocketException e) {
            Logging.debugLog("Could not connect to given peer. Full error details: " + e.getMessage(), true);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static int parseInteger(String portAsString) {
        try {
            return Integer.parseInt(portAsString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("The given value (%s) must be a integer.", portAsString));
        }
    }

    private static String getOwnIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }

    private enum Command {
        NEW,
        JOIN,
        PUT,
        GET
    }
}
