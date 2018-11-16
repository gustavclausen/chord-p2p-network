package main.java.messages;

import main.java.PeerAddress;

import java.math.BigInteger;

/**
 * Message is a request for data associated the given key from a 'GetMessage'
 */
public class LookupMessage extends Message {
    private final int key;
    private final PeerAddress getClientAddress;
    private final BigInteger hashIdOfPeerStartedLookup;

    public LookupMessage(int key, PeerAddress getClientAddress, BigInteger hashIdOfPeerStartedLookup) {
        this.key = key;
        this.getClientAddress = getClientAddress;
        this.hashIdOfPeerStartedLookup = hashIdOfPeerStartedLookup;
    }

    public int getKey() {
        return key;
    }

    public PeerAddress getGetClientAddress() {
        return getClientAddress;
    }

    public BigInteger getHashIdOfPeerStartedLookup() {
        return hashIdOfPeerStartedLookup;
    }
}
