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
        // TODO: name constants
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .withJoinTime(1)
                .build(5555, simulationQueues);
        SimulationNode node2 = new SimulationNode.Builder()
                .withJoinTime(2)
                .build(5556, simulationQueues);
        SimulationNode node3 = new SimulationNode.Builder()
                .withJoinTime(3)
                .build(5557, simulationQueues);
        List<SimulationNode> nodes = Arrays.asList(node1, node2, node3);
        Simulation simulation = new Simulation(nodes, 6);

        simulation.run();

        for (SimulationNode node : nodes) {
            assertEquals(nodes.size(), node.getMemberList().size());
        }
    }

    @Test
    void testDetectionOfSingleFailure() throws InterruptedException, ExecutionException {
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .withJoinTime(1)
                .build(5555, simulationQueues);
        SimulationNode node2 = new SimulationNode.Builder()
                .withJoinTime(2)
                .build(5556, simulationQueues);
        SimulationNode node3 = new SimulationNode.Builder()
                .withJoinTime(3)
                .withFailureTime(5)
                .build(5557, simulationQueues);
        List<SimulationNode> nodes = Arrays.asList(node1, node2, node3);
        Simulation simulation = new Simulation(nodes, 20);

        simulation.run();

        for (SimulationNode node : nodes) {
            if (node.alive) {
                assertEquals(nodes.size() - 1, node.getMemberList().size());
            }
        }
    }

    @Test
    void testDetectionOfMultipleFailures() throws InterruptedException, ExecutionException {
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .withJoinTime(1)
                .build(5555, simulationQueues);
        SimulationNode node2 = new SimulationNode.Builder()
                .withJoinTime(2)
                .withFailureTime(7)
                .build(5556, simulationQueues);
        SimulationNode node3 = new SimulationNode.Builder()
                .withJoinTime(3)
                .withFailureTime(5)
                .build(5557, simulationQueues);
        List<SimulationNode> nodes = Arrays.asList(node1, node2, node3);
        Simulation simulation = new Simulation(nodes, 20);

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
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .withJoinTime(1)
                .withDropProbability(dropProbability)
                .build(5555, simulationQueues);
        SimulationNode node2 = new SimulationNode.Builder()
                .withJoinTime(3)
                .withDropProbability(dropProbability)
                .build(5556, simulationQueues);
        SimulationNode node3 = new SimulationNode.Builder()
                .withJoinTime(8)
                .withDropProbability(dropProbability)
                .build(5557, simulationQueues);
        List<SimulationNode> nodes = Arrays.asList(node1, node2, node3);
        Simulation simulation = new Simulation(nodes, 30);

        simulation.run();

        for (SimulationNode node : nodes) {
            assertEquals(nodes.size(), node.getMemberList().size());
        }
    }

    @Test
    void testWithPacketLossAndSingleFailure() throws ExecutionException, InterruptedException {
        double dropProbability = 0.1;
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .withJoinTime(1)
                .withDropProbability(dropProbability)
                .withFailureTime(13)
                .build(5555, simulationQueues);
        SimulationNode node2 = new SimulationNode.Builder()
                .withJoinTime(3)
                .withDropProbability(dropProbability)
                .build(5556, simulationQueues);
        SimulationNode node3 = new SimulationNode.Builder()
                .withJoinTime(8)
                .withDropProbability(dropProbability)
                .build(5557, simulationQueues);
        List<SimulationNode> nodes = Arrays.asList(node1, node2, node3);
        Simulation simulation = new Simulation(nodes, 30);

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
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .withJoinTime(1)
                .withDropProbability(dropProbability)
                .withSuspicionTimeout(1000)
                .build(5555, simulationQueues);
        SimulationNode node2 = new SimulationNode.Builder()
                .withJoinTime(3)
                .withDropProbability(dropProbability)
                .withSuspicionTimeout(1000)
                .build(5556, simulationQueues);
        SimulationNode node3 = new SimulationNode.Builder()
                .withJoinTime(5)
                .withDropProbability(dropProbability)
                .withSuspicionTimeout(1000)
                .build(5557, simulationQueues);
        SimulationNode node4 = new SimulationNode.Builder()
                .withJoinTime(7)
                .withDropProbability(dropProbability)
                .withSuspicionTimeout(1000)
                .build(5558, simulationQueues);
        SimulationNode node5 = new SimulationNode.Builder()
                .withJoinTime(9)
                .withDropProbability(dropProbability)
                .withSuspicionTimeout(1000)
                .build(5559, simulationQueues);
        List<SimulationNode> nodes = Arrays.asList(node1, node2, node3, node4, node5);
        Simulation simulation = new Simulation(nodes, 30);

        simulation.run();

        for (SimulationNode node : nodes) {
            assertEquals(nodes.size(), node.getMemberList().size());
        }
    }
}
