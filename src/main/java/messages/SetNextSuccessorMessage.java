package main.java.messages;

import main.java.PeerAddress;
import main.java.utilities.SHA1Hasher;

import java.math.BigInteger;

/**
 * Message used to update the next successor of a peer after that a new peer has been placed
 * in the network.
 */
public class SetNextSuccessorMessage extends Message {
    private final BigInteger senderPeerHashId;
    private final PeerAddress newPeerAddress;

    public SetNextSuccessorMessage(PeerAddress senderPeerAddress, PeerAddress newPeerAddress) {
        this.senderPeerHashId = SHA1Hasher.hashAddress(senderPeerAddress);
        this.newPeerAddress = newPeerAddress;
    }

    public BigInteger getSenderPeerHashId() {
        return senderPeerHashId;
    }

    public PeerAddress getNewPeerAddress() {
        return newPeerAddress;
    }
}
