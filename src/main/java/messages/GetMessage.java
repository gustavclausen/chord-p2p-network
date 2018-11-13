package main.java.messages;

import main.java.PeerAddress;

/**
 * Message containing the key to the value that a 'GetClient' has requested.
 * It also contains the address of the 'GetClient'.
 */
public class GetMessage extends Message {
    private final int key;
    private final PeerAddress getClientAddress;

    public GetMessage(int key, PeerAddress getClientAddress) {
        this.key = key;
        this.getClientAddress = getClientAddress;
    }

    public int getKey() {
        return this.key;
    }

    public PeerAddress getGetClientAddress() {
        return this.getClientAddress;
    }
}
