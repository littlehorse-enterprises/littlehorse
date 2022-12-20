package io.littlehorse.server.streamsimpl.taskqueue;

import io.littlehorse.server.KafkaStreamsServerImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TaskQueueManager {

    private Map<String, OneTaskQueue> taskQueues;
    private ReadWriteLock taskQueuesLock;
    private KafkaStreamsServerImpl backend;

    public TaskQueueManager(KafkaStreamsServerImpl backend) {
        this.taskQueues = new HashMap<>();
        this.taskQueuesLock = new ReentrantReadWriteLock();
        this.backend = backend;
    }

    public void onPollRequest(PollTaskRequestObserver listener) {
        getSubQueueManager(listener.getTaskDefName()).onPollRequest(listener);
    }

    public void onRequestDisconnected(PollTaskRequestObserver observer) {
        getSubQueueManager(observer.getTaskDefName()).onRequestDisconnected(observer);
    }

    public void onTaskScheduled(String taskDefName, String taskScheduleRequestId) {
        getSubQueueManager(taskDefName).onTaskScheduled(taskScheduleRequestId);
    }

    public void itsAMatch(String tsrId, PollTaskRequestObserver luckyClient) {
        backend.returnTaskToClient(tsrId, luckyClient);
    }

    private OneTaskQueue getSubQueueManager(String taskDefName) {
        Lock l = null;
        try {
            l = taskQueuesLock.readLock();
            l.lock();
            OneTaskQueue tqm = taskQueues.get(taskDefName);
            if (tqm != null) {
                return tqm;
            }
        } finally {
            if (l != null) {
                l.unlock();
            }
        }

        // If we got this far then we know we need to initialize the lock forrealz.
        OneTaskQueue out = new OneTaskQueue(taskDefName, this);
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
