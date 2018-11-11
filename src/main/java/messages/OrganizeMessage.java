package main.java.messages;

import main.java.PeerAddress;

public class OrganizeMessage extends Message {
    private final Type type;
    private final PeerAddress peerPointerAddress;

    public OrganizeMessage(PeerAddress peerPointerAddress, Type type) {
        this.peerPointerAddress = peerPointerAddress;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public PeerAddress getPeerPointerAddress() {
        return peerPointerAddress;
    }

    public enum Type {
        NEW_SUCCESSOR,
        NEW_NEXT_SUCCESSOR
    }
}
