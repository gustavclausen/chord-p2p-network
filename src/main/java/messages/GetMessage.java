package main.java.messages;

import main.java.PeerAddress;
import main.java.utilities.SHA1Hasher;

import java.math.BigInteger;

public class GetMessage extends Message {
    private final int key;
    private final BigInteger keyHashId;
    private final PeerAddress address;
    private BigInteger hashOfFirstReceivedPeer;

    public GetMessage(int key, PeerAddress address) {
        this.key = key;
        this.keyHashId = SHA1Hasher.hashKey(key);
        this.address = address;
    }

    public int getKey() {
        return this.key;
    }

    public BigInteger getKeyHashId() {
        return this.keyHashId;
    }

    public PeerAddress getAddressToReceiver() {
        return address;
    }

    public BigInteger getHashOfFirstReceivedPeer() {
        return this.hashOfFirstReceivedPeer;
    }

    public void setHashOfFirstReceivedPeer(BigInteger hashOfFirstReceivedPeer) {
        this.hashOfFirstReceivedPeer = hashOfFirstReceivedPeer;
    }
}
