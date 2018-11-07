package main.java;

import main.java.messages.JoinMessage;
import main.java.messages.PeerDTO;
import main.java.utilities.Logging;
import main.java.utilities.SHA1Hasher;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Peer implements Serializable {
    private InetSocketAddress ownAddress;
    private BigInteger hashId;

    private Socket predecessor;
    private Socket nextPredecessor; // Predecessor's predecessor
    private Socket successor;
    private Socket nextSuccessor; // Successor's successor

    Peer(InetSocketAddress ownAddress) {
        this.ownAddress = ownAddress;

        try {
            this.hashId = SHA1Hasher.hashAddress(ownAddress.getHostName(), ownAddress.getPort());

            System.out.println(String.format("Peer started on %s:%d. ID: %s\n",
                                             ownAddress.getHostName(),
                                             ownAddress.getPort(),
                                             this.hashId));

            Listener listener = new Listener(this);
            listener.start();
        } catch (IOException e) {
            System.err.println(String.format("Failed to start peer. Reason: %s", e.getMessage()));
        }
    }

    static class Listener extends Thread {
        private ServerSocket socket;
        private Peer peer;

        private Listener(Peer peer) throws IOException {
            this.peer = peer;
            this.socket = new ServerSocket(this.peer.ownAddress.getPort());
        }

        @Override
        public void run() {
            while (true) try {
                Socket clientSocket = this.socket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this.peer);
                clientHandler.start();

                Logging.debugLog(clientSocket, "Incoming connection", false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class ClientHandler extends Thread {
        private Socket clientSocket;
        private Peer peer;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;

        private ClientHandler(Socket clientSocket, Peer peer) throws IOException {
            this.clientSocket = clientSocket;
            this.peer = peer;
            this.inputStream = new ObjectInputStream(this.clientSocket.getInputStream());
            this.outputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
        }

        @Override
        public void run() {
            while (true) try {
                Object message = this.inputStream.readObject();

                if (message instanceof JoinMessage) {
                    PlacementHandler.placeNewPeer(this.peer, (JoinMessage) message);
                } else if (message instanceof String) {
                    switch ((String) message) {
                        case "GETPEERINFO":
                            this.outputStream.writeObject(new PeerDTO(this.peer));
                            break;
                        default:
                            break;
                    }
                }

            } catch (EOFException e) {
                Logging.debugLog(this.clientSocket, "Client disconnected", false);
                return;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    void connectToPeer(InetSocketAddress addressOfExistingPeer) {
        Socket socketToPeer = null;
        JoinMessage message = new JoinMessage(this.ownAddress);

        try {
            socketToPeer = new Socket(addressOfExistingPeer.getHostName(),
                                      addressOfExistingPeer.getPort());

            if (socketToPeer.isConnected())
                Logging.debugLog(null, "Connected to peer", false);

            ObjectOutputStream outputStream = new ObjectOutputStream(socketToPeer.getOutputStream());
            outputStream.writeObject(message);
        } catch (IOException e) {
            Logging.debugLog(socketToPeer,
                               String.format("Failed to connect to peer. Reason: %s", e.getMessage()),
                         true);
        }
    }

    void sendMessageToPeer(Socket socketToPeer, JoinMessage message) {
        ObjectOutputStream outputStream = null;

        try {
            outputStream = new ObjectOutputStream(socketToPeer.getOutputStream());
            outputStream.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    PeerDTO requestInfoFromPeer(Socket socketToPeer) {
        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;

        try {
            outputStream = new ObjectOutputStream(socketToPeer.getOutputStream());
            inputStream = new ObjectInputStream(socketToPeer.getInputStream());

            outputStream.writeObject("GETPEERINFO");

            return (PeerDTO) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public InetSocketAddress getOwnAddress() {
        return ownAddress;
    }

    public BigInteger getHashId() {
        return this.hashId;
    }

    public Socket getPredecessor() {
        return predecessor;
    }

    public Socket getNextPredecessor() {
        return nextPredecessor;
    }

    public Socket getSuccessor() {
        return successor;
    }

    public Socket getNextSuccessor() {
        return nextSuccessor;
    }
}
