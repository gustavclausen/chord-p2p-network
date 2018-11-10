package main.java.messages;

import main.java.PeerAddress;

import java.io.Serializable;

public class RestructureMessage implements Serializable {
    private final RestructureType type;
    private final PeerAddress peerAddress;

    public RestructureMessage(RestructureType type, PeerAddress peerAddress) {
        this.type = type;
        this.peerAddress = peerAddress;
    }

    public RestructureType getType() {
        return type;
    }

    public PeerAddress getPeerAddress() {
        return peerAddress;
    }

    public enum RestructureType {
        NEW_SUCCESSOR,
        NEW_NEXT_SUCCESSOR
    }
}
