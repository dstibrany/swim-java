package ca.davestibrany.swimjava;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SimulationTests {

    @Test
    void checkMemberListAfterJoins() throws InterruptedException, ExecutionException {
        int maxRounds = 6;
        int node1Port = 5555;
        int node2Port = 5556;
        int node3Port = 5557;
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .withJoinTime(1)
                .build(node1Port, simulationQueues);
        SimulationNode node2 = new SimulationNode.Builder()
                .withJoinTime(2)
                .build(node2Port, simulationQueues);
        SimulationNode node3 = new SimulationNode.Builder()
                .withJoinTime(3)
                .build(node3Port, simulationQueues);
        List<SimulationNode> nodes = Arrays.asList(node1, node2, node3);
        Simulation simulation = new Simulation(nodes, maxRounds);

        simulation.run();

        for (SimulationNode node : nodes) {
            assertEquals(nodes.size(), node.getMemberList().size());
        }
    }

    @Test
    void testDetectionOfSingleFailure() throws InterruptedException, ExecutionException {
        int maxRounds = 20;
        int node1Port = 5555;
        int node2Port = 5556;
        int node3Port = 5557;
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .withJoinTime(1)
                .build(node1Port, simulationQueues);
        SimulationNode node2 = new SimulationNode.Builder()
                .withJoinTime(2)
                .build(node2Port, simulationQueues);
        SimulationNode node3 = new SimulationNode.Builder()
                .withJoinTime(3)
                .withFailureTime(5)
                .build(node3Port, simulationQueues);
        List<SimulationNode> nodes = Arrays.asList(node1, node2, node3);
        Simulation simulation = new Simulation(nodes, maxRounds);

        simulation.run();

        for (SimulationNode node : nodes) {
            if (node.alive) {
                assertEquals(nodes.size() - 1, node.getMemberList().size());
            }
        }
    }

    @Test
    void testDetectionOfMultipleFailures() throws InterruptedException, ExecutionException {
        int maxRounds = 20;
        int node1Port = 5555;
        int node2Port = 5556;
        int node3Port = 5557;
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .withJoinTime(1)
                .build(node1Port, simulationQueues);
        SimulationNode node2 = new SimulationNode.Builder()
                .withJoinTime(2)
                .withFailureTime(7)
                .build(node2Port, simulationQueues);
        SimulationNode node3 = new SimulationNode.Builder()
                .withJoinTime(3)
                .withFailureTime(5)
                .build(node3Port, simulationQueues);
        List<SimulationNode> nodes = Arrays.asList(node1, node2, node3);
        Simulation simulation = new Simulation(nodes, maxRounds);

        simulation.run();

        for (SimulationNode node : nodes) {
            if (node.alive) {
                assertEquals(1, node.getMemberList().size());
            }
        }
    }

    @Test
    void testWithPacketLossAndNoFailures() throws ExecutionException, InterruptedException {
        double dropProbability = 0.05;
        int maxRounds = 30;
        int node1Port = 5555;
        int node2Port = 5556;
        int node3Port = 5557;
        SimulationQueues simulationQueues = new SimulationQueues();
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
        Simulation simulation = new Simulation(nodes, maxRounds);

        simulation.run();

        for (SimulationNode node : nodes) {
            assertEquals(nodes.size(), node.getMemberList().size());
        }
    }

    @Test
    void testWithPacketLossAndSingleFailure() throws ExecutionException, InterruptedException {
        double dropProbability = 0.1;
        int maxRounds = 30;
        int node1Port = 5555;
        int node2Port = 5556;
        int node3Port = 5557;
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .withJoinTime(1)
                .withDropProbability(dropProbability)
                .withFailureTime(13)
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
        Simulation simulation = new Simulation(nodes, maxRounds);

        simulation.run();

        for (SimulationNode node : nodes) {
            if (node.alive) {
                assertEquals(nodes.size() - 1, node.getMemberList().size());
            }
        }
    }

    @Test
    void test5NodesWithPacketLossAndNoFailures() throws ExecutionException, InterruptedException {
        double dropProbability = 0.05;
        int maxRounds = 30;
        int node1Port = 5555;
        int node2Port = 5556;
        int node3Port = 5557;
        int node4Port = 5558;
        int node5Port = 5559;
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .withJoinTime(1)
                .withDropProbability(dropProbability)
                .withSuspicionTimeout(1000)
                .build(node1Port, simulationQueues);
        SimulationNode node2 = new SimulationNode.Builder()
                .withJoinTime(3)
                .withDropProbability(dropProbability)
                .withSuspicionTimeout(1000)
                .build(node2Port, simulationQueues);
        SimulationNode node3 = new SimulationNode.Builder()
                .withJoinTime(5)
                .withDropProbability(dropProbability)
                .withSuspicionTimeout(1000)
                .build(node3Port, simulationQueues);
        SimulationNode node4 = new SimulationNode.Builder()
                .withJoinTime(7)
                .withDropProbability(dropProbability)
                .withSuspicionTimeout(1000)
                .build(node4Port, simulationQueues);
        SimulationNode node5 = new SimulationNode.Builder()
                .withJoinTime(9)
                .withDropProbability(dropProbability)
                .withSuspicionTimeout(1000)
                .build(node5Port, simulationQueues);
        List<SimulationNode> nodes = Arrays.asList(node1, node2, node3, node4, node5);
        Simulation simulation = new Simulation(nodes, maxRounds);

        simulation.run();

        for (SimulationNode node : nodes) {
            assertEquals(nodes.size(), node.getMemberList().size());
        }
    }
}
