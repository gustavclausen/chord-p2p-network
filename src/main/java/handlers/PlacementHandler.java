package main.java.handlers;

import main.java.Peer;
import main.java.PeerAddress;
import main.java.exceptions.FaultyPeerException;
import main.java.messages.JoinMessage;
import main.java.messages.OrganizeMessage;
import main.java.messages.SetNextSuccessorMessage;
import main.java.utilities.Common;
import main.java.utilities.Logging;

import java.math.BigInteger;

import static main.java.utilities.Logging.ErrorType.*;

/*
 * IMPORTANT NOTE: A peer will not be able to rejoin the network after disconnection without
 * destroying the order of successors for the peers it rejoins.
 */
public class PlacementHandler {
    /**
     * Evaluates and acts upon the placement of a new peer joining the network relative to the peer
     * taken as argument (denoted as "current").
     */
    public static void placeNewPeer(Peer currentPeer, JoinMessage joinMessage) {
        PeerAddress newPeerAddress = joinMessage.getNewPeerAddress();
        BigInteger newPeerHashId = newPeerAddress.getHashId();

        PeerAddress currentPeerAddress = currentPeer.getPeerAddress();
        BigInteger currentPeerHashId = currentPeerAddress.getHashId();

        /*
         * Special case for second peer joining the network.
         * Assumes that a peer is the first in the network if it does not have a reference to a successor.
         */
        if (currentPeer.getSuccessor() == null) {
            try {
                // Throws FaultyPeerException if a connection could not be establish to the new peer
                placeSecondPeerInNetwork(currentPeer, newPeerAddress);
            } catch (FaultyPeerException e) {
                // The new peer will not be connected to the network since current peer cannot connect to it
                Logging.printConnectionError(e, FAULTY_NEWPEER);
            }
        }
        else if (currentPeer.getSuccessor() != null) {
            /*
             * Determines if the new peer joining the network should be placed between the current peer
             * and its successor based on the hash id value of all three peers
             */
            if (Common.idIsBetweenPeerAndSuccessor(currentPeerHashId,
                                                   newPeerHashId,
                                                   currentPeer.getSuccessor().getHashId())) {
                try {
                    /*
                     * Current peer sends the new peer a message telling it to set its successor to the
                     * successor of the current peer.
                     * Throws FaultyPeerException if a connection could not be establish to the new peer.
                     */
                    currentPeer.sendMessageToPeer(newPeerAddress,
                                                  new OrganizeMessage(currentPeer.getSuccessor(),
                                                                      OrganizeMessage.Type.SET_NEW_SUCCESSOR));
                } catch (FaultyPeerException e) {
                    // The new peer will not be connected to the network since current peer cannot connect to it
                    Logging.printConnectionError(e, FAULTY_NEWPEER);
                    return;
                }

                /*
                 * Special case for third peer joining the network.
                 * Assumes that only two peers exists in the network if the current peer does not have
                 * a next successor assigned. The current peer can be any one of these two peers.
                 */
                if (currentPeer.getNextSuccessor() == null) {
                    placeThirdPeerInNetwork(currentPeer, newPeerAddress);
                }
                /*
                 * Case for fourth (and above) peer joining the network.
                 * Assumes that there exists at least three peers in the network if the current peer, which can be any
                 * peer, has a reference to a next successor.
                 */
                else if (currentPeer.getNextSuccessor() != null) {
                    placeFourthAndAbovePeerInNetwork(currentPeer, newPeerAddress);
                }
            }
            /*
             * Pass on the 'JoinMessage' to successor if the new peer should not be placed between the current peer and
             * its successor.
             */
            else {
                try {
                    // Throws FaultyPeerException if a connection could not be establish to successor
                    currentPeer.sendMessageToPeer(currentPeer.getSuccessor(), joinMessage);
                } catch (FaultyPeerException e) {
                    Logging.printConnectionError(e, FAULTY_SUCCESSOR);

                    /*
                     * If the message could not be sent to the successor of current peer, then it
                     * sends it to its next successor.
                     * Afterwards, the current peer updates its successor to its next successor, and does nothing
                     * to find a new next successor to reestablish the network since only one faulty peer per
                     * network is acceptable for this assignment.
                     */
                    try {
                        // Throws FaultyPeerException if a connection could not be established to the next successor
                        currentPeer.sendMessageToPeer(currentPeer.getNextSuccessor(), joinMessage);
                        currentPeer.setSuccessor(currentPeer.getNextSuccessor());
                    } catch (FaultyPeerException e1) {
                        // Two faulty peers has now been detected, and the system is now nonfunctional
                        Logging.printConnectionError(e, SYSTEM_NONFUNCTIONAL);
                    }
                }
            }
        }
    }

    // Special case for placing second peer in network
    private static void placeSecondPeerInNetwork(Peer currentPeer, PeerAddress newPeerAddress) throws FaultyPeerException {
        /*
         * Current peer sends the new peer a message telling it to set its successor to current peer.
         * Throws FaultyPeerException if connection could not be established to new peer.
         */
        currentPeer.sendMessageToPeer(newPeerAddress,
                                      new OrganizeMessage(currentPeer.getPeerAddress(),
                                                          OrganizeMessage.Type.SET_NEW_SUCCESSOR));

        // Current peer sets its successor to the new peer
        currentPeer.setSuccessor(newPeerAddress);
    }

    // Special case for placing third peer in network
    private static void placeThirdPeerInNetwork(Peer currentPeer, PeerAddress newPeerAddress) {
        try {
            /*
             * Current peer sends its successor a message telling it to set its next successor to new peer.
             * Throws FaultyPeerException if connection could not be established to successor.
             */
            currentPeer.sendMessageToPeer(currentPeer.getSuccessor(),
                                          new OrganizeMessage(newPeerAddress,
                                                              OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
        } catch (FaultyPeerException e) {
            Logging.printConnectionError(e, FAULTY_SUCCESSOR);

            // Current peer removes faulty peer as successor
            currentPeer.setSuccessor(null);

            /*
             * Now only two peers exists in the network.
             * Current peer simply sets its successor to new peer, and sends the new peer a message
             * telling it to set its successor to the current peer.
             */
            try {
                // Throws FaultyPeerException if connection could not be established to new peer
                currentPeer.sendMessageToPeer(newPeerAddress,
                                              new OrganizeMessage(currentPeer.getPeerAddress(),
                                                                  OrganizeMessage.Type.SET_NEW_SUCCESSOR));
                currentPeer.setSuccessor(newPeerAddress);
            } catch (FaultyPeerException e1) {
                // Now current peer is the only peer left in the network
                Logging.printConnectionError(e1, FAULTY_NEWPEER);
            }

            return;
        }

        try {
            /*
             * Current peer sends message to new peer telling it to set its next successor to current peer.
             * Throws FaultyPeerException if connection could not be established to new peer.
             */
            currentPeer.sendMessageToPeer(newPeerAddress,
                                          new OrganizeMessage(currentPeer.getPeerAddress(),
                                                              OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
        } catch (FaultyPeerException e) {
            Logging.printConnectionError(e, FAULTY_NEWPEER);

            try {
                /*
                 * Current peer sends message to successor telling it to remove its next successor
                 * (new peer) because it is faulty.
                 */
                currentPeer.sendMessageToPeer(currentPeer.getSuccessor(),
                                              new OrganizeMessage(null,
                                                                  OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
            } catch (FaultyPeerException e1) {
                // Two faulty peers has now been detected, and the system is now nonfunctional
                Logging.printConnectionError(e, SYSTEM_NONFUNCTIONAL);
            }

            return;
        }

        PeerAddress previousSuccessor = currentPeer.getSuccessor();
        currentPeer.setSuccessor(newPeerAddress);
        currentPeer.setNextSuccessor(previousSuccessor);
    }

    private static void placeFourthAndAbovePeerInNetwork(Peer currentPeer, PeerAddress newPeerAddress) {
        try {
            /*
             * Current peer sends message to new peer telling it to set its next successor to the next
             * successor of current peer.
             * Throws FaultyPeerException if connection could not be established to new peer.
             */
            currentPeer.sendMessageToPeer(newPeerAddress,
                                          new OrganizeMessage(currentPeer.getNextSuccessor(),
                                                              OrganizeMessage.Type.SET_NEW_NEXT_SUCCESSOR));
        } catch (FaultyPeerException e) {
            // The new peer will not be connected to the network since current peer cannot connect to it
            Logging.printConnectionError(e, FAULTY_NEWPEER);
            return;
        }

        currentPeer.setNextSuccessor(currentPeer.getSuccessor());
        currentPeer.setSuccessor(newPeerAddress);

        try {
            /*
             * Current peer sends 'SetNextSuccessorMessage' to successor asking the following condition:
             * "if the peer receiving this message has current peer as successor, then this peer
             * must set its next successor to the new peer. If the peer does not live up to this
             * condition, then it must pass on the message to its successor".
             * Throws FaultyPeerException if connection could not be established to successor.
             */
            currentPeer.sendMessageToPeer(currentPeer.getSuccessor(),
                                          new SetNextSuccessorMessage(currentPeer.getPeerAddress(),
                                                                      newPeerAddress));
        } catch (FaultyPeerException e) {
            Logging.printConnectionError(e, FAULTY_SUCCESSOR);

            /*
             * Current peer sets back its old successor.
             * Nothing is done to find a new next successor for current peer and reestablish the network
             * since only one faulty peer per network is acceptable for this assignment.
             */
            currentPeer.setSuccessor(currentPeer.getNextSuccessor());
        }
    }
}
