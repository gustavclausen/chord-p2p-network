package main.java;

import main.java.messages.JoinMessage;
import main.java.messages.RequestMessage;
import main.java.messages.RestructureMessage;

import java.math.BigInteger;
import java.net.Socket;

class PlacementHandler {
    static void placeNewPeer(Peer currentPeer, JoinMessage joinMessage) {
        PeerAddress newPeerAddress = joinMessage.getNewPeerAddress();
        BigInteger newPeerHashId = joinMessage.getNewPeerHashId();

        PeerAddress currentPeerAddress = currentPeer.getPeerAddress();
        BigInteger currentPeerHashId = currentPeer.getHashId();

        // It's assumed that currentPeer is the first peer in the network
        if (currentPeer.getSuccessor() == null) {
            currentPeer.setSuccessor(newPeerAddress);
            currentPeer.sendMessageToPeer(newPeerAddress, new RestructureMessage(currentPeerAddress,
                                                                                 RestructureMessage.Type.NEW_SUCCESSOR));
        } else if (currentPeer.getSuccessor() != null) {
            Socket successorSocket = currentPeer.getSuccessor();
            PeerAddress successorAddress = new PeerAddress(successorSocket.getInetAddress().getHostAddress(),
                                                            successorSocket.getPort());
            BigInteger successorHashId = (BigInteger) currentPeer.sendRequestToPeer(successorAddress,
                    new RequestMessage(currentPeerAddress, RequestMessage.Type.HASHID));

            if ((successorHashId.compareTo(newPeerHashId) > 0 && currentPeerHashId.compareTo(newPeerHashId) < 0) ||
                    (successorHashId.compareTo(newPeerHashId) < 0 && currentPeerHashId.compareTo(newPeerHashId) < 0) ||
            newPeerHashId.compareTo(successorHashId) < 0 && newPeerHashId.compareTo(currentPeerHashId) < 0) {
                // Make it look like successor send the message

                currentPeer.sendMessageToPeer(newPeerAddress, new RestructureMessage(successorAddress, RestructureMessage.Type.NEW_SUCCESSOR));

                // Third peer joining the network
                if (currentPeer.getNextSuccessor() == null) {
                    // Make it look like newpeer send the message
                    currentPeer.sendMessageToPeer(successorAddress, new RestructureMessage(newPeerAddress, RestructureMessage.Type.NEW_NEXT_SUCCESSOR));
                    currentPeer.sendMessageToPeer(newPeerAddress, new RestructureMessage(currentPeerAddress, RestructureMessage.Type.NEW_NEXT_SUCCESSOR));

                    PeerAddress previousSuccessor = successorAddress;
                    currentPeer.setSuccessor(newPeerAddress);
                    currentPeer.setNextSuccessor(previousSuccessor);
                }
            } else {
                currentPeer.sendMessageToPeer(successorAddress, new JoinMessage(currentPeerAddress, newPeerAddress));
            }
        }
    }
}
