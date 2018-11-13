package main.java.messages;

import main.java.PeerAddress;

/**
 * Message used to tell the receiver of this message to change its successor or next successor
 * to another peer given in the message
 */
public class OrganizeMessage extends Message {
    private final Type type;
    private final PeerAddress peerToUpdateTo;

    public OrganizeMessage(PeerAddress peerToUpdateTo, Type type) {
        this.peerToUpdateTo = peerToUpdateTo;
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public PeerAddress getPeerToUpdateTo() {
        return this.peerToUpdateTo;
    }

    public enum Type {
        SET_NEW_SUCCESSOR,
        SET_NEW_NEXT_SUCCESSOR
    }
}
