package main.java;

import main.java.exceptions.FaultyPeerException;
import main.java.handlers.ConnectionHandler;
import main.java.handlers.IncomingMessageHandler;
import main.java.handlers.PlacementHandler;
import main.java.messages.*;
import main.java.utilities.Logging;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import static main.java.utilities.Logging.ErrorType.SYSTEM_NONFUNCTIONAL;

/**
 * Represents a peer in the P2P network
 */
public class Peer {
    private final PeerAddress ownAddress;

    /*
     * Address of the peer laying next to this peer clockwise around the ring.
     * Defined as a successor to this peer.
     */
    private PeerAddress successor;
    private PeerAddress nextSuccessor; // Address of the successor's successor

    /*
     * Helper that handles all the different kind of messages that this peer can
     * receive on its incoming connections
     */
    private final IncomingMessageHandler incomingMessageHandler;

    // Key-value pairs that this peer is responsible for storing
    private final Map<Integer, String> storedData;

    Peer(PeerAddress ownAddress) {
        this.ownAddress = ownAddress;
        this.incomingMessageHandler = new IncomingMessageHandler(this);
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
                    PlacementHandler.placeNewPeer(this.peer, (JoinMessage) input);
                }
                else if (input instanceof OrganizeMessage) {
                    this.peer.incomingMessageHandler.handleOrganizeMessage((OrganizeMessage) input);
                }
                else if (input instanceof SetNextSuccessorMessage) {
                    this.peer.incomingMessageHandler.handleSetNextSuccessorMessage((SetNextSuccessorMessage) input);
                }
                else if (input instanceof StoreMessage) {
                    this.peer.incomingMessageHandler.handleStoreMessage((StoreMessage) input);
                }
                else if (input instanceof PutMessage) {
                    this.peer.incomingMessageHandler.handlePutMessage((PutMessage) input);
                }
                else if (input instanceof GetMessage) {
                    this.peer.incomingMessageHandler.handleGetMessage((GetMessage) input);
                }
                else if (input instanceof LookupMessage) {
                    this.peer.incomingMessageHandler.handleLookupMessage((LookupMessage) input);
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
     * Peer tries to join network by an existing peer in its network by sending a 'JoinMessage'-message to that
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
     * Peer tries to send message to successor, and if that fails it changes its successor to
     * its next successor, and sends the message to this peer.
     * Nothing is done to find a new next successor for the peer and reestablish the network
     * since only one faulty peer per network is acceptable for this assignment.
     */
    public void sendMessageToSuccessor(Message message) {
        try {
            this.sendMessageToPeer(this.successor, message);
        } catch (FaultyPeerException e) {
            try {
                this.setSuccessor(this.nextSuccessor);
                this.sendMessageToPeer(this.successor, message);
            } catch (FaultyPeerException e1) {
                // Two faulty peers has now been detected, and the system is now nonfunctional
                Logging.printConnectionError(e1, SYSTEM_NONFUNCTIONAL);
            }
        }
    }

    /**
     * Peer tries to send a message to another given peer in the network.
     * Throws 'FaultyPeerException' if a connection to the other peer - which address is taken as argument -
     * could not be established.
     */
    public void sendMessageToPeer(PeerAddress destinationPeer, Message message) throws FaultyPeerException {
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


    /************************
     ** GETTER AND SETTERS **
     ************************/
    public PeerAddress getPeerAddress() {
        return this.ownAddress;
    }

    public PeerAddress getSuccessor() {
        return this.successor;
    }

    public void setSuccessor(PeerAddress newSuccessor) {
        this.successor = newSuccessor;

        Logging.debugLog(String.format("Updated successor to %s:%d (ID: %s)",
                                       newSuccessor.getIp(),
                                       newSuccessor.getPort(),
                                       newSuccessor.getHashId()),
                         false);
    }

    public PeerAddress getNextSuccessor() {
        return this.nextSuccessor;
    }

    public void setNextSuccessor(PeerAddress newNextSuccessor) {
        this.nextSuccessor = newNextSuccessor;

        Logging.debugLog(String.format("Updated next successor to %s:%d (ID: %s)",
                                       newNextSuccessor.getIp(),
                                       newNextSuccessor.getPort(),
                                       newNextSuccessor.getHashId()),
                         false);
    }

    public Map<Integer, String> getStoredData() {
        return storedData;
    }
}
