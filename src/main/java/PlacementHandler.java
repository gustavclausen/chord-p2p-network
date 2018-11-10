package main.java;

import main.java.messages.JoinMessage;
import main.java.messages.RequestMessage;
import main.java.messages.RestructureMessage;

import java.math.BigInteger;
import java.net.Socket;

class PlacementHandler {
    static void placeNewPeer(Peer currentPeer, JoinMessage joinMessage, Socket socketToNewPeer) {
        Socket successor = currentPeer.getSuccessor();
        Socket nextSuccessor = currentPeer.getNextSuccessor();
        BigInteger newPeerHashId = joinMessage.getHashId();

        // It's assumed that currentPeer is the first peer in the network
        if (successor == null) {
            currentPeer.setSuccessor(joinMessage.getNewPeerAddress());
            currentPeer.sendMessageToPeer(currentPeer.getSuccessor(), new RestructureMessage(currentPeer.getPeerAddress(),
                                                                      RestructureMessage.Type.NEW_SUCCESSOR));
        } else if (successor != null) {
            BigInteger successorHashId = (BigInteger) currentPeer.sendRequestToPeer(
                    new RequestMessage(currentPeer.getPeerAddress(), RequestMessage.Type.HASHID),
                    currentPeer.getSuccessor());

            if (((successorHashId.compareTo(newPeerHashId) > 0) &&
                    (currentPeer.getHashId().compareTo(newPeerHashId) < 0)) ||
                    ((successorHashId.compareTo(newPeerHashId) < 0) && (currentPeer.getHashId().compareTo(newPeerHashId) < 0))) {

                currentPeer.sendMessageToPeer(socketToNewPeer, new RestructureMessage(new PeerAddress(successor.getInetAddress().getHostAddress(), successor.getPort()), RestructureMessage.Type.NEW_SUCCESSOR));
            } else {
                currentPeer.sendMessageToPeer(successor, joinMessage);
            }
        }
    }
}
