package io.littlehorse.server.streamsbackend.taskqueue;

import io.littlehorse.server.streamsbackend.KafkaStreamsBackend;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GodzillaTaskQueueManager {

    private Map<String, TaskQueueManager> taskQueues;
    private ReadWriteLock taskQueuesLock;
    private KafkaStreamsBackend ksBackend;

    public GodzillaTaskQueueManager() {
        this.taskQueues = new HashMap<>();
        this.taskQueuesLock = new ReentrantReadWriteLock();
    }

    public void setBackend(KafkaStreamsBackend backend) {
        this.ksBackend = backend;
    }

    public void onPollRequest(TaskQueueStreamObserver listener) {
        getSubQueueManager(listener.getTaskDefName()).onPollRequest(listener);
    }

    public void onRequestDisconnected(TaskQueueStreamObserver observer) {
        getSubQueueManager(observer.getTaskDefName()).onRequestDisconnected(observer);
    }

    public void onTaskScheduled(String taskDefName, String taskScheduleRequestId) {
        getSubQueueManager(taskDefName).onTaskScheduled(taskScheduleRequestId);
    }

    public void itsAMatch(String tsrId, TaskQueueStreamObserver luckyClient) {
        ksBackend.returnTaskToClient(tsrId, luckyClient);
    }

    private TaskQueueManager getSubQueueManager(String taskDefName) {
        Lock l = null;
        try {
            l = taskQueuesLock.readLock();
            l.lock();
            TaskQueueManager tqm = taskQueues.get(taskDefName);
            if (tqm != null) {
                return tqm;
            }
        } finally {
            if (l != null) {
                l.unlock();
            }
        }

        // If we got this far then we know we need to initialize the lock forrealz.
        TaskQueueManager out = new TaskQueueManager(taskDefName, this);
        try {
            l = taskQueuesLock.writeLock();
            l.lock();
            taskQueues.put(taskDefName, out);
            return out;
        } finally {
            if (l != null) {
                l.unlock();
            }
        }
    }
}
