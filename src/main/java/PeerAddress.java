package main.java;

import main.java.utilities.SHA1Hasher;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Represent the address (IP and port) of a peer and contains its hash id
 */
public class PeerAddress implements Serializable {
    private final String ip;
    private final int port;
    private final BigInteger hashId;

    public PeerAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.hashId = SHA1Hasher.hashAddress(this);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public BigInteger getHashId() {
        return hashId;
    }

    @Override
    public String toString() {
        return String.format("%s:%d (ID: %s)",
                this.getIp(),
                this.getPort(),
                this.getHashId()
        );
    }
}
