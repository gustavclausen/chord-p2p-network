package main.java.clients;

import main.java.messages.GetMessage;
import main.java.messages.PutMessage;
import main.java.utilities.Logging;
import main.java.utilities.StartupUtils;

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
    public static void get(String[] programArguments) {
        String peerAddress = programArguments[1];
        int peerPort = StartupUtils.parseInteger(programArguments[2]);

        String ownIp = StartupUtils.getOwnIp();
        int ownPort = StartupUtils.parseInteger(programArguments[3]);

        int key = StartupUtils.parseInteger(programArguments[4]);

        try {
            ServerSocket listenSocket = new ServerSocket(ownPort); // Socket to listen for response to 'GetMessage'

            Socket requestSocket = new Socket(peerAddress, peerPort); // Socket for sending request to peer
            ObjectOutputStream requestOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());

            requestOutputStream.writeObject(new GetMessage(key, ownIp, ownPort));

            // Waits for incoming connection with response
            Socket responseSocket = listenSocket.accept();
            ObjectInputStream responseInputStream = new ObjectInputStream(responseSocket.getInputStream());

            Object input = responseInputStream.readObject();

            // Print the content of the response
            if (input instanceof PutMessage) {
                PutMessage message = (PutMessage) input;

                System.out.println(String.format("Key: %d, value: %s", message.getKey(), message.getValue()));
            }
        } catch (SocketException e) {
            Logging.debugLog("Could not connect to given peer. Full error details: " + e.getMessage(), true);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
