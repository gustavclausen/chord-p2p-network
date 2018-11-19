package main.java.clients;

import main.java.PeerAddress;
import main.java.messages.GetMessage;
import main.java.messages.PutMessage;
import main.java.utilities.Common;
import main.java.utilities.Logging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class GetClient {
    /**
     * Sends a 'GetMessage' to the peer which address is taken as argument to program.
     * Afterwards listen on the port - also taken as argument to program - for any response.
     */
    public static PutMessage get(String peerAddress, int peerPort, int ownPort, int key) {
        String ownIp = Common.getOwnIp();

        try {
            Socket requestSocket = new Socket(peerAddress, peerPort); // Socket for sending 'GetMessage' to peer
            ObjectOutputStream requestOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());

            // Send 'GetMessage' to Peer
            requestOutputStream.writeObject(new GetMessage(key, new PeerAddress(ownIp, ownPort)));

            ServerSocket listenSocket = new ServerSocket(ownPort); // Socket to listen for response to 'GetMessage'
            // Waits for incoming connection with response
            Socket responseSocket = listenSocket.accept();
            // Stop listening
            listenSocket.close();
            ObjectInputStream responseInputStream = new ObjectInputStream(responseSocket.getInputStream());

            Object input = responseInputStream.readObject();

            // Print the content of the response
            if (input instanceof PutMessage) {
                PutMessage message = (PutMessage) input;

                if (message.getValue() == null) {
                    System.out.println(String.format("Value not found. Key: %d", message.getKey()));
                } else {
                    System.out.println(String.format("Key: %d, value: %s", message.getKey(), message.getValue()));
                }

                return message;
            }
        } catch (SocketException e) {
            Logging.debugLog("Could not establish connection to given peer. Full error details: " + e.getMessage(),
                             true);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
