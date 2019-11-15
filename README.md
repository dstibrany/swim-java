# SWIM Protocol 

This is an implementation of the SWIM protocol in Java, which stands for "*S*calable *W*eakly-consistent *I*nfection-style Process Group *M*embership Protocol". 
See the [original paper](docs/SWIM.pdf).

A clustered system needs a way to keep track of which of its members are alive and which have failed. One way to do this is through a group membership list, which is a list of the live members in a cluster. A copy of this list is available at each node and is kept
synchronized in an eventually-consistent manner.

The SWIM protocol is a group membership list with two main components, a failure detector that is used to detect the alive/dead status of other members, and a dissemination component that is used to propagate this information to other nodes
in the cluster.

## Simulation Example

One of the goals of this project was to be able to swap out the UDP network layer with an emulated network layer. This enables the ability to write to run multi-node simulations quickly and in a single JVM process.

Here is an example of a simulation:

```java
void testWithPacketLossAndNoFailures() throws ExecutionException, InterruptedException {
    // C
    double dropProbability = 0.05;
    int maxRounds = 30;
    int node1Port = 5555;
    int node2Port = 5556;
    int node3Port = 5557;
    SimulationQueues simulationQueues = new SimulationQueues();

    // B
    SimulationNode node1 = new SimulationNode.Builder()
            .withJoinTime(1)
            .withDropProbability(dropProbability)
            .build(node1Port, simulationQueues);
    SimulationNode node2 = new SimulationNode.Builder()
            .withJoinTime(3)
            .withDropProbability(dropProbability)
            .build(node2Port, simulationQueues);
    SimulationNode node3 = new SimulationNode.Builder()
            .withJoinTime(8)
            .withDropProbability(dropProbability)
            .build(node3Port, simulationQueues);
    List<SimulationNode> nodes = Arrays.asList(node1, node2, node3);

    // A
    Simulation simulation = new Simulation(nodes, maxRounds);

    simulation.run();

    // D
    for (SimulationNode node : nodes) {
        assertEquals(nodes.size(), node.getMemberList().size());
    }
}
``` 

A - We create a 3 node simulation that runs the protocol 30 times for each node.

B - The nodes join the cluster on rounds 1, 3 and 8 respectively.

C - We add a 5% chance that the emulated network will drop a message, allowing us to test the robustness of the protocol.
 
D - We end the simulation by ensuring that each member has a complete copy of the list, in spite the of "network jitter" that we introduced.

## Building

##### To compile
`./gradlew build`

##### To run unit tests
`./gradlew test`

##### To run simulations
`./gradlew integrationTest`
