package main.java;

import main.java.exceptions.FaultyPeerException;
import main.java.messages.*;
import main.java.utilities.Logging;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

class Peer {
    private final PeerAddress address;

    private PeerAddress successor;
    private PeerAddress nextSuccessor;

    Peer(PeerAddress address) {
        this.address = address;

        System.out.println(String.format("Peer started on %s:%d. ID: %s",
                                         this.address.getIp(),
                                         this.address.getPort(),
                                         this.address.getHashId()));

        new Listener(this).start();
    }

    private class Listener extends Thread {
        private final Peer peer;

        private Listener(Peer peer) {
            this.peer = peer;
        }

        @Override
        public void run() {
            try (ServerSocket socket = new ServerSocket(this.peer.address.getPort())) {
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
        private final Peer peer;

        private ClientHandler(Socket clientSocket, Peer peer) {
            this.clientSocket = clientSocket;
            this.peer = peer;
        }

        @Override
        public void run() {
            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(this.clientSocket.getInputStream());

                Object input = inputStream.readObject();

                if (input instanceof JoinMessage) {
                    PlacementHandler.placeNewPeer(this.peer, (JoinMessage) input);
                } else if (input instanceof OrganizeMessage) {
                    OrganizeMessage organizeMessage = (OrganizeMessage) input;

                    switch (organizeMessage.getType()) {
                        case NEW_SUCCESSOR:
                            this.peer.setSuccessor(organizeMessage.getPeerPointerAddress());
                            break;
                        case NEW_NEXT_SUCCESSOR:
                            this.peer.setNextSuccessor(organizeMessage.getPeerPointerAddress());
                            break;
                        default:
                            break;
                    }
                } else if (input instanceof RequestMessage) {
                    RequestMessage requestMessage = (RequestMessage) input;

                    switch (requestMessage.getType()) {
                        case HASH_ID:
                            outputStream.writeObject(this.peer.address.getHashId());
                        default:
                            break;
                    }
                } else if (input instanceof NextSuccessorMessage) {
                    this.peer.handleNextSuccessorMessage((NextSuccessorMessage) input);
                } else if (input instanceof PutMessage) {
                    PutMessage putMessage = (PutMessage) input;

                    // TODO: Store data in network
                    System.out.println(String.format("Received PUT-message (key: %d, value: %s, ID: %s)",
                                                     putMessage.getKey(),
                                                     putMessage.getValue(),
                                                     putMessage.getKeyHashId()));
                } else if (input instanceof GetMessage) {
                    GetMessage getMessage = (GetMessage) input;

                    // TODO: Get data from network and remove this dummy
                    try {
                        this.peer.sendMessageToPeer(new PeerAddress(getMessage.getIpOfRequester(),
                                                                    getMessage.getPortOfRequester()),
                                                    new PutMessage(1, "Test"));
                    } catch (FaultyPeerException e) {
                        Logging.debugLog("Can't send message to PUT-client. Full error details: " + e.getMessage(), true);
                    }
                }
            } catch (EOFException e) {
                // Do nothing...  Object is deserialized // FIXME: Find out if this is necessary
            } catch (SocketException e) {
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

    void joinNetworkByExistingPeer(PeerAddress addressOfExistingPeer) {
        try (
             Socket existingPeerSocket = new Socket(addressOfExistingPeer.getIp(), addressOfExistingPeer.getPort());
             ObjectOutputStream outputStream = new ObjectOutputStream(existingPeerSocket.getOutputStream())
        ) {
            if (existingPeerSocket.isConnected())
                Logging.debugLog("Connected to peer.", false);

            outputStream.writeObject(new JoinMessage(this.address));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendMessageToPeer(PeerAddress destinationPeer, Message message) throws FaultyPeerException {
        try {
            Socket socket = PlacementHandler.establishSocketConnection(destinationPeer);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

            outputStream.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Expects a response from peer
    Object sendRequestToPeer(PeerAddress destinationPeer, RequestMessage message) throws FaultyPeerException {
        try {
            Socket socket = PlacementHandler.establishSocketConnection(destinationPeer);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            outputStream.writeObject(message);

            Object response;
            while ((response = inputStream.readObject()) != null) {
                return response;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void handleNextSuccessorMessage(NextSuccessorMessage message) {
        try {
            // FIXME: Waits till organize message arrives. Make it smarter...
            while (this.successor == null);

            BigInteger hashIdSuccessor = (BigInteger) sendRequestToPeer(this.successor,
                                                                        new RequestMessage(this.address,
                                                                                           RequestMessage.Type.HASH_ID));

            if (hashIdSuccessor.equals(message.getSenderHashId())) {
                this.setNextSuccessor(message.getNewPeerAddress());
            } else {
                sendMessageToPeer(this.successor, message);
            }
        } catch (FaultyPeerException e) {
            Logging.debugLog("Could not send message to successor. Reason: " + e.getMessage(), true);
            // TODO: Reestablish network
        }
    }

    PeerAddress getPeerAddress() {
        return this.address;
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
