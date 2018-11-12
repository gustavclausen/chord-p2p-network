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

    /**
     * Print a predefined error message based on the type of the error to the console
     */
    public static void printConnectionError(ErrorType type) {
        switch (type) {
            case FAULTY_SUCCESSOR:
                debugLog("Faulty successor", true);
                break;
            case FAULTY_NEXTSUCCESSOR:
                debugLog("Faulty next successor", true);
                break;
            case FAULTY_NEWPEER:
                debugLog("Faulty new peer", true);
                break;
            default:
                break;
        }
    }

    public enum ErrorType {
        FAULTY_SUCCESSOR,
        FAULTY_NEXTSUCCESSOR,
        FAULTY_NEWPEER
    }
}
