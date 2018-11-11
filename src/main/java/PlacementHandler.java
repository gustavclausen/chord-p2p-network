package main.java;

import main.java.exceptions.FaultyPeerException;
import main.java.messages.JoinMessage;
import main.java.messages.RequestMessage;
import main.java.messages.OrganizeMessage;
import main.java.messages.NextSuccessorMessage;
import main.java.utilities.Logging;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;

class PlacementHandler {
    static void placeNewPeer(Peer currentPeer, JoinMessage joinMessage) {
        PeerAddress newPeerAddress = joinMessage.getNewPeerAddress();
        BigInteger newPeerHashId = joinMessage.getNewPeerHashId();

        PeerAddress currentPeerAddress = currentPeer.getPeerAddress();
        BigInteger currentPeerHashId = currentPeerAddress.getHashId();

        // Special case for second peer joining the network
        // Assume that currentPeer is the first peer in the network
        if (currentPeer.getSuccessor() == null) {
            try {
                // Order is important
                currentPeer.sendMessageToPeer(newPeerAddress, new OrganizeMessage(currentPeerAddress,
                                                                                  OrganizeMessage.Type.NEW_SUCCESSOR));
                currentPeer.setSuccessor(newPeerAddress);
            } catch (FaultyPeerException e) {
                Logging.debugLog("Could not connect to new peer while placing it in network.\n" +
                                 "Full error details: " + e.getMessage(),
                                 true);
                // Returns
            }
        } else if (currentPeer.getSuccessor() != null) {
            BigInteger successorHashId;

            try {
                successorHashId = (BigInteger) currentPeer.sendRequestToPeer(currentPeer.getSuccessor(),
                                                                             new RequestMessage(currentPeerAddress,
                                                                                                RequestMessage.Type.HASH_ID));
            } catch (FaultyPeerException e) {
                Logging.debugLog("Could not connect to successor. Full error details: " + e.getMessage(), true);
                return; // TODO: Reorganize network
            }

            // TODO: We can safely remove this when the code above is implemented
            assert successorHashId != null;

            if ((successorHashId.compareTo(newPeerHashId) > 0 && currentPeerHashId.compareTo(newPeerHashId) < 0) ||
                (successorHashId.compareTo(newPeerHashId) < 0 && currentPeerHashId.compareTo(newPeerHashId) < 0) ||
                (newPeerHashId.compareTo(successorHashId) < 0 && newPeerHashId.compareTo(currentPeerHashId) < 0)) {

                try {
                    currentPeer.sendMessageToPeer(newPeerAddress, new OrganizeMessage(currentPeer.getSuccessor(),
                                                                                      OrganizeMessage.Type.NEW_SUCCESSOR));
                } catch (FaultyPeerException e) {
                    Logging.debugLog("Could not connect to new peer while placing it in network.\n" +
                                     "Full error details: " + e.getMessage(), true);
                    return;
                }

                // Special case for third peer joining the network
                if (currentPeer.getNextSuccessor() == null) {
                    try {
                        currentPeer.sendMessageToPeer(currentPeer.getSuccessor(),
                                                      new OrganizeMessage(newPeerAddress,
                                                                          OrganizeMessage.Type.NEW_NEXT_SUCCESSOR));
                    } catch (FaultyPeerException e) {
                        Logging.debugLog("Can't send message to successor. Reason: " + e.getMessage(), true);
                        return; // TODO: Reorganize network
                    }

                    try {
                        currentPeer.sendMessageToPeer(newPeerAddress,
                                                      new OrganizeMessage(currentPeerAddress,
                                                                          OrganizeMessage.Type.NEW_NEXT_SUCCESSOR));
                    } catch (FaultyPeerException e) {
                        Logging.debugLog("Can't connect to new peer. Reason: " + e.getMessage(), true);
                        return; // TODO: Redo the last action (if possible?)
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
                                                                          OrganizeMessage.Type.NEW_NEXT_SUCCESSOR));
                    } catch (FaultyPeerException e) {
                        Logging.debugLog("Can't connect to new peer. Reason: " + e.getMessage(), true);
                        // TODO: Redo the last actions (if possible?)
                    }

                    currentPeer.setNextSuccessor(currentPeer.getSuccessor());
                    currentPeer.setSuccessor(newPeerAddress);

                    try {
                        currentPeer.sendMessageToPeer(currentPeer.getSuccessor(),
                                                      new NextSuccessorMessage(currentPeer.getPeerAddress(),
                                                                               newPeerAddress));
                    } catch (FaultyPeerException e) {
                        Logging.debugLog("Can't send message to successor. Reason: " + e.getMessage(), true);
                        // TODO: Reestablish network
                    }
                }
            } else {
                try {
                    currentPeer.sendMessageToPeer(currentPeer.getSuccessor(), joinMessage);
                } catch (FaultyPeerException e) {
                    Logging.debugLog("Can't send message to successor. Reason: " + e.getMessage(), true);
                    // TODO: Reestablish network
                }
            }
        }
    }

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
