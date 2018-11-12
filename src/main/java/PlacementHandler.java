package main.java;

import main.java.exceptions.FaultyPeerException;
import main.java.messages.JoinMessage;
import main.java.messages.OrganizeMessage;
import main.java.messages.NextSuccessorMessage;
import main.java.utilities.Logging;

import java.math.BigInteger;

// TODO: Check for successor is null!
// TODO: A node can't rejoin once disconnected

/**
 * Evaluates the placement of a new peer joining the network relative to this peer
 */
class PlacementHandler {
    static void placementOfNewPeer(Peer currentPeer, JoinMessage joinMessage) {
        PeerAddress newPeerAddress = joinMessage.getNewPeerAddress();
        BigInteger newPeerHashId = joinMessage.getNewPeerHashId();

        PeerAddress currentPeerAddress = currentPeer.getPeerAddress();
        BigInteger currentPeerHashId = currentPeerAddress.getHashId();

        // Special case for second peer joining the network
        // Assume that currentPeer is the first peer in the network
        if (currentPeer.getSuccessor() == null) {
            try {
                // Current peer sends the new peer a message telling it to set its successor to current peer
                currentPeer.sendMessageToPeer(newPeerAddress,
                                              new OrganizeMessage(currentPeerAddress,
                                                                  OrganizeMessage.Type.SET_NEW_SUCCESSOR));

                currentPeer.setSuccessor(newPeerAddress); // Current peer sets its successor to the new peer
            } catch (FaultyPeerException e) {
                Logging.debugLog("Could not send message to new peer while placing it in network. The peer will not " +
                                 "be connected to the network. Full error details: " + e.getMessage(),
                                 true);
            }
        }
        else if (currentPeer.getSuccessor() != null) {
            BigInteger successorHashId = currentPeer.getSuccessor().getHashId();

            if (shouldBePlacedBetweenCurrentPeerAndSuccessor(currentPeerHashId, newPeerHashId, successorHashId)) {
                try {
                    /*
                     * Current peer sends the new peer a message telling it to set its successor to the
                     * successor of the current peer
                     */
                    currentPeer.sendMessageToPeer(newPeerAddress,
                                                  new OrganizeMessage(currentPeer.getSuccessor(),
                                                                      OrganizeMessage.Type.SET_NEW_SUCCESSOR));
                } catch (FaultyPeerException e) {
                    Logging.debugLog("Could not send message to new peer while placing it in network. The peer will " +
                                     "not be connected to the network. Full error details: " + e.getMessage(),
                                     true);
                    return;
                }

                // Special case for third peer joining the network
                if (currentPeer.getNextSuccessor() == null) {
                    try {
                        // Current peer sends its successor a message telling it to set its next successor to new peer
                        currentPeer.sendMessageToPeer(currentPeer.getSuccessor(),
                                                      new OrganizeMessage(newPeerAddress,
                                                                          OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
                    } catch (FaultyPeerException e) {
                        Logging.debugLog("Could not send message to successor. " +
                                         "Full error details: " + e.getMessage(), true);

                        currentPeer.setSuccessor(null); // Remove dead connection

                        // Now only two peers left. Current peer simply sets its successor to new peer, and sends
                        // the new peer a message telling it to set its successor to the current peer
                        try {
                            currentPeer.sendMessageToPeer(newPeerAddress,
                                                          new OrganizeMessage(currentPeerAddress,
                                                                              OrganizeMessage.Type.SET_NEW_SUCCESSOR));
                            currentPeer.setSuccessor(newPeerAddress);
                        } catch (FaultyPeerException e1) {
                            // Now current peer is the only peer left in the network
                            Logging.debugLog("Could not send message to new peer. " +
                                             "Full error details: " + e.getMessage(), true);
                        }

                        return;
                    }

                    try {
                        currentPeer.sendMessageToPeer(newPeerAddress,
                                                      new OrganizeMessage(currentPeerAddress,
                                                                          OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
                    } catch (FaultyPeerException e) {
                        Logging.debugLog("Could not send message to new peer. " +
                                         "Full error details: " + e.getMessage(), true);

                        // Clean up actions
                        try {
                            currentPeer.sendMessageToPeer(currentPeer.getSuccessor(),
                                                          new OrganizeMessage(null,
                                                                              OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
                        } catch (FaultyPeerException e1) {
                            Logging.debugLog("Could not send message to successor. The system is now nonfunctional. " +
                                             "Full error details: " + e.getMessage(), true);
                        }

                        return;
                    }

                    PeerAddress previousSuccessor = currentPeer.getSuccessor();
                    currentPeer.setSuccessor(newPeerAddress);
                    currentPeer.setNextSuccessor(previousSuccessor);
                }
                // Case for forth (and above) peer joining the network
                else if (currentPeer.getNextSuccessor() != null) {
                    try {
                        currentPeer.sendMessageToPeer(newPeerAddress,
                                                      new OrganizeMessage(currentPeer.getNextSuccessor(),
                                                                          OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
                    } catch (FaultyPeerException e) {
                        Logging.debugLog("Could not send message to new peer while placing it in network. The peer will not " +
                                         "be connected to the network. Full error details: " + e.getMessage(),
                                         true);
                    }

                    currentPeer.setNextSuccessor(currentPeer.getSuccessor());
                    currentPeer.setSuccessor(newPeerAddress);

                    try {
                        currentPeer.sendMessageToPeer(currentPeer.getSuccessor(),
                                                      new NextSuccessorMessage(currentPeer.getPeerAddress(),
                                                                               newPeerAddress));
                    } catch (FaultyPeerException e) {
                        Logging.debugLog("Could not send message to successor." +
                                "Full error details: " + e.getMessage(), true);

                        // Put back the successor. Do nothing to reestablish next successor (not a requirement to this assignment)
                        currentPeer.setSuccessor(currentPeer.getNextSuccessor());
                    }
                }
            }
            // Pass on JoinMessage to successor
            else {
                try {
                    currentPeer.sendMessageToPeer(currentPeer.getSuccessor(), joinMessage);
                } catch (FaultyPeerException e) {
                    Logging.debugLog("Could not send message to successor. Sending message to next successor instead. " +
                            "Full error details: " + e.getMessage(), true);

                    // Here we would reestablish network but simply sends message to next successor instead
                    try {
                        currentPeer.sendMessageToPeer(currentPeer.getNextSuccessor(), joinMessage);
                        currentPeer.setSuccessor(currentPeer.getNextSuccessor());
                    } catch (FaultyPeerException e1) {
                        Logging.debugLog("Could not send message to next successor. The system is now nonfunctional. " +
                                "Full error details: " + e.getMessage(), true);
                    }
                }
            }
        }
    }

    /**
     * Determines if the new peer joining the network should be placed between the current peer
     * and its successor based on the id-value of all three peers
     */
    private static boolean shouldBePlacedBetweenCurrentPeerAndSuccessor(BigInteger currentPeerHashId,
                                                                        BigInteger newPeerHashId,
                                                                        BigInteger successorHashId) {
        /*
         * The id of the current peer is lower than the id of the new peer, and the id of the current peer's
         * successor is higher than the new peer.
         */
        if (newPeerHashId.compareTo(currentPeerHashId) > 0 && successorHashId.compareTo(newPeerHashId) > 0) {
            return true;
        }
        /*
         * Same condition as before, only this time taken into account that the id of the current peer's successor
         * can be lower than the new peer's id. That is, the current peer had to be the peer with the highest id in the
         * network until the new peer with a higher id joins. The id of the peer with the highest id in the network has
         * to have a successor which id is the lowest id of the network.
         */
        else if (newPeerHashId.compareTo(currentPeerHashId) > 0 && successorHashId.compareTo(newPeerHashId) < 0) {
            return true;
        }
        /*
         * Same condition as the first, only this time taken into account that the id of the new peer can be the lowest
         * in the network. Thus, if the current peer has the lowest id in the network and receives the 'JoinMessage' as
         * the first in the network, then the last condition will evaluate to false.
         */
        else if (newPeerHashId.compareTo(currentPeerHashId) < 0 && successorHashId.compareTo(newPeerHashId) > 0 &&
                 currentPeerHashId.compareTo(successorHashId) > 0) {
            return true;
        }

        return false;
    }
}
