package main.java.messages;

import main.java.utilities.SHA1Hasher;

import java.math.BigInteger;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PutMessage that = (PutMessage) o;
        return key == that.key &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "Put(" + key + ", " + value + ')';
    }
}
