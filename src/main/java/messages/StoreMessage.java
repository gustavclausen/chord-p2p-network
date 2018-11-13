package main.java.messages;

/**
 * Message used to tell the receiving peer to store the data given in the message.
 * This message also keeps a counter on how many times the data has to be replicated
 * to the peer's successors.
 */
public class StoreMessage extends Message {
    private final int key;
    private final String value;
    private int remainingReplicasNeeded;

    public StoreMessage(int key, String value, int remainingReplicasNeeded) {
        this.key = key;
        this.value = value;
        this.remainingReplicasNeeded = remainingReplicasNeeded;
    }

    public int getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public int getRemainingReplicasNeeded() { return this.remainingReplicasNeeded; }

    public void setRemainingReplicasNeeded(int n) { this.remainingReplicasNeeded = n; }
}