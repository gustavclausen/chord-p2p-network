package main.java.handlers;

import main.java.Peer;
import main.java.exceptions.FaultyPeerException;
import main.java.messages.*;
import main.java.utilities.Common;
import main.java.utilities.Logging;

import java.math.BigInteger;

/**
 * Helper methods for the handling of the different kind of messages that a peer can receive
 * on any incoming connection
 */
public class IncomingMessageHandler {
    private final Peer peer; // Reference to peer that this handler is attached to

    public IncomingMessageHandler(Peer peer) {
        this.peer = peer;
    }

    /*
     * When a peer receives this message, it checks the following condition:
     * if the original sender of the message is this peer's successor, then this peer
     * must set its next successor to the new peer joining the network.
     * If the peer does not live up to this condition, then it must pass on the message to its
     * successor.
     */
    public void handleSetNextSuccessorMessage(SetNextSuccessorMessage message) {
        // Waits till peer updates its successor to another peer
        while (this.peer.getSuccessor() == null);

        BigInteger hashIdSuccessor = this.peer.getSuccessor().getHashId();

        if (hashIdSuccessor.equals(message.getSenderPeerHashId())) {
            this.peer.setNextSuccessor(message.getNewPeerAddress());
        } else {
            this.peer.sendMessageToSuccessor(message); // Pass on the message to successor in the ring
        }
    }

    public void handleOrganizeMessage(OrganizeMessage message) {
        switch (message.getType()) {
            case SET_NEW_SUCCESSOR:
                this.peer.setSuccessor(message.getPeerToUpdateTo());
                break;
            case SET_NEW_NEXT_SUCCESSOR:
                this.peer.setNextSuccessor(message.getPeerToUpdateTo());
                break;
            default:
                break;
        }
    }

    public void handleStoreMessage(StoreMessage message) {
        // Store the data from message in map
        this.peer.getStoredData().put(message.getKey(), message.getValue());

        message.setRemainingReplicasNeeded(message.getRemainingReplicasNeeded() - 1);

        // If any more replicas of the data is needed, then the message is sent to the peer's successor in the ring
        if (message.getRemainingReplicasNeeded() > 0) this.peer.sendMessageToSuccessor(message);
    }

    public void handlePutMessage(PutMessage message) {
        /*
         * Since all peers stores data with a hash value of a key lower than itself,
         * the peer receiving this message checks if the hash value of the key lies
         * between itself and its successor. If that is the case, then the peer tells
         * its successor to store the data and distribute replicas of it with a 'StoreMessage'.
         * If not, the peer forwards this message to its successor in the ring.
         */
        if (Common.idIsBetweenPeerAndSuccessor(this.peer.getPeerAddress().getHashId(),
                                               message.getKeyHashId(),
                                               this.peer.getSuccessor().getHashId())) {

            this.peer.sendMessageToSuccessor(new StoreMessage(message.getKey(),
                                                              message.getValue(),
                                                              2));
        } else {
            this.peer.sendMessageToSuccessor(message); // Pass on the message to successor in the ring
        }
    }

    public void handleGetMessage(GetMessage message) {
        // Check if this peer has the requested data itself
        String valueToKey = this.peer.getStoredData().get(message.getKey());

        // If that is the case, the peer sends the key and value to the 'GetClient' requesting the data
        if (valueToKey != null) {
            try {
                this.peer.sendMessageToPeer(message.getClientAddress(), new PutMessage(message.getKey(),
                                                                                       valueToKey));
            } catch (FaultyPeerException e) {
                Logging.debugLog(String.format("Could not connect to client requesting the data (key: %d).",
                                               message.getKey()),
                                 true);
            }
        }
        /*
         * If the peer does not have the data, the peer forwards the request to its successor by sending it
         * a 'LookupMessage'
         */
        else {
            this.peer.sendMessageToSuccessor(new LookupMessage(message.getKey(),
                                                       message.getClientAddress(),
                                                       this.peer.getPeerAddress().getHashId()));
        }
    }

    public void handleLookupMessage(LookupMessage message) {
        /*
         * The peer receives its own 'LookupMessage' back.
         * This means that the request has been through all peers - around the ring - in the network, which means that
         * there is no associated value with the key given by the 'GetClient'. Therefore, it will not be forwarded.
         */
        if (message.getHashIdOfPeerStartedLookup().compareTo(this.peer.getPeerAddress().getHashId()) == 0) {
            Logging.debugLog(String.format("A value for the given key (%d) was not found.", message.getKey()), false);
            return;
        }

        // Check if this peer has the requested data
        String valueToKey = this.peer.getStoredData().get(message.getKey());

        // If that is the case, the peer sends the key and value to the 'GetClient' requesting the data
        if (valueToKey != null) {
            try {
                this.peer.sendMessageToPeer(message.getGetClientAddress(), new PutMessage(message.getKey(),
                                                                                          valueToKey));
            } catch (FaultyPeerException e) {
                Logging.debugLog(String.format("Could not connect to client requesting the data (key: %d).",
                                               message.getKey()),
                                 true);
            }
        }
        // If this peer does not have the data, the peer forwards the message (hence the request) to its successor
        else {
            this.peer.sendMessageToSuccessor(message);
        }
    }
}
