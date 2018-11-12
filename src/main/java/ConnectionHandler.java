package main.java;

import main.java.exceptions.FaultyPeerException;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ConnectionHandler {
    static Socket establishSocketConnection(PeerAddress address) throws FaultyPeerException {
        try {
            return new Socket(address.getIp(), address.getPort());
        } catch (SocketException e) {
            throw new FaultyPeerException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
