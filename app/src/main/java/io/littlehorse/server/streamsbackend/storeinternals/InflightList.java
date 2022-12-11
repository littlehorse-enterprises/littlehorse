package io.littlehorse.server.streamsbackend.storeinternals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 * The inflight list is used in the following places:
 * - Adding items when the BackendInternalComms starts the process of claiming
 *   a Task (this is also where the reading happens)
 * - Removing items when the claim process for the Task has completed or failed
 *   (this can also be done in the BackendInternalComms).
 *
 * This class is a wrapper over the entire thing to hide implementation details.
 */
public class InflightList {

    private Map<String, IFLPQueue> queues;
    private ReadWriteLock rwl;

    public InflightList() {
        queues = new HashMap<>();
        rwl = new ReentrantReadWriteLock();
    }

    /*
     * Guarded get-or-create-if-not-exists
     */
    private IFLPQueue getQueue(String taskDefName) {
        Lock l = rwl.readLock();
        try {
            l.lock();
            IFLPQueue out = queues.get(taskDefName);
            if (out != null) return out;
        } finally {
            l.unlock();
        }

        l = rwl.writeLock();
        try {
            l.lock();
            if (!queues.containsKey(taskDefName)) {
                queues.put(taskDefName, new IFLPQueue());
            }
            return queues.get(taskDefName);
        } finally {
            l.unlock();
        }
    }

    public boolean markInFlight(String taskDefName, String nodeRunId) {
        return getQueue(taskDefName).markInFlight(nodeRunId);
    }

    public void markDone(String taskDefName, String nodeRunId) {
        getQueue(taskDefName).markDone(nodeRunId);
    }
}

/*
 * This class represents just the stuff inside one queue.
 */
class IFLPQueue {

    private Set<String> inflightNodeRunIds;
    private ReadWriteLock rwl;

    public IFLPQueue() {
        inflightNodeRunIds = new HashSet<>();
        rwl = new ReentrantReadWriteLock();
    }

    public void markDone(String nodeRunId) {
        Lock l = rwl.writeLock();
        try {
            l.lock();
            inflightNodeRunIds.remove(nodeRunId);
        } finally {
            l.unlock();
        }
    }

    public boolean markInFlight(String nodeRunId) {
        Lock l = rwl.writeLock();
        try {
            l.lock();
            return inflightNodeRunIds.add(nodeRunId);
        } finally {
            l.unlock();
        }
    }
}
