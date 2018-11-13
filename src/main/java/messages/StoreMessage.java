package main.java.messages;

import main.java.utilities.SHA1Hasher;

import java.math.BigInteger;

// TODO: Describe that this message tells the receiving peer what to do
public class StoreMessage extends Message {
    private final int key;
    private final BigInteger keyHashId;
    private final String value;
    private int copyNumber;

    public StoreMessage(int key, String value, int copyNumber) {
        this.key = key;
        this.keyHashId = SHA1Hasher.hashKey(this.key);
        this.value = value;
        this.copyNumber = copyNumber;
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

    public int getCopyNumber() { return copyNumber; }

    public void setCopyNumber(int n) { copyNumber = n; }
}