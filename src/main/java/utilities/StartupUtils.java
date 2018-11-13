package main.java.utilities;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class StartupUtils {
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
}
