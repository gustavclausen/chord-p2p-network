package main.java.handlers;

import main.java.Peer;
import main.java.exceptions.FaultyPeerException;
import main.java.messages.*;
import main.java.utilities.Common;
import main.java.utilities.Logging;

import java.math.BigInteger;

/*
 * Helper class for Peer receiving incoming messages
 */
public class IncomingMessageHandler {
    private final Peer peer;

    public IncomingMessageHandler(Peer peer) {
        this.peer = peer;
    }

    public void handleSetNextSuccessorMessage(SetNextSuccessorMessage message) {
        // FIXME: Waits till organize message arrives. Make it smarter...
        // This is for the new peer receiving the message first
        while (this.peer.getSuccessor() == null);

        BigInteger hashIdSuccessor = this.peer.getSuccessor().getHashId();

        if (hashIdSuccessor.equals(message.getSenderHashId())) {
            this.peer.setNextSuccessor(message.getNewPeerAddress());
        } else {
            this.peer.sendToBestPeer(message); // Bounce the message to successor
        }
    }

    public void handleOrganizeMessage(OrganizeMessage message) {
        switch (message.getType()) {
            case SET_NEW_SUCCESSOR:
                this.peer.setSuccessor(message.getPeerToPointTo());
                break;
            case SET_NEW_NEXT_SUCCESSOR:
                this.peer.setNextSuccessor(message.getPeerToPointTo());
                break;
            default:
                break;
        }
    }

    public void handleStoreMessage(StoreMessage message) {
        // Store data in map
        this.peer.getStoredData().put(message.getKey(), message.getValue());

        message.setCopyNumber(message.getCopyNumber() - 1);

        // TODO: Describe this
        if (message.getCopyNumber() > 0) this.peer.sendToBestPeer(message);
    }

    public void handlePutMessage(PutMessage message) {
        // TODO: Describe this
        if (Common.idIsBetweenPeerAndSuccessor(this.peer.getPeerAddress().getHashId(),
                message.getKeyHashId(),
                this.peer.getSuccessor().getHashId())) {
            this.peer.sendToBestPeer(new StoreMessage(message.getKey(), message.getValue(), 2));
        } else {
            this.peer.sendToBestPeer(message);
        }
    }

    public void handleGetMessage(GetMessage message) {
        // Check if peer has the requested data itself
        String valueToKey = this.peer.getStoredData().get(message.getKey());

        if (valueToKey != null) {
            try {
                this.peer.sendMessageToPeer(message.getAddressOfGetClient(), new PutMessage(message.getKey(), valueToKey));
            } catch (FaultyPeerException e) {
                // FIXME: Give proper message
            }
        } else {
            // Start lookup
            this.peer.sendToBestPeer(new LookUpMessage(message.getKey(),
                                                       message.getAddressOfGetClient(),
                                                       this.peer.getPeerAddress().getHashId()));
        }
    }

    public void handleLookUpMessage(LookUpMessage message) {
        // There has been a round-trip if this evaluates to false
        if (message.getHashIdOfPeerStartedLookup().compareTo(this.peer.getPeerAddress().getHashId()) == 0) {
            Logging.debugLog(String.format("The requested value for key %d was not found.", message.getKey()), false);
            return;
        }

        // Check peer has the requested data
        String valueToKey = this.peer.getStoredData().get(message.getKey());
        if (valueToKey != null) {
            try {
                this.peer.sendMessageToPeer(message.getAddressOfClient(), new PutMessage(message.getKey(), valueToKey));
            } catch (FaultyPeerException e) {
                // FIXME: Give proper message
            }
        } else {
            // Forward message to successor
            this.peer.sendToBestPeer(message);
        }
    }
}
