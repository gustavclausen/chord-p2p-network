package main.java;

import main.java.messages.*;
import main.java.utilities.Logging;
import main.java.utilities.SHA1Hasher;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

class Peer implements Serializable {
    private final BigInteger hashId;
    private final PeerAddress address;

    private Socket successor;
    private Socket nextSuccessor;

    Peer(PeerAddress address) {
        this.address = address;
        this.hashId = SHA1Hasher.hashAddress(this.address);

        System.out.println(String.format("Peer started on %s:%d. ID: %s",
                                         this.address.getIp(),
                                         this.address.getPort(),
                                         this.hashId));

        new Listener(this).start();
    }

    private class Listener extends Thread {
        private final Peer peer;

        private Listener(Peer peer) {
            this.peer = peer;
        }

        @Override
        public void run() {
            try(ServerSocket socket = new ServerSocket(this.peer.address.getPort())) {
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
                } else if (input instanceof RestructureMessage) {
                    RestructureMessage restructureMessage = (RestructureMessage) input;
                    switch (restructureMessage.getType()) {
                        case NEW_SUCCESSOR:
                            this.peer.setSuccessor(restructureMessage.getSenderPeerAddress());
                            break;
                        case NEW_NEXT_SUCCESSOR:
                            this.peer.setNextSuccessor(restructureMessage.getSenderPeerAddress());
                            break;
                        default:
                            break;
                    }
                } else if (input instanceof RequestMessage) {
                    RequestMessage requestMessage = (RequestMessage) input;

                    switch (requestMessage.getType()) {
                        case HASHID:
                            outputStream.writeObject(this.peer.hashId);
                        default:
                            break;
                    }
                } else if (input instanceof RoundTripMessage) {
                    this.peer.handleRoundTripMessage((RoundTripMessage) input);
                }
            } catch (EOFException e) {
                // Do nothing...  Object is deserialized
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

            outputStream.writeObject(new JoinMessage(this.address, this.address));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendMessageToPeer(Socket socketPeer, Message message) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socketPeer.getOutputStream());
        outputStream.writeObject(message);
    }

    // Expect a response from peer
    Object sendRequestToPeer(Socket socketPeer, RequestMessage message) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socketPeer.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(socketPeer.getInputStream());

        outputStream.writeObject(message);

        try {
            Object response;
            while ((response = inputStream.readObject()) != null) {
                return response;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void handleRoundTripMessage(RoundTripMessage input) {
        try {
            if (this.successor == null) {
                System.out.println("WTF?");
            }
            BigInteger hashIdSuccessor = (BigInteger) sendRequestToPeer(this.successor, new RequestMessage(this.address, RequestMessage.Type.HASHID));

            if (hashIdSuccessor.equals(input.getSenderHashId())) {
                this.setNextSuccessor(input.getNewPeerAddress());
            } else {
                sendMessageToPeer(successor, input);
            }
        } catch (SocketException e) {
            Logging.debugLog("Could not send message to successor. Reason: " + e.getMessage(), true);
            // TODO: Reestablish network
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    BigInteger getHashId() {
        return this.hashId;
    }

    PeerAddress getPeerAddress() {
        return this.address;
    }

    Socket getSuccessor() {
        return this.successor;
    }

    void setSuccessor(Socket socketNewSuccessor, PeerAddress peerAddress) {
        this.successor = socketNewSuccessor;

        Logging.debugLog(String.format("Updated successor to %s:%d",
                peerAddress.getIp(),
                peerAddress.getPort()),
                false);
    }

    // Method overloading
    void setSuccessor(PeerAddress peerAddress) {
        try {
            this.successor = new Socket(peerAddress.getIp(), peerAddress.getPort());

            Logging.debugLog(String.format("Updated successor to %s:%d",
                    peerAddress.getIp(),
                    peerAddress.getPort()),
                    false);
        } catch (SocketException e) {
            Logging.debugLog("Couldn't connect to peer. Reason: " + e.getMessage(), true);
            // TODO: Reestablish network
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Socket getNextSuccessor() {
        return this.nextSuccessor;
    }

    void setNextSuccessor(Socket socketNewNextSuccessor, PeerAddress peerAddress) {
        this.successor = socketNewNextSuccessor;

        Logging.debugLog(String.format("Updated next successor to %s:%d",
                peerAddress.getIp(),
                peerAddress.getPort()),
                false);
    }

    // Method overloading
    void setNextSuccessor(PeerAddress peerAddress) {
        try {
            this.nextSuccessor = new Socket(peerAddress.getIp(), peerAddress.getPort());

            Logging.debugLog(String.format("Updated next successor to %s:%d",
                    peerAddress.getIp(),
                    peerAddress.getPort()),
                    false);
        } catch (SocketException e) {
            Logging.debugLog("Couldn't connect to peer. Reason: " + e.getMessage(), true);
            // TODO: Reestablish network
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
