# custom-p2p-network

## Purpose
This project is a solution to the mini project dealing with peer-to-peer (P2P) networks which is part of the course, _Mobile and Distributed Systems_, taught on IT-University of Copenhagen.

**Note:** This project is purely for demonstration purposes only. It serves as an inspiration for other CS/software engineering students and other stakeholders.

## Project description
The goal of this project is to implement a distributed hash table (DHT) that allows any client to store a given key-value pair or to retrieve a stored value that is associated with a given key in the network.  
The requirements to the project are as follows:
- The processes must not scale linearly in space with the number of other processes.
- The network must be resilient to the loss of any one node. In other words, if one node crashes/leaves then the system must still be able to provide proper responses to its clients.

## The solution - a structured P2P network
### Overview
The solution is heavily inspired by the [Chord protocol](https://en.wikipedia.org/wiki/Chord_(peer-to-peer)) that arranges nodes and keys in an identifier circle that has at most 2<sup>m</sup> nodes ranging from 0 to 2<sup>m−1</sup> without the _finger table_ however.  
Each node or key is assigned a m-bit identifier using the hash algorithm SHA-1, which determines its placement in the identifier circle. For the node, the identifier is a combined hash value of the IP address and port of that node, and for the key it is simply the hash value of that key.  


In contrast to the original protocol, this solution establishes the connections between the nodes a little differently. Each node knows its successor and next successor (the successor's successor), that is the next nodes with a higher identifier in the circle in clockwise direction. A node’s next successor then acts like a backup if the successor disconnects from the network.  
When a new node joins the network by any other node, it will be correctly placed between its predecessor and successor given its identifier, while all surrounding nodes in the circle will correctly update their references to a new pair of successors. This is made possible by the nodes sending messages to each other and knowing the identifier of their two successors.
The same process occurs when a new key-value pair has to be stored in the network.  

By design, a node is responsible for storing the key-value pairs which key has a identifier that is between the identifier of the node itself and its predecessor. To ensure that the data remains in the network when a node disconnects, a node simply replicates the given key-value pair to its successor when it is inserted.  

To query a key-value pair, a node sends a request message to a node that checks if it has the key-value pair locally. If that is the case, it returns the result to the client and if not, it sends the request message to its successor. That way, the request message will eventually reach a node that has the given key-value pair if it exists in the network.  

It is important to note that this solution does not reestablish the network, in terms of updating the references to a new pair of successors for all surrounding pairs, when one peer disconnects from the network since it was not a requirement to accommodate subsequent crashes. Though, as stated in the comments in the source code, it would be possible to support this with the current solution.  
If it is assumed that the nodes go down sequentially and the references to successors for each affected node can be reconfigured in between the node failures, then it would be possible to reconfigure the network to be stable again. In terms of replicating the data, if each key-value pair at each node is marked as either replicated – that is, the node’s predecessor has replicated the key-value pair that should primarily be stored at the predecessor based on the key’s identifier to the node in question – or not, then the node – which successor has disconnected – has to replicate the key-value pairs that the node should primarily store to its new successor – namely the next successor. The new successor should then mark all its replicated key-value pairs as being not replicated, and replicate all those key-value pairs to its own successor.

### Meeting the requirements
By having
- each node connect to two successor nodes (immediate successor and the node after that),
- and storing a key-value pair at two nodes  

failure in case of a single node crashes or simply disconnects is tolerated. Since this solution use TCP, it is known if the immediate successor node has dropped out of the network while trying to send a message to that node, and if this is the case, the same message is simply sent to the peer after that.


## Space consumption
Since each node only has two outgoing connections, the space usage for connections is constant for all of average-, best- and worst-case.  
For storage of keys and values, the average case (evenly distributed data) space consumed at each node is O(k/n), where k is the number of key-value pairs stored in the network, and n is the number of nodes. The best case is zero space consumed at each node in case no keys hash to an identifier that is less than or equal to the node’s identifier but greater than the previous node’s identifier. The worst case is O(k) in case all keys hash to an identifier that is less or equal to the given node’s identifier but greater than the previous node’s identifier.


## Considerations about running time and scalability
Due to O(n) search time (worst case) for both adding new nodes, GET and PUT, the system would not perform well with a large number of nodes. Unsuccessful GET operations have particularly poor performance, since all nodes need to participate in these queries.
The next improvement would be implementing finger tables, so that – instead of each node only knowing its successor and the node after that – each node knows nodes that are logarithmically distributed around the ring. This would reduce all operations to O(log n) search time, since it would be possible to skip up to n/2 nodes when searching through the ring for the right node.