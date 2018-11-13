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

    PeerAddress(String ip, int port) {
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

    BigInteger getHashId() {
        return hashId;
    }
}
