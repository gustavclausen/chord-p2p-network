package main.java.messages;

import main.java.PeerAddress;

import java.io.Serializable;

public abstract class Message implements Serializable {
    private final PeerAddress senderPeerAddress;

    Message(PeerAddress senderPeerAddress) {
        this.senderPeerAddress = senderPeerAddress;
    }

    public PeerAddress getSenderPeerAddress() {
        return senderPeerAddress;
    }
}
