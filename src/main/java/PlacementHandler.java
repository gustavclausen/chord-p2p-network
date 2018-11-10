package main.java;

import main.java.messages.JoinMessage;
import main.java.messages.RestructureMessage;

class PlacementHandler {
    static void placeNewPeer(Peer currentPeer, JoinMessage joinMessage) {
        // It's assumed that currentPeer is the first peer in the network
        if (currentPeer.getSuccessor() == null && currentPeer.getNextSuccessor() == null) {
            currentPeer.setSuccessor(joinMessage.getNewPeerAddress());
            currentPeer.sendMessageToSuccessor(new RestructureMessage(RestructureMessage.RestructureType.NEW_SUCCESSOR,
                                                                      currentPeer.getPeerAddress()));
        }
    }
}
