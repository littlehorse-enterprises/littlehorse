package io.littlehorse.server.streamsimpl.taskqueue;

import io.littlehorse.common.model.objectId.TaskDefIdModel;
import io.littlehorse.common.model.wfrun.ScheduledTaskModel;
import io.littlehorse.server.KafkaStreamsServerImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TaskQueueManager {

    private Map<String, OneTaskQueue> taskQueues;
    private ReadWriteLock taskQueuesLock;
    public KafkaStreamsServerImpl backend;

    public TaskQueueManager(KafkaStreamsServerImpl backend) {
        this.taskQueues = new HashMap<>();
        this.taskQueuesLock = new ReentrantReadWriteLock();
        this.backend = backend;
    }

    public void onPollRequest(PollTaskRequestObserver listener) {
        getSubQueue(listener.getTaskDefName()).onPollRequest(listener);
    }

    public void onRequestDisconnected(PollTaskRequestObserver observer) {
        getSubQueue(observer.getTaskDefName()).onRequestDisconnected(observer);
    }

    public void onTaskScheduled(TaskDefIdModel taskDef, ScheduledTaskModel scheduledTask) {
        getSubQueue(taskDef.getName()).onTaskScheduled(scheduledTask);
    }

    public void itsAMatch(ScheduledTaskModel scheduledTask, PollTaskRequestObserver luckyClient) {
        backend.returnTaskToClient(scheduledTask, luckyClient);
    }

    private OneTaskQueue getSubQueue(String taskDefName) {
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
