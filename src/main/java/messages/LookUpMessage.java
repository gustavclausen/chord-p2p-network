package main.java.messages;

import main.java.PeerAddress;
import main.java.utilities.SHA1Hasher;

import java.math.BigInteger;

public class LookUpMessage extends Message {
    private final int key;
    private final PeerAddress peerStartedSearch;
    private final PeerAddress clientAddress;

    public LookUpMessage(int key, PeerAddress clientAddress, PeerAddress peerStartedSearch) {
        this.key = key;
        this.clientAddress = clientAddress;
        this.peerStartedSearch = peerStartedSearch;
    }

    public int getKey() {
        return this.key;
    }

    public PeerAddress getPeerStartedSearch() {return peerStartedSearch; }

    public PeerAddress getClientAddress() { return clientAddress; }
}
