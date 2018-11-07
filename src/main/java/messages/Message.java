package main.java.messages;

import main.java.utilities.SHA1Hasher;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetSocketAddress;

public class Message implements Serializable {
    private Type type;
    private InetSocketAddress addressOfInstantiator;
    private BigInteger hashIdOfInstantiator;

    public Message(Type type, InetSocketAddress addressOfInstantiator) {
        this.type = type;
        this.addressOfInstantiator = addressOfInstantiator;
        this.hashIdOfInstantiator = SHA1Hasher.hashAddress(addressOfInstantiator.getHostName(),
                                                           addressOfInstantiator.getPort());
    }

    public Type getType() {
        return type;
    }

    public InetSocketAddress getAddressOfInstantiator() {
        return addressOfInstantiator;
    }

    public BigInteger getHashIdOfInstantiator() {
        return hashIdOfInstantiator;
    }

    public enum Type {
         JOIN,
         SET_PREDECESSOR
     }
}
