package main.java.clients;

import main.java.messages.PutMessage;
import main.java.utilities.Logging;
import main.java.utilities.Common;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class PutClient {
    /**
     * Sends a 'PutMessage' to the peer which address is taken as argument to program.
     * Terminates the process afterwards.
     */
    public static void put(String[] programArguments) {
        String peerAddress = programArguments[1];
        int peerPort = Common.parseInteger(programArguments[2]);

        int key = Common.parseInteger(programArguments[3]);
        String value = programArguments[4];

        try (
             Socket socket = new Socket(peerAddress, peerPort);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())
        ) {
            outputStream.writeObject(new PutMessage(key, value));
        } catch (SocketException e) {
            Logging.debugLog("Could not establish connection to given peer. Full error details: " + e.getMessage(),
                             true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
