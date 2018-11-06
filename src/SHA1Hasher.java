import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Hasher {
    public static BigInteger hashAddress(String ip, int port) {
        return hash(ip.concat(Integer.toString(port)));
    }

    public static BigInteger hashKey(int key) {
        return hash(Integer.toString(key));
    }

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
