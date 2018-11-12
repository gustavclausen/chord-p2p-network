package main.java.exceptions;

/**
 * Exception used to indicate that a connection to a given peer could not be established (e.g. it is disconnected
 * or generally faulty and therefore not receiving any incoming messages)
 */
public class FaultyPeerException extends Exception {
    public FaultyPeerException(String message) {
        super(message);
    }
}
