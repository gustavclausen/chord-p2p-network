package main.java;

import main.java.messages.JoinMessage;
import main.java.messages.RestructureMessage;
import main.java.utilities.Logging;
import main.java.utilities.SHA1Hasher;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

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
            try (
                ObjectInputStream inputStream = new ObjectInputStream(this.clientSocket.getInputStream());
                //ObjectOutputStream outputStream = new ObjectOutputStream(this.clientSocket.getOutputStream())
            ) {
                Object input = inputStream.readObject();

                if (input instanceof JoinMessage) {
                    PlacementHandler.placeNewPeer(this.peer, (JoinMessage) input);
                } else if (input instanceof RestructureMessage) {
                    RestructureMessage message = (RestructureMessage) input;
                    switch (message.getType()) {
                        case NEW_SUCCESSOR:
                            this.peer.setSuccessor(message.getPeerAddress());
                            break;
                    }
                }
            } catch (EOFException e) {
                // Do nothing...  Object is deserialized
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    void connectToOtherPeer(PeerAddress addressOfPeer) {
        try (
             Socket peerSocket = new Socket(addressOfPeer.getIp(), addressOfPeer.getPort());
             ObjectOutputStream outputStream = new ObjectOutputStream(peerSocket.getOutputStream())
        ) {
            if (peerSocket.isConnected())
                Logging.debugLog("Connected to peer.", false);

            outputStream.writeObject(new JoinMessage(this.address));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendMessageToSuccessor(RestructureMessage message) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(this.successor.getOutputStream())) {
            outputStream.writeObject(message);
        } catch (IOException e) {
            // TODO: Check if disconnected or non-existing here
            e.printStackTrace();
        }
    }

    void sendMessageToNextSuccessor() {

    }

    PeerAddress getPeerAddress() {
        return this.address;
    }


    public Socket getSuccessor() {
        return this.successor;
    }

    public void setSuccessor(PeerAddress addressOfNewSuccessor) {
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

    public Socket getNextSuccessor() {
        return this.nextSuccessor;
    }
}
