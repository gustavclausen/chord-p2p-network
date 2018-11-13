package main.java.messages;

import main.java.PeerAddress;

import java.math.BigInteger;

public class LookUpMessage extends Message {
    private final int key;
    private final PeerAddress addressOfClient;
    private final BigInteger hashIdOfPeerStartedLookup;

    public LookUpMessage(int key, PeerAddress addressOfClient, BigInteger hashIdOfPeerStartedLookup) {
        this.key = key;
        this.addressOfClient = addressOfClient;
        this.hashIdOfPeerStartedLookup = hashIdOfPeerStartedLookup;
    }

    public int getKey() {
        return key;
    }

    public PeerAddress getAddressOfClient() {
        return addressOfClient;
    }

    public BigInteger getHashIdOfPeerStartedLookup() {
        return hashIdOfPeerStartedLookup;
    }
}
