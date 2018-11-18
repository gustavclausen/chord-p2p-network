package main.java;

import main.java.clients.GetClient;
import main.java.clients.PutClient;
import main.java.messages.PutMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private static String OWN_IP = "localhost";

    /*
        https://learnit.itu.dk/mod/assign/view.php?id=92747

        Node starts at 1025.
        Node starts at 1026, knowing 1025.
        Client PUT(1, A) to 1025.
        Client at 2048 sends GET(1, 2048) to 1025; eventually receives PUT(1, A) at 2048 from someone.
        Client at 2049 sends GET(1, 2049) to 1026; eventually receives PUT(1, A) at 2049 from someone.
        Client at 2049 sends GET(2, 2049) to 1025; eventually receives message saying the resource is not found.
        Node C starts at 1027, knowing 1026.
        Client at 2048 sends GET(1, 2048) to 1027; eventually receives PUT(1, A) at 2048 from someone.
        PUT(2, B) to 1025.
        Node 1025 crashes.
        Client at 2048 sends GET(2, 2048) to 1026, eventually receives PUT(2, B) from someone.
    */
    @Test
    void main() {
        // Node starts at 1025
        Peer peer1025 = new Peer(new PeerAddress(OWN_IP, 1025));
        WAIT(1000);

        // Node starts at 1026, knowing 1025
        Peer peer1026 = new Peer(new PeerAddress(OWN_IP, 1026));
        peer1026.joinNetworkByExistingPeer(new PeerAddress(OWN_IP, 1025));
        WAIT(1000);

        // Client PUT(1, A) to 1025
        PutClient.put(OWN_IP, 1025, 1, "A");

        // Client at 2048 sends GET(1, 2048) to 1025; eventually receives PUT(1, A) at 2048 from someone
        assertEquals(new PutMessage(1, "A"), GET(1, 2048, 1025));

        // Client at 2049 sends GET(1, 2049) to 1026; eventually receives PUT(1, A) at 2049 from someone.
        assertEquals(new PutMessage(1, "A"), GET(1, 2049, 1026));

        // Client at 2049 sends GET(2, 2049) to 1025; eventually receives message saying the resource is not found.
        assertEquals(new PutMessage(2, null), GET(2, 2049, 1025));

        // Node C starts at 1027, knowing 1026.
        Peer peer1027 = new Peer(new PeerAddress(OWN_IP, 1027));
        peer1027.joinNetworkByExistingPeer(new PeerAddress(OWN_IP, 1026));
        WAIT(1000);

        // Client at 2048 sends GET(1, 2048) to 1027; eventually receives PUT(1, A) at 2048 from someone.
        assertEquals(new PutMessage(1, "A"), GET(1, 2048, 1027));

        // PUT(2, B) to 1025.
        PutClient.put(OWN_IP, 1025, 2, "B");

        // Node 1025 crashes.
        peer1025.stop();
        WAIT(1000);

        // Client at 2048 sends GET(2, 2048) to 1026, eventually receives PUT(2, B) from someone.
        assertEquals(new PutMessage(2, "B"), GET(2, 2048, 1026));
    }

    private static PutMessage GET(int key, int ownPort, int peerPort) {
        return GetClient.get(OWN_IP, peerPort, ownPort, key);
    }

    private static void WAIT(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}