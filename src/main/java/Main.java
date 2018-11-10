package main.java;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    /*
     NEW run configuration
        - Arg 1: Command (must be NEW) to create new network
        - Arg 2: Port for this process to bind to locally

     JOIN run configuration
        - Arg 1: Command (must be JOIN)
        - Arg 2: IP-address of existing peer in network
        - Arg 3: Port of existing peer in network
        - Arg 4: Port for this process to bind to locally

     PUT run configuration
        - Arg 1: Command (must be PUT)
        - Arg 2: IP-address of existing peer in network
        - Arg 3: Port of existing peer in network
        - Arg 4: Port for this process to bind to locally // TODO: Find out if this is necessary
        - Arg 5: Key (integer)
        - Arg 6: Value (string)

     GET run configuration
        - Arg 1: Command (must be GET)
        - Arg 2: IP-address of existing peer in network
        - Arg 3: Port of existing peer in network
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
                        "Please read the documentation.", args[0]));
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
                        if (args.length != 6)
                            throw new IllegalArgumentException("You must provide exactly six arguments " +
                                    "(PUT <IP ADDRESS OF EXISTING PEER IN NETWORK> " +
                                    "<PORT OF EXISTING PEER IN NETWORK> " +
                                    "<OWN PORT TO BIND TO> <KEY (INTEGER)> <VALUE (STRING)>) to use this command.");

                        throw new RuntimeException("Not implemented");
                        //break;
                    case GET:
                        if (args.length != 5)
                            throw new IllegalArgumentException("You must provide exactly five arguments " +
                                    "(PUT <IP ADDRESS OF EXISTING PEER IN NETWORK> " +
                                    "<PORT OF EXISTING PEER IN NETWORK> " +
                                    "<OWN PORT TO BIND TO> <KEY (INTEGER)>) to use this command.");

                        throw new RuntimeException("Not implemented");
                        //break;
                }
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());

                System.exit(-1);
            }
        } else {
            throw new IllegalArgumentException("Please provide arguments.");
        }
    }

    private static void createNewNetwork(String[] programArguments) {
        String ownIp = getOwnIp();
        int ownPort = parsePort(programArguments[1]);

        System.out.println("Trying to start a new network ...");

        new Peer(ownIp, ownPort);
    }

    private static void joinExistingNetwork(String[] programArguments) {
        String ownIp = getOwnIp();
        int ownPort = parsePort(programArguments[3]);
        String peerAddress = programArguments[1];
        int peerPort = parsePort(programArguments[2]);

        try {
            Peer peer = new Peer(ownIp, ownPort);

            System.out.println(String.format("Trying to join network by peer %s:%d ...",
                    peerAddress,
                    peerPort));

            peer.connectToOtherPeer(peerAddress, peerPort);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("The given address of the peer (IP: %s PORT: %s)" +
                            "is not valid. Please read the documentation.",
                    peerAddress,
                    peerPort));
        }
    }

    private static int parsePort(String portAsString) {
        try {
            return Integer.parseInt(portAsString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("The given port (%s) is not valid.", portAsString));
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
