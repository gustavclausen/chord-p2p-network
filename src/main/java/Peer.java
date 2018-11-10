package main.java;

import main.java.messages.JoinMessage;
import main.java.utilities.Logging;
import main.java.utilities.SHA1Hasher;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

public class Peer implements Serializable {
    private final BigInteger hashId;
    private final PeerAddress address;

    private Socket succesor;
    private Socket nextSuccessor;

    Peer(PeerAddress address) {
        this.address = address;
        this.hashId = SHA1Hasher.hashAddress(this.address);

        System.out.println(String.format("Peer started on %s:%d. ID: %s",
                                         this.address.getIp(),
                                         this.address.getPort(),
                                         this.hashId));

        new Listener(this.address.getPort()).start();
    }

    private class Listener extends Thread {
        private final int port;

        private Listener(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try(ServerSocket socket = new ServerSocket(this.port)) {
                System.out.println("Listening...");

                while (true) {
                    Socket clientSocket = socket.accept();
                    new ClientHandler(clientSocket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler extends Thread {
        private final Socket clientSocket;

        private ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                ObjectInputStream inputStream = new ObjectInputStream(this.clientSocket.getInputStream());
                //ObjectOutputStream outputStream = new ObjectOutputStream(this.clientSocket.getOutputStream())
            ) {
                Object input = inputStream.readObject();

                if (input instanceof JoinMessage) {
                    PeerAddress address = ((JoinMessage) input).getNewPeerAddress();
                    System.out.println(address.getIp() + address.getPort());
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
}
