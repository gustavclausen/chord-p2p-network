# chord-p2p-network

## Status
Work in progress

## Protocol in pseudocode
### Joining
**Note**: Not considering disconnected or otherwise faulty peers yet
```
ENTRY CONDITION:
NEWPEER connects to PEER

STEPS:
    if (SUCCESSOR_OF_PEER == null): // Second peer joining network
        PEER sets SUCCESSOR_OF_PEER = NEWPEER
        PEER sends message to SUCCESSOR_OF_PEER (NEWPEER): SUCCESSOR_OF_NEWPEER = PEER
    else if (SUCCESSOR_OF_PEER != null):
        PEER requests SUCCESSOR_OF_PEER: ID_SUCCESSOR_PEER
        
        if ((ID_SUCCESSOR_PEER > ID_NEWPEER && ID_PEER < ID_NEWPEER) ||
            (ID_SUCCESSOR_OF_PEER < ID_NEWPEER && ID_PEER < ID_NEWPEER) ||
            (ID_NEWPEER < ID_SUCCESSOR_OF_PEER && ID_NEWPEER < ID_PEER)):
            
            PEER sends message to NEWPEER: SUCCESSOR_OF_NEWPEER = SUCCESSOR_OF_PEER
            
            if (NEXTSUCCESSOR_OF_PEER == null): // Third peer joining network
                PEER sends message to SUCCESSOR_OF_PEER: NEXTSUCCESSOR_OF_SUCCESSOR_OF_PEER = NEWPEER
                PEER sends message to NEWPEER: NEXTSUCCESSOR_OF_NEWPEER = PEER
                PEER sets TEMPSUCCESSOR_OF_PEER = SUCCESSOR_OF_PEER
                PEER sets SUCCESSOR_OF_PEER = NEWPEER
                PEER sets NEXTSUCCESSOR_OF_PEER = TEMPSUCCESSOR_OF_PEER
            else if (NEXTSUCCESSOR_OF_PEER != null): // Fourth and above peer joining network
                PEER sends message to NEWPEER: NEXTSUCCESSOR_OF_NEWPEER = NEXTSUCCESSOR_OF_PEER
                PEER sets NEXTSUCCESSOR_OF_PEER = SUCCESSOR_OF_PEER
                PEER sets SUCCESSOR_OF_PEER = NEWPEER
                
                PEER sends message to SUCCESSOR_OF_PEER: Send message "around the ring", and if
                the peer (called X) receiving the message has PEER as SUCCESSOR_OF_X, then X sets
                NEXTSUCCESSOR_OF_X = NEWPEER
        else:
            PEER sends message to SUCCESSOR_OF_PEER: Rerun this function with SUCCESSOR_OF_PEER as PEER
```