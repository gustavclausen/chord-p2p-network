package main.java.utilities;

import main.java.exceptions.FaultyPeerException;

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

    /**
     * Print a predefined error message based on the type of the error to the console
     */
    public static void printConnectionError(FaultyPeerException e, ErrorType type) {
        switch (type) {
            case FAULTY_SUCCESSOR:
                debugLog("Could not send message to successor. Full error details: " + e.getMessage(), true);
                break;
            case FAULTY_NEXTSUCCESSOR:
                debugLog("Could not send message to next successor. Full error details: " + e.getMessage(), true);
                break;
            case FAULTY_NEWPEER:
                debugLog("Could not send message to new peer. Full error details: " + e.getMessage(), true);
                break;
            case SYSTEM_NONFUNCTIONAL:
                debugLog("The system has detected two faulty peers, and the system is now nonfunctional.", true);
                break;
            default:
                break;
        }
    }

    public enum ErrorType {
        FAULTY_SUCCESSOR,
        FAULTY_NEXTSUCCESSOR,
        FAULTY_NEWPEER,
        SYSTEM_NONFUNCTIONAL
    }
}
