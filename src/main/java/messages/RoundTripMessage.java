package main.java.messages;

import main.java.PeerAddress;
import main.java.utilities.SHA1Hasher;

import java.math.BigInteger;

public class RoundTripMessage extends Message {
    private final BigInteger senderHashId;
    private final PeerAddress newPeerAddress;

    public RoundTripMessage(PeerAddress senderPeerAddress, PeerAddress newPeerAddress) {
        super(senderPeerAddress);
        this.senderHashId = SHA1Hasher.hashAddress(senderPeerAddress);
        this.newPeerAddress = newPeerAddress;
    }

    public BigInteger getSenderHashId() {
        return senderHashId;
    }

    public PeerAddress getNewPeerAddress() {
        return newPeerAddress;
    }
}
