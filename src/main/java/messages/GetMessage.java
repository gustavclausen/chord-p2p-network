package main.java.messages;

import main.java.utilities.SHA1Hasher;

import java.math.BigInteger;

public class GetMessage extends Message {
    private final int key;
    private final BigInteger keyHashId;
    private final String ipOfRequester;
    private final int portOfRequester;
    private BigInteger hashOfFirstReceivedPeer;

    public GetMessage(int key, String ipOfRequester, int portOfRequester) {
        this.key = key;
        this.keyHashId = SHA1Hasher.hashKey(key);
        this.ipOfRequester = ipOfRequester;
        this.portOfRequester = portOfRequester;
    }

    public int getKey() {
        return this.key;
    }

    public BigInteger getKeyHashId() {
        return this.keyHashId;
    }

    public String getIpOfRequester() {
        return ipOfRequester;
    }

    public int getPortOfRequester() {
        return portOfRequester;
    }

    public BigInteger getHashOfFirstReceivedPeer() {
        return this.hashOfFirstReceivedPeer;
    }

    public void setHashOfFirstReceivedPeer(BigInteger hashOfFirstReceivedPeer) {
        this.hashOfFirstReceivedPeer = hashOfFirstReceivedPeer;
    }
}
