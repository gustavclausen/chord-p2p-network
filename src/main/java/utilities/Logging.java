package main.java.utilities;

import java.net.Socket;
import java.sql.Timestamp;

public class Logging {
    public static void debugLog(Socket socket, String message, boolean error) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String messageToOutput;

        if (socket != null)
            messageToOutput = String.format("(%s) %s:%d: %s",
                                            timestamp,
                                            socket.getLocalAddress().getHostAddress(),
                                            socket.getPort(),
                                            message);
        else
            messageToOutput = String.format("(%s) %s", timestamp, message);

        if (error)
            System.err.println(messageToOutput);
        else
            System.out.println(messageToOutput);
    }
}
