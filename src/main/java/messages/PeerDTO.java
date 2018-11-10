package main.java.messages;

import main.java.Peer;

import java.math.BigInteger;
import java.net.Socket;

public class PeerDTO {
    private final BigInteger hashId;
    private final Socket successorSocket;
    private final Socket nextSuccessorSocket;

    public PeerDTO(Peer peer) {
        this.hashId = peer.getHashId();
        this.successorSocket = peer.getSuccessor();
        this.nextSuccessorSocket = peer.getNextSuccessor();
    }

    public BigInteger getHashId() {
        return this.hashId;
    }

    public Socket getSuccessorSocket() {
        return this.successorSocket;
    }

    public Socket getNextSuccessorSocket() {
        return this.nextSuccessorSocket;
    }
}
