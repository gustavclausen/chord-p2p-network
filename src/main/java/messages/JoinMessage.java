package main.java.messages;

import main.java.PeerAddress;

public class JoinMessage extends Message {
    private final PeerAddress newPeerAddress;

    public JoinMessage(PeerAddress newPeerAddress) {
        this.newPeerAddress = newPeerAddress;
    }

    public PeerAddress getNewPeerAddress() {
        return this.newPeerAddress;
    }
}
