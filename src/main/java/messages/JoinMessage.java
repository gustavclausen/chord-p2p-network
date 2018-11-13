package main.java.messages;

import main.java.PeerAddress;

/**
 * Message containing the address of a new peer trying to join an existing network of peers
 */
public class JoinMessage extends Message {
    private final PeerAddress newPeerAddress;

    public JoinMessage(PeerAddress newPeerAddress) {
        this.newPeerAddress = newPeerAddress;
    }

    public PeerAddress getNewPeerAddress() {
        return this.newPeerAddress;
    }
}
