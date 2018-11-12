package main.java.utilities;

import java.sql.Timestamp;

public class Logging {
    /**
     * Simply print a message (marked as either informational or as an error) with timestamp to the console
     */
    public static void debugLog(String message, boolean error) {
        String messageToOutput = String.format("(%s) %s",
                                               new Timestamp(System.currentTimeMillis()),
                                               message);

        if (error)
            System.err.println(messageToOutput);
        else
            System.out.println(messageToOutput);
    }
}
