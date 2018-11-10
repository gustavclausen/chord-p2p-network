package main.java.messages;

import main.java.PeerAddress;
import main.java.utilities.SHA1Hasher;

import java.io.Serializable;
import java.math.BigInteger;

public class JoinMessage extends Message implements Serializable {
    private final PeerAddress newPeerAddress;
    private final BigInteger hashId;

    public JoinMessage(PeerAddress senderPeerAddress, PeerAddress newPeerAddress) {
        super(senderPeerAddress);
        this.newPeerAddress = newPeerAddress;
        this.hashId = SHA1Hasher.hashAddress(this.newPeerAddress);
    }

    public PeerAddress getNewPeerAddress() {
        return this.newPeerAddress;
    }

    public BigInteger getHashId() {
        return hashId;
    }
}
