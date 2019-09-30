package ca.davestibrany.swimjava;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

class SimulationQueues {
    private final Map<Member, BlockingQueue<Message>> failureDetectorQueues = new ConcurrentHashMap<>();
    private final Map<Member, Queue<Message>> listenerQueues = new ConcurrentHashMap<>();

    Map<Member, BlockingQueue<Message>> getFailureDetectorQueues() {
        return failureDetectorQueues;
    }

    Map<Member, Queue<Message>> getListenerQueues() {
        return listenerQueues;
    }
}
