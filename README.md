# SWIM Protocol 

This is an implementation of the SWIM protocol in Java, which stands for "*S*calable *W*eakly-consistent *I*nfection-style Process Group *M*embership Protocol".

// sentence about what it's used for
- A clustered system typically needs a way to keep track of which of its members are alive and which have failed
- A membership list is a list of alive members of a cluster and a copy of this list is available at each node.
- In order to create such a list, we can use a failure detector, which can continously ping a node. If the node responds to the ping we consider it alive. If it does not respond, we consider it dead.
- There's many kinds of failure detectors. A simple one is a centralized failure detector. We have one node that is responsible for pinging each 
node of the cluster to determine its status. The problem with this approach is this centralized node becomes a single point of failure - if it goes down, we lose our failure detector.
- Another type is known as all to all heartbeating. Each member of the cluster pings all other members. This ends up using a lot of network bandwidth, where each member ends up sending out O(n) messages. Since there are n members in the cluster, we get O(n^2) messages per protocol period, a typical protocol period value being 1 or 2 seconds. So this doesn't scale well at all as size of the cluster grows.

An alternative is to have each member ping one other member at random. This means that each node will be sending a constant number of messages per round (one in this case), and the total bandwidth across the network will be O(n).
This is a much light protocol, will scale better and also doesn't suffer from the single point of failure issues that you would get with centralized heartbeating.

With this kind of protocol, on expectation each member of the cluster will be pinged once per round. If a node fails, it would take n - 1 rounds until each member
has pinged the failed member, and thus determined that it has indeed failed. To propogate this information quicker and reduce the amount of time it takes for the
entire cluster to acknowledge that the ith member has failed, we can introduce a dissemination component to the failure detector. In addition to participating in the ping-ack protocol just described, a member can pass start passing along or "gossiping" information about it's own membership list.
For example, if member a has already marked member b as failed, it can pass this information along to some other nodes in the cluster. If each node gossips to two other nodes
in a round, it would take O(log n) rounds for the information to completely disseminate through the cluster. This is the I or "Infection-style" in SWIM. 


See the original paper [here](docs/SWIM.pdf).

// links to blog posts

### To build
`./gradlew build`

### To run unit tests
`./gradlew test`

### To run integration tests
`./gradlew integrationTest`
