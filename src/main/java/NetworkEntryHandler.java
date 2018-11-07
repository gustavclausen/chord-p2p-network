package main.java;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class NetworkEntryHandler {
    /*** TWO RUN CONFIGURATIONS ***/
    /* FIRST */
    // Args 1: Command (must be NEW) to create new network
    // Args 2: Port for this process to bind to locally

    /* SECOND */
    // Args 1: Command (must be JOIN) to join existing network of peers
    // Args 2: IP-address of existing peer in network
    // Args 3: Port of existing peer in network
    // Args 4: Port for this process to bind to locally
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
                }
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());

                System.exit(-1);
            }
        }
    }

    private static void createNewNetwork(String[] programArguments) {
        int ownPort = parsePort(programArguments[1]);
        InetSocketAddress ownAddress = getLocalAddress(ownPort);

        System.out.println("Trying to start a new network ...");

        new Peer(ownAddress);
    }

    private static void joinExistingNetwork(String[] programArguments) {
        String peerAddress = programArguments[1];
        int peerPort = parsePort(programArguments[2]);
        int ownPort = parsePort(programArguments[3]);
        InetSocketAddress ownAddress = getLocalAddress(ownPort);

        try {
            InetSocketAddress addressOfExistingPeer = new InetSocketAddress(peerAddress, peerPort);

            Peer peer = new Peer(ownAddress);

            System.out.println(String.format("Trying to join network by peer %s:%d ...",
                                             addressOfExistingPeer.getHostName(),
                                             addressOfExistingPeer.getPort(),
                                             ownAddress.getHostName(),
                                             ownAddress.getPort()));

            peer.connectToPeer(addressOfExistingPeer);
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

    private static InetSocketAddress getLocalAddress(int portToBindTo) {
        try {
            return new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), portToBindTo);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }

    private enum Command {
        NEW,
        JOIN
    }
}
