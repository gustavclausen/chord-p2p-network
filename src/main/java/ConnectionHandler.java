package main.java;

import main.java.exceptions.FaultyPeerException;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

class ConnectionHandler {
    /**
     * Tries to establish connection to peer which address is taken as argument.
     * If connection could not be established, then a FaultyPeerException will be thrown.
     * If connection is established, then the socket-connection is returned.
     */
    static Socket establishConnectionToPeer(PeerAddress address) throws FaultyPeerException {
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
