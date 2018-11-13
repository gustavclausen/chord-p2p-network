package main.java.messages;

import main.java.utilities.SHA1Hasher;

import java.math.BigInteger;

/**
 * Message containing the data that has to be stored in the network
 */
public class PutMessage extends Message {
    private final int key;
    private final BigInteger keyHashId;
    private final String value;

    public PutMessage(int key, String value) {
        this.key = key;
        this.keyHashId = SHA1Hasher.hashKey(this.key);
        this.value = value;
    }

    public int getKey() {
        return this.key;
    }

    public BigInteger getKeyHashId() {
        return this.keyHashId;
    }

    public String getValue() {
        return this.value;
    }
}
