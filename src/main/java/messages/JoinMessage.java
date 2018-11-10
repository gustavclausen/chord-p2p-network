package main.java.messages;

import main.java.PeerAddress;

import java.io.Serializable;

public class JoinMessage implements Serializable {
    private final PeerAddress newPeerAddress;

    public JoinMessage(PeerAddress newPeerAddress) {
        this.newPeerAddress = newPeerAddress;
    }

    public PeerAddress getNewPeerAddress() {
        return this.newPeerAddress;
    }
}
