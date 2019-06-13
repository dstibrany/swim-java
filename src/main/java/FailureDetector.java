public class FailureDetector {
    private int protocolPeriod;
    private int subgroupSize;
    private MembershipList membershipList;

    public void start() {
        // choose random member from List
        // send member PING
        // wait for ACK
        // if no ACK before timeout, select subgroupSize members and send PING_REQ to each
        // at end of protocolPeriod, check for any ACKS
        // if no ACKS, delete member from local list
        // hand update to dissemination component.


        // LISTENER
        // IF PING -> send back ACK
        // IF PING-REQ -> PING Target -> If ACK, send back ACK to source
    }
}
