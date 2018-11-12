package main.java.utilities;

import main.java.PeerAddress;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Hasher {
    /**
     * Returns the hash-value of IP and port (combined) which are defined in the 'PeerAddress'-object taken as
     * argument
     */
    public static BigInteger hashAddress(PeerAddress address) {
        String formattedAddress = address.getIp().concat(" " + Integer.toString(address.getPort()));
        BigInteger hash = hash(formattedAddress);

        if (hash == null)
            throw new NumberFormatException("Could not hash the address: " + formattedAddress);

        return hash;
    }

    /**
     * Returns the hash-value of a single integer taken as argument
     */
    public static BigInteger hashKey(int key) {
        BigInteger hash = hash(Integer.toString(key));

        if (hash == null)
            throw new NumberFormatException("Could not hash the key: " + key);

        return hash;
    }

    /**
     * The actual hash-function (SHA-1).
     * The function produces a 160-bit hash-value, and afterwards converts the value from a hexadecimal to a decimal
     * value. This value is stored in a 'BigInteger'-object, which is able to "hold" and represent this value.
     * The advantage of representing this value in a 'BigInteger'-object is to make it possible to compare two values
     * (a "less or greater than"-comparison) which this class supports.
     *
     * Source: http://www.sha1-online.com/sha1-java/ (accessed 2018-11-06)
     */
    private static BigInteger hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashedBytes = md.digest(input.getBytes());

            StringBuffer sb = new StringBuffer();
            for (byte hashedByte : hashedBytes) {
                sb.append(Integer.toString((hashedByte & 0xff) + 0x100, 16).substring(1));
            }

            // Convert from hexadecimal to BigInteger (Precision: (2^32)^Integer.MAX_VALUE)
            return new BigInteger(sb.toString(), 16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
}
