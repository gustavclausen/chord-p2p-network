package main.java.messages;

import main.java.PeerAddress;

public class RequestMessage extends Message {
    private final PeerAddress senderPeerAddress;
    private final Type type;

    public RequestMessage(PeerAddress senderPeerAddress, Type type) {
        this.senderPeerAddress = senderPeerAddress;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public PeerAddress getSenderPeerAddress() {
        return senderPeerAddress;
    }

    public enum Type {
        HASH_ID
    }
}
