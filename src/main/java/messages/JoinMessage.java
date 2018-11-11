package main.java.messages;

import main.java.PeerAddress;
import main.java.utilities.SHA1Hasher;

import java.math.BigInteger;

public class JoinMessage extends Message {
    private final PeerAddress newPeerAddress;
    private final BigInteger newPeerHashId;

    public JoinMessage(PeerAddress newPeerAddress) {
        this.newPeerAddress = newPeerAddress;
        this.newPeerHashId = SHA1Hasher.hashAddress(this.newPeerAddress);
    }

    public PeerAddress getNewPeerAddress() {
        return this.newPeerAddress;
    }

    public BigInteger getNewPeerHashId() {
        return newPeerHashId;
    }
}
