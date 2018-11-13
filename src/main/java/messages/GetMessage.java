package main.java.messages;

import main.java.PeerAddress;
import main.java.utilities.SHA1Hasher;

import java.math.BigInteger;

public class GetMessage extends Message {
    private final int key;
    private final BigInteger keyHashId;
    private final PeerAddress addressOfGetClient;

    public GetMessage(int key, PeerAddress addressOfGetClient) {
        this.key = key;
        this.keyHashId = SHA1Hasher.hashKey(key);
        this.addressOfGetClient = addressOfGetClient;
    }

    public int getKey() {
        return this.key;
    }

    public BigInteger getKeyHashId() {
        return this.keyHashId;
    }

    public PeerAddress getAddressOfGetClient() {
        return this.addressOfGetClient;
    }
}
