package main.java;

import main.java.exceptions.FaultyPeerException;
import main.java.messages.*;
import main.java.utilities.Logging;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a peer in the network
 */
class Peer {
    private final PeerAddress ownAddress;

    /*
     * Address of the peer laying next to this peer clockwise "around the ring".
     * Defined as a successor to this peer.
     */
    private PeerAddress successor;
    private PeerAddress nextSuccessor; // Address of the successor's successor

    private final Map<Integer, String> storedData;

    Peer(PeerAddress ownAddress) {
        this.ownAddress = ownAddress;
        this.storedData = new HashMap<>();

        System.out.println(String.format("Peer started on %s:%d (ID: %s)",
                                         this.ownAddress.getIp(),
                                         this.ownAddress.getPort(),
                                         this.ownAddress.getHashId()));

        new Listener(this).start(); // Starts to listen for incoming connections
    }

    private class Listener extends Thread {
        private final Peer peer; // Reference to peer that initialized this thread

        private Listener(Peer peer) {
            this.peer = peer;
        }

        /**
         * Awaits incoming connections on the given port of the peer, and starts the process of
         * handling the request from the incoming connection on a separate thread
         */
        @Override
        public void run() {
            try (ServerSocket socket = new ServerSocket(this.peer.ownAddress.getPort())) {
                System.out.println("Listening...");

                while (true) {
                    Socket clientSocket = socket.accept();
                    new ClientHandler(clientSocket, this.peer).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler extends Thread {
        private final Socket clientSocket;
        private final Peer peer; // Reference to peer that initialized this thread

        private ClientHandler(Socket clientSocket, Peer peer) {
            this.clientSocket = clientSocket;
            this.peer = peer;
        }

        @Override
        public void run() {
            try {
                ObjectInputStream inputStream = new ObjectInputStream(this.clientSocket.getInputStream());

                Object input = inputStream.readObject(); // Incoming message

                // Check which type of message it is
                if (input instanceof JoinMessage) {
                    /*
                     * Evaluate and acts upon the placement of a new peer joining the network relative to
                     * this peer receiving the message
                     */
                    RoutingHandler.placementOfNewPeer(this.peer, (JoinMessage) input);
                }
                else if (input instanceof OrganizeMessage) {
                    OrganizeMessage organizeMessage = (OrganizeMessage) input;

                    switch (organizeMessage.getType()) {
                        case SET_NEW_SUCCESSOR:
                            this.peer.setSuccessor(organizeMessage.getPeerToPointTo());
                            break;
                        case SET_NEW_NEXT_SUCCESSOR:
                            this.peer.setNextSuccessor(organizeMessage.getPeerToPointTo());
                            break;
                        default:
                            break;
                    }
                }
                else if (input instanceof NextSuccessorMessage) {
                    this.peer.handleNextSuccessorMessage((NextSuccessorMessage) input);
                }
                else if (input instanceof PutMessage) {
                    PutMessage putMessage = (PutMessage) input;

                    // TODO: Store storedData in network
                    System.out.println(String.format("Received PUT-message (key: %d, value: %s, ID: %s)",
                                                     putMessage.getKey(),
                                                     putMessage.getValue(),
                                                     putMessage.getKeyHashId()));
                }
                else if (input instanceof GetMessage) {
                    GetMessage getMessage = (GetMessage) input;

                    // TODO: Check if it is in map or simply forward
                    try {
                        this.peer.sendMessageToPeer(new PeerAddress(getMessage.getIpOfRequester(),
                                                                    getMessage.getPortOfRequester()),
                                                    new PutMessage(1, "Test"));
                    } catch (FaultyPeerException e) {
                        Logging.debugLog("Can't send message to PUT-client. Full error details: " + e.getMessage(), true);
                    }
                }
            } catch (EOFException e) {
                // Simply do nothing...  Incoming message is already deserialized
            } catch (SocketException e) {
                // Close socket to client if an connection error occurs
                try {
                    this.clientSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Peer tries to join network by an existing peer in that network by sending a 'JoinMessage'-message to that
     * existing peer
     */
    void joinNetworkByExistingPeer(PeerAddress addressOfExistingPeer) {
        try (
             Socket existingPeerSocket = new Socket(addressOfExistingPeer.getIp(), addressOfExistingPeer.getPort());
             ObjectOutputStream outputStream = new ObjectOutputStream(existingPeerSocket.getOutputStream())
        ) {
            if (existingPeerSocket.isConnected())
                Logging.debugLog("Connected to peer.", false);

            outputStream.writeObject(new JoinMessage(this.ownAddress));
        } catch (SocketException e) {
            Logging.debugLog("Could not connect to peer. Full error details: " + e.getMessage(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Peer tries to send a message to another peer in the network.
     * Throws 'FaultyPeerException' if a connection to the other peer - which address is taken as argument -
     * could not be established.
     */
    void sendMessageToPeer(PeerAddress destinationPeer, Message message) throws FaultyPeerException {
        try {
            Socket socket = ConnectionHandler.establishConnectionToPeer(destinationPeer);

            if (socket != null) {
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(message);
            } else {
                Logging.debugLog(String.format("Failed to send message (type: %s) to peer " +
                                               "(IP: %s, port: %d, ID: %s)",
                                               message.getClass().toString(),
                                               destinationPeer.getIp(),
                                               destinationPeer.getPort(),
                                               destinationPeer.getHashId()), true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Peer receiving 'NextSuccessorMessage'
     */
    private void handleNextSuccessorMessage(NextSuccessorMessage message) {
        try {
            // FIXME: Waits till organize message arrives. Make it smarter...
            // This is for the new peer receiving the message first
            while (this.successor == null);

            BigInteger hashIdSuccessor = this.successor.getHashId();

            if (hashIdSuccessor.equals(message.getSenderHashId())) {
                this.setNextSuccessor(message.getNewPeerAddress());
            } else {
                sendMessageToPeer(this.successor, message); // Bounce the message to successor
            }
        } catch (FaultyPeerException e) {
            Logging.debugLog("Could not send message to successor. Sending message to next successor instead. " +
                             "Full error details: " + e.getMessage(), true);

            // Here we would reestablish network but simply sends message to next successor instead
            try {
                sendMessageToPeer(this.nextSuccessor, message);
            } catch (FaultyPeerException e1) {
                Logging.debugLog("Could not send message to next successor. The system is now nonfunctional. " +
                                 "Full error details: " + e.getMessage(), true);
            }
        }
    }

    PeerAddress getPeerAddress() {
        return this.ownAddress;
    }

    PeerAddress getSuccessor() {
        return this.successor;
    }

    void setSuccessor(PeerAddress newSuccessor) {
        this.successor = newSuccessor;

        Logging.debugLog(String.format("Updated successor to %s:%d (ID: %s)",
                                       newSuccessor.getIp(),
                                       newSuccessor.getPort(),
                                       newSuccessor.getHashId()),
                         false);
    }

    PeerAddress getNextSuccessor() {
        return this.nextSuccessor;
    }

    void setNextSuccessor(PeerAddress newNextSuccessor) {
        this.nextSuccessor = newNextSuccessor;

        Logging.debugLog(String.format("Updated next successor to %s:%d (ID: %s)",
                                       newNextSuccessor.getIp(),
                                       newNextSuccessor.getPort(),
                                       newNextSuccessor.getHashId()),
                         false);
    }
}
