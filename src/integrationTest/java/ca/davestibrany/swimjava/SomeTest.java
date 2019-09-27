package ca.davestibrany.swimjava;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;


class SomeTest {

    @Test
    void xTest() throws InterruptedException, ExecutionException {
        /*
        Pass in config
        specify  # of nodes
        specify  join time
        specify failure time
        assert state
         */
        Simulation simulation = new Simulation();
        simulation.run();

        List<Simulation.SwimJavaState> nodeStates = simulation.getState();
        for (Simulation.SwimJavaState state : nodeStates) {
            System.out.println(state.getMemberList().size());
//            assertTrue(state.getMemberList().size() == 2);
        }
    }

    @Test
    void test2() throws InterruptedException, ExecutionException {
        Simulation simulation = new Simulation();
        simulation.run();
        List<Simulation.SwimJavaState> nodeStates = simulation.getState();
        for (Simulation.SwimJavaState state : nodeStates) {
            System.out.println(state.getMemberList().size());
//            assertTrue(state.getMemberList().size() == 2);
        }
    }
}
