package main.java.messages;

import main.java.Peer;

import java.math.BigInteger;
import java.net.Socket;

public class PeerDTO {
    private final BigInteger hashId;
    private final Socket predecessorSocket;
    private final Socket nextPredecessorSocket;
    private final Socket successorSocket;
    private final Socket nextSuccessorSocket;

    public PeerDTO(Peer peer) {
        this.hashId = peer.getHashId();
        this.predecessorSocket = peer.getPredecessor();
        this.nextPredecessorSocket = peer.getNextPredecessor();
        this.successorSocket = peer.getSuccessor();
        this.nextSuccessorSocket = peer.getNextSuccessor();
    }

    public BigInteger getHashId() {
        return this.hashId;
    }

    public Socket getPredecessorSocket() {
        return this.predecessorSocket;
    }

    public Socket getNextPredecessorSocket() {
        return this.nextPredecessorSocket;
    }

    public Socket getSuccessorSocket() {
        return this.successorSocket;
    }

    public Socket getNextSuccessorSocket() {
        return this.nextSuccessorSocket;
    }
}
