package main.java.messages;

import main.java.PeerAddress;

public class RequestMessage extends Message {
    private final Type type;

    public RequestMessage(PeerAddress senderPeerAddress, Type type) {
        super(senderPeerAddress);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        HASHID
    }
}
