package main.java.messages;

import main.java.PeerAddress;

public class OrganizeMessage extends Message {
    private final Type type;
    private final PeerAddress peerToPointTo;

    // TODO: Describe that this message tells the receiving peer what to do
    public OrganizeMessage(PeerAddress peerToPointTo, Type type) {
        this.peerToPointTo = peerToPointTo;
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public PeerAddress getPeerToPointTo() {
        return this.peerToPointTo;
    }

    public enum Type {
        SET_NEW_SUCCESSOR,
        SET_NEW_NEXT_SUCCESSOR
    }
}
