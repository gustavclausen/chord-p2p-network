package main.java;

import main.java.messages.JoinMessage;
import main.java.messages.RequestMessage;
import main.java.messages.RestructureMessage;
import main.java.messages.RoundTripMessage;
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
        BigInteger currentPeerHashId = currentPeer.getHashId();

        Socket newPeerSocket = null;
        try {
            newPeerSocket = new Socket(newPeerAddress.getIp(), newPeerAddress.getPort());
        } catch (SocketException e) {
            // Could be disconnected by bouncing around
            Logging.debugLog("Can't connect to new peer. Reason: " + e.getMessage(), true);
            // TODO: Print message saying new peer couldn't join the network
        } catch (IOException e) {
            e.printStackTrace();
        }

        // It's assumed that currentPeer is the first peer in the network
        if (currentPeer.getSuccessor() == null) {
            try {
                // Order is important here!
                currentPeer.sendMessageToPeer(newPeerSocket, new RestructureMessage(currentPeerAddress, RestructureMessage.Type.NEW_SUCCESSOR));
                currentPeer.setSuccessor(newPeerSocket, newPeerAddress);
            } catch (SocketException e) {
                Logging.debugLog("Can't connect to new peer. Reason: " + e.getMessage(), true);
                // TODO: Print message saying new peer couldn't join the network
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (currentPeer.getSuccessor() != null) {
            Socket successor = currentPeer.getSuccessor();

            BigInteger successorHashId = null;
            try {
                // FIXME: Why is it necessary to create new socket???
                successorHashId = (BigInteger) currentPeer.sendRequestToPeer(new Socket(successor.getInetAddress().getHostAddress(), successor.getPort()),
                        new RequestMessage(currentPeerAddress, RequestMessage.Type.HASHID));
            } catch (SocketException e) {
                Logging.debugLog("Could not send message to peer. Reason: " + e.getMessage(), true);
                // TODO: Reestablish network
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert successorHashId != null;
            if ((successorHashId.compareTo(newPeerHashId) > 0 && currentPeerHashId.compareTo(newPeerHashId) < 0) ||
                    (successorHashId.compareTo(newPeerHashId) < 0 && currentPeerHashId.compareTo(newPeerHashId) < 0) ||
                    newPeerHashId.compareTo(successorHashId) < 0 && newPeerHashId.compareTo(currentPeerHashId) < 0) {
                // Make it look like successor send the message
                try {
                    currentPeer.sendMessageToPeer(newPeerSocket, new RestructureMessage(new PeerAddress(successor.getInetAddress().getHostAddress(),
                            successor.getPort()), RestructureMessage.Type.NEW_SUCCESSOR));
                } catch (SocketException e) {
                    Logging.debugLog("Can't send message to new peer. Reason: " + e.getMessage(), true);
                    // TODO: Print message saying new peer couldn't join the network
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Third peer joining the network
                if (currentPeer.getNextSuccessor() == null) {
                    // Make it look like newpeer send the message
                    try {
                        currentPeer.sendMessageToPeer(new Socket(successor.getInetAddress().getHostAddress(), successor.getPort()), new RestructureMessage(newPeerAddress, RestructureMessage.Type.NEW_NEXT_SUCCESSOR));
                    } catch (SocketException e) {
                        Logging.debugLog("Can't send message to successor. Reason: " + e.getMessage(), true);
                        // TODO: Print message saying new peer couldn't join the network
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        currentPeer.sendMessageToPeer(new Socket(newPeerSocket.getInetAddress().getHostAddress(), newPeerSocket.getPort()), new RestructureMessage(currentPeerAddress, RestructureMessage.Type.NEW_NEXT_SUCCESSOR));
                    } catch (SocketException e) {
                        Logging.debugLog("Can't connect to new peer. Reason: " + e.getMessage(), true);
                        // TODO: Print message saying new peer couldn't join the network
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Socket previousSuccessor = successor;
                    currentPeer.setSuccessor(newPeerSocket, newPeerAddress);
                    currentPeer.setNextSuccessor(new PeerAddress(previousSuccessor.getInetAddress().getHostAddress(), previousSuccessor.getPort()));
                }
                // Four and above joining
                else if (currentPeer.getNextSuccessor() != null) {
                    // Make it look like next successor sent the message
                    try {
                        currentPeer.sendMessageToPeer(new Socket(newPeerAddress.getIp(), newPeerAddress.getPort()), new RestructureMessage(new PeerAddress(currentPeer.getNextSuccessor().getInetAddress().getHostAddress(), currentPeer.getSuccessor().getPort()), RestructureMessage.Type.NEW_NEXT_SUCCESSOR));
                    } catch (SocketException e) {
                        Logging.debugLog("Can't connect to new peer. Reason: " + e.getMessage(), true);
                        // TODO: Print message saying new peer couldn't join the network
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    currentPeer.setNextSuccessor(currentPeer.getSuccessor(), new PeerAddress(currentPeer.getSuccessor().getInetAddress().getHostAddress(), currentPeer.getSuccessor().getPort()));
                    currentPeer.setSuccessor(newPeerSocket, newPeerAddress);

                    try {
                        currentPeer.sendMessageToPeer(new Socket(currentPeer.getSuccessor().getInetAddress().getHostAddress(), currentPeer.getSuccessor().getPort()),
                                new RoundTripMessage(currentPeer.getPeerAddress(), newPeerAddress));
                    } catch (SocketException e) {
                        Logging.debugLog("Can't send message to successor. Reason: " + e.getMessage(), true);
                        // TODO: Reestablish network
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    currentPeer.sendMessageToPeer(successor, new JoinMessage(currentPeerAddress, newPeerAddress));
                } catch (SocketException e) {
                    Logging.debugLog("Can't connect to peer. Reason: " + e.getMessage(), true);
                    // TODO: Reestablish network
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
