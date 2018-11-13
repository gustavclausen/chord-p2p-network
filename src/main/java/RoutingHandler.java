package main.java;

import main.java.exceptions.FaultyPeerException;
import main.java.messages.JoinMessage;
import main.java.messages.OrganizeMessage;
import main.java.messages.NextSuccessorMessage;
import main.java.utilities.Logging;

import java.math.BigInteger;

// TODO: Check for successor is null!
class RoutingHandler {
    /**
     * Evaluates and acts upon the placement of a new peer joining the network relative the peer
     * taken as argument (denoted as "current").
     *
     * IMPORTANT NOTE: A peer will not be able to rejoin the network after disconnection without
     * destroying the order of successors for the peers it rejoins.
     */
    static void placementOfNewPeer(Peer currentPeer, JoinMessage joinMessage) {
        PeerAddress newPeerAddress = joinMessage.getNewPeerAddress();
        BigInteger newPeerHashId = newPeerAddress.getHashId();

        PeerAddress currentPeerAddress = currentPeer.getPeerAddress();
        BigInteger currentPeerHashId = currentPeerAddress.getHashId();

        /*
         * Special case for second peer joining the network.
         * Assumes that a peer is the first in the network if it does not have a successor.
         */
        if (currentPeer.getSuccessor() == null) {
            try {
                // Current peer sends the new peer a message telling it to set its successor to current peer
                currentPeer.sendMessageToPeer(newPeerAddress,
                                              new OrganizeMessage(currentPeerAddress,
                                                                  OrganizeMessage.Type.SET_NEW_SUCCESSOR));

                // Current peer sets its successor to the new peer
                currentPeer.setSuccessor(newPeerAddress);
            } catch (FaultyPeerException e) {
                // The new peer will not be connected to the network
                Logging.printConnectionError(Logging.ErrorType.FAULTY_NEWPEER, e);
            }
        }
        else if (currentPeer.getSuccessor() != null) {
            if (shouldBePlacedBetweenCurrentPeerAndSuccessor(currentPeerHashId,
                                                             newPeerHashId,
                                                             currentPeer.getSuccessor().getHashId())) {
                try {
                    /*
                     * Current peer sends the new peer a message telling it to set its successor to the
                     * successor of the current peer
                     */
                    currentPeer.sendMessageToPeer(newPeerAddress,
                                                  new OrganizeMessage(currentPeer.getSuccessor(),
                                                                      OrganizeMessage.Type.SET_NEW_SUCCESSOR));
                } catch (FaultyPeerException e) {
                    // The new peer will not be connected to the network
                    Logging.printConnectionError(Logging.ErrorType.FAULTY_NEWPEER, e);

                    // Stop placement of faulty/disconnected new peer
                    return;
                }

                /*
                 * Special case for third peer joining the network.
                 * Assumes that only two peers exists in the network if the current peer does not have
                 * a next successor assigned. The current peer can be any one of these two peers.
                 */
                if (currentPeer.getNextSuccessor() == null) {
                    try {
                        // Current peer sends its successor a message telling it to set its next successor to new peer
                        currentPeer.sendMessageToPeer(currentPeer.getSuccessor(),
                                                      new OrganizeMessage(newPeerAddress,
                                                                          OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
                    } catch (FaultyPeerException e) {
                        Logging.printConnectionError(Logging.ErrorType.FAULTY_SUCCESSOR, e);

                        // Current peer removes faulty peer as successor
                        currentPeer.setSuccessor(null);

                        /*
                         * Now only two peers exists in the network.
                         * Current peer simply sets its successor to new peer, and sends the new peer a message
                         * telling it to set its successor to the current peer.
                         */
                        try {
                            currentPeer.sendMessageToPeer(newPeerAddress,
                                                          new OrganizeMessage(currentPeerAddress,
                                                                              OrganizeMessage.Type.SET_NEW_SUCCESSOR));
                            currentPeer.setSuccessor(newPeerAddress);
                        } catch (FaultyPeerException e1) {
                            // Now current peer is the only peer left in the network
                            Logging.printConnectionError(Logging.ErrorType.FAULTY_NEWPEER, e);
                        }

                        // Stop further replacement
                        return;
                    }

                    try {
                        // Current sends message to new peer telling it to set next successor to current peer
                        currentPeer.sendMessageToPeer(newPeerAddress,
                                                      new OrganizeMessage(currentPeerAddress,
                                                                          OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
                    } catch (FaultyPeerException e) {
                        Logging.printConnectionError(Logging.ErrorType.FAULTY_NEWPEER, e);

                        try {
                            /*
                             * Current peer sends message to successor telling it to remove its next successor
                             * (new peer) because it is faulty.
                             */
                            currentPeer.sendMessageToPeer(currentPeer.getSuccessor(),
                                                          new OrganizeMessage(null,
                                                                              OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
                        } catch (FaultyPeerException e1) {
                            Logging.printConnectionError(Logging.ErrorType.SYSTEM_NONFUNCTIONAL, e);
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
                        /*
                         * Current peer sends message to new peer telling it to set its next successor to the next
                         * successor of current peer.
                         */
                        currentPeer.sendMessageToPeer(newPeerAddress,
                                                      new OrganizeMessage(currentPeer.getNextSuccessor(),
                                                                          OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
                    } catch (FaultyPeerException e) {
                        Logging.printConnectionError(Logging.ErrorType.FAULTY_NEWPEER, e);

                        // Stop placement of faulty/disconnected new peer
                        return;
                    }

                    currentPeer.setNextSuccessor(currentPeer.getSuccessor());
                    currentPeer.setSuccessor(newPeerAddress);

                    try {
                        /*
                         * Current peer sends message to successor asking for the following condition:
                         * "if the peer receiving this message has current peer as successor, then this peer
                         * must set its next successor to the new peer. If the peer does not live up to this
                         * condition, then it must pass on the message to its successor".
                         */
                        currentPeer.sendMessageToPeer(currentPeer.getSuccessor(),
                                                      new NextSuccessorMessage(currentPeer.getPeerAddress(),
                                                                               newPeerAddress));
                    } catch (FaultyPeerException e) {
                        Logging.printConnectionError(Logging.ErrorType.FAULTY_SUCCESSOR, e);

                        /*
                         * Current peer sets back its old successor.
                         * Nothing is done to find a new next successor for current peer and reestablish the network
                         * since only one faulty peer per network is acceptable for this assignment.
                         */
                        currentPeer.setSuccessor(currentPeer.getNextSuccessor());
                    }
                }
            }
            /*
             * Pass on the 'JoinMessage' to successor if the new peer should not be placed between the current peer and
             * its successor.
             */
            else {
                try {
                    currentPeer.sendMessageToPeer(currentPeer.getSuccessor(), joinMessage);
                } catch (FaultyPeerException e) {
                    Logging.printConnectionError(Logging.ErrorType.FAULTY_SUCCESSOR, e);

                    try {
                        /*
                         * If the message could not be sent to the successor of current peer, then it
                         * sends it to its next successor.
                         * Afterwards, the current peer updates its successor to next successor, and does nothing
                         * to find a new next successor to reestablish the network since only one faulty peer per
                         * network is acceptable for this assignment.
                         */
                        currentPeer.sendMessageToPeer(currentPeer.getNextSuccessor(), joinMessage);
                        currentPeer.setSuccessor(currentPeer.getNextSuccessor());
                        currentPeer.setNextSuccessor(null);
                    } catch (FaultyPeerException e1) {
                        Logging.printConnectionError(Logging.ErrorType.SYSTEM_NONFUNCTIONAL, e);
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
        else if (successorHashId.compareTo(currentPeerHashId) < 0 && newPeerHashId.compareTo(currentPeerHashId) > 0) {
            return true;
        }
        /*
         * Same condition as the first, only this time taken into account that the id of the new peer can be the lowest
         * in the network. Thus, if the current peer has the lowest id in the network and receives the 'JoinMessage' as
         * the first in the network, then the last condition will evaluate to false.
         */
        else if (successorHashId.compareTo(currentPeerHashId) < 0 && newPeerHashId.compareTo(successorHashId) < 0) {
            return true;
        }

        return false;
    }
}
