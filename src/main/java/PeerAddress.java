package main.java;

import java.io.Serializable;

public class PeerAddress implements Serializable {
    private final String ip;
    private final int port;

    public PeerAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
