import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Hasher {
    public static BigInteger hashAddress(String ip, int port) {
        String formattedAddress = ip.concat(" " + Integer.toString(port));
        BigInteger hash = hash(formattedAddress);

        if (hash == null)
            throw new NumberFormatException("Could not hash the address: " + formattedAddress);

        return hash;
    }

    public static BigInteger hashKey(int key) {
        BigInteger hash = hash(Integer.toString(key));

        if (hash == null)
            throw new NumberFormatException("Could not hash the key: " + key);

        return hash;
    }

    // Source: http://www.sha1-online.com/sha1-java/ (accessed 2018-11-06)
    private static BigInteger hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashedBytes = md.digest(input.getBytes());

            StringBuffer sb = new StringBuffer();
            for (byte hashedByte : hashedBytes) {
                sb.append(Integer.toString((hashedByte & 0xff) + 0x100, 16).substring(1));
            }

            // Convert hexadecimal to BigInteger (Precision: (2^32)^Integer.MAX_VALUE)
            return new BigInteger(sb.toString(), 16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
}
