package ca.davestibrany.swimjava;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


class SomeTest {

    @Test
    void xTest() throws InterruptedException, ExecutionException {
        SimulationQueues simulationQueues = new SimulationQueues();
        SimulationNode node1 = new SimulationNode.Builder()
                .build(5555, simulationQueues);
        SimulationNode node2 = new SimulationNode.Builder()
                .build(5556, simulationQueues);
        SimulationNode node3 = new SimulationNode.Builder()
                .build(5557, simulationQueues);

        Simulation simulation = new Simulation(Arrays.asList(node1, node2, node3), 10);

        simulation.run();

        List<SimulationNode> nodeStates = simulation.getState();
        for (SimulationNode state : nodeStates) {
            System.out.println(state.getMemberList().size());
//            assertTrue(state.getMemberList().size() == 2);
        }
    }

    @Test
    void test2() throws InterruptedException, ExecutionException {
//        Simulation simulation = new Simulation();
//        simulation.run();
//        List<SimulationNode> nodeStates = simulation.getState();
//        for (SimulationNode state : nodeStates) {
//            System.out.println(state.getMemberList().size());
//            assertTrue(state.getMemberList().size() == 2);
//        }
    }
}
