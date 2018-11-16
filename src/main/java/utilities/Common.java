package main.java.utilities;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Common {
    public static int parseInteger(String portAsString) {
        try {
            return Integer.parseInt(portAsString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("The given value (%s) must be a integer.", portAsString));
        }
    }

    public static String getOwnIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * Determines if a given id is between a peer (denoted as "pivot") and its successor based on the hash id value of
     * the peers and the given id
     */
    public static boolean idIsBetweenPeerAndSuccessor(BigInteger pivotPeerHashId,
                                                      BigInteger examinedId,
                                                      BigInteger successorHashId) {
        /*
         * If the id of the pivot peer is lower than the examined id, and the id of its
         * successor is higher than the examined id, then the examined id is between the pivot peer
         * and its successor.
         */
        if (examinedId.compareTo(pivotPeerHashId) > 0 && successorHashId.compareTo(examinedId) > 0) {
            return true;
        }
        /*
         * Special case for the same condition as above. It is possible that the pivot peer has the
         * "highest" id in the network, and of course has the peer with the "lowest" id as its successor.
         * Furthermore, the examined id has a id greater than the pivot peer and the examined id is therefore between
         * the pivot peer and the pivot peer's successor.
         */
        else if (successorHashId.compareTo(pivotPeerHashId) < 0 && examinedId.compareTo(pivotPeerHashId) > 0) {
            return true;
        }
        /*
         * Same special case as the one above, only this time the examined id has a lower id than the successor of the
         * pivot peer (which has the "highest" id in the network). Thus, the examined id is between the pivot peer
         * with the "highest" id and its successor with the "lowest" id in the network.
         */
        else if (successorHashId.compareTo(pivotPeerHashId) < 0 && examinedId.compareTo(successorHashId) < 0) {
            return true;
        }

        return false;
    }
}
