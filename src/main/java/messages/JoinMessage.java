package main.java.messages;

import main.java.utilities.SHA1Hasher;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetSocketAddress;

public class JoinMessage implements Serializable {
    private InetSocketAddress addressOfInstantiator;
    private BigInteger hashIdOfInstantiator;

    public JoinMessage(InetSocketAddress addressOfInstantiator) {
        this.addressOfInstantiator = addressOfInstantiator;
        this.hashIdOfInstantiator = SHA1Hasher.hashAddress(addressOfInstantiator.getHostName(),
                                                           addressOfInstantiator.getPort());
    }

    public InetSocketAddress getAddressOfInstantiator() {
        return addressOfInstantiator;
    }

    public BigInteger getHashIdOfInstantiator() {
        return hashIdOfInstantiator;
    }
}
