package main.java;

import main.java.messages.JoinMessage;
import main.java.messages.Message;
import main.java.messages.RequestMessage;
import main.java.messages.RestructureMessage;
import main.java.utilities.Logging;
import main.java.utilities.SHA1Hasher;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
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
                }
            } catch (EOFException e) {
                // Do nothing...  Object is deserialized
            } catch (IOException | ClassNotFoundException e) {
                // TODO: Check if disconnect
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

    void sendMessageToPeer(PeerAddress peerAddress, Message message) {
        try {
            Socket socket = new Socket(peerAddress.getIp(), peerAddress.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

            outputStream.writeObject(message);
        } catch (IOException e) {
            // TODO: Check if disconnected or non-existing here
            e.printStackTrace();
        }
    }

    // Expect a response from peer
    Object sendRequestToPeer(PeerAddress peerAddress, RequestMessage message) {
        try {
            Socket socket = new Socket(peerAddress.getIp(), peerAddress.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            outputStream.writeObject(message);

            Object response;
            while ((response = inputStream.readObject()) != null) {
                return response;
            }
        } catch (IOException | ClassNotFoundException e) {
            // TODO: Check if disconnected or non-existing here
            e.printStackTrace();
        }

        return null;
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

    void setSuccessor(PeerAddress addressOfNewSuccessor) {
        try {
            this.successor = new Socket(addressOfNewSuccessor.getIp(), addressOfNewSuccessor.getPort());

            Logging.debugLog(String.format("Updated successor to %s:%d",
                    addressOfNewSuccessor.getIp(),
                    addressOfNewSuccessor.getPort()),
                    false);
        } catch (IOException e) {
            // TODO: Check if disconnected or non-existing here
            e.printStackTrace();
        }
    }

    Socket getNextSuccessor() {
        return this.nextSuccessor;
    }

    void setNextSuccessor(PeerAddress addressOfNewNextSuccessor) {
        try {
            this.nextSuccessor = new Socket(addressOfNewNextSuccessor.getIp(), addressOfNewNextSuccessor.getPort());

            Logging.debugLog(String.format("Updated next successor to %s:%d",
                    addressOfNewNextSuccessor.getIp(),
                    addressOfNewNextSuccessor.getPort()),
                    false);
        } catch (IOException e) {
            // TODO: Check if disconnected or non-existing here
            e.printStackTrace();
        }
    }
}
