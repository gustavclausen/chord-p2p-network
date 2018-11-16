package main.java.messages;

import main.java.PeerAddress;

/**
 * Message containing the key to the value that a 'GetClient' has requested.
 * It also contains the address of the 'GetClient'.
 */
public class GetMessage extends Message {
    private final int key;
    private final PeerAddress clientAddress;

    public GetMessage(int key, PeerAddress clientAddress) {
        this.key = key;
        this.clientAddress = clientAddress;
    }

    public int getKey() {
        return this.key;
    }

    public PeerAddress getClientAddress() {
        return this.clientAddress;
    }
}
