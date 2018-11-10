package main.java;

import main.java.utilities.Logging;
import main.java.utilities.SHA1Hasher;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Peer implements Serializable {
    private final BigInteger hashId;

    private Socket succesor;
    private Socket nextSuccessor;

    Peer(String ip, int port) {
        this.hashId = SHA1Hasher.hashAddress(ip, port);

        System.out.println(String.format("Peer started on %s:%d. ID: %s",
                                         ip,
                                         port,
                                         this.hashId));

        new Listener(port).start();
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

                System.out.println(input);
            } catch (EOFException e) {
                // Do nothing...  Object is deserialized
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    void connectToOtherPeer(String ipOfPeer, int portOfPeer) {
        try (
             Socket peerSocket = new Socket(ipOfPeer, portOfPeer);
             ObjectOutputStream outputStream = new ObjectOutputStream(peerSocket.getOutputStream())
        ) {
            if (peerSocket.isConnected())
                Logging.debugLog("Connected to peer.", false);

            outputStream.writeObject("Hey");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
