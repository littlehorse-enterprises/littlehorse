package io.littlehorse.server.streams.taskqueue;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.TaskId;

// One instance of this class is responsible for coordinating the grpc backend for
// one specific TaskDef on one LH Server host.
@Slf4j
public class OneTaskQueue {

    private Queue<PollTaskRequestObserver> hungryClients;
    private Lock lock;

    private LinkedBlockingQueue<QueueItem> pendingTasks;
    private TaskQueueManager parent;

    @Getter
    private String taskDefName;

    @Getter
    private TenantIdModel tenantId;

    private String instanceName;
    private Date lastRehydratedTask;
    private ScheduledTaskModel lastReturnedTask;

    @Getter
    /*
     * If it is true, the queue should execute a task rehydration from store
     */
    private boolean hasMoreTasksOnDisk;

    public OneTaskQueue(String taskDefName, TaskQueueManager parent, int capacity, TenantIdModel tenantId) {
        this.taskDefName = taskDefName;
        this.tenantId = tenantId;
        this.pendingTasks = new LinkedBlockingQueue<>(capacity);
        this.hungryClients = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.parent = parent;
        instanceName = parent.getBackend().getInstanceName();
    }

    /**
     * Called when a gRPC client (and its StreamObserver) disconnect, whether due to
     * a clean
     * shutdown (onCompleted()) or connection error (onError()).
     *
     * @param observer is the TaskQueueStreamObserver for the client whose
     *                 connection is now gone.
     */
    public void onRequestDisconnected(PollTaskRequestObserver disconnectedObserver) {
        // Remove the request listener when the gRPC stream is completed (i.e.
        // graceful shutdown) or when the connection is broken.

        try {
            lock.lock();
            hungryClients.removeIf(thing -> {
                log.trace(
                        "Instance {}: Removing task queue observer for taskdef {} with" + " client id {}: {}",
                        parent.getBackend().getInstanceName(),
                        taskDefName,
                        disconnectedObserver.getClientId(),
                        disconnectedObserver);
                return thing.equals(disconnectedObserver);
            });
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called in two places: 1. In the CommandProcessorDaoImpl::scheduleTask() 2. In
     * the
     * CommandProcessor::init().
     *
     * <p>
     * Item 1) is quite self-explanatory.
     *
     * <p>
     * For Item 2), remember that the Task Queue Manager system is only in-memory.
     * Upon a restart
     * or rebalance, we need to rebuild that state. During the init() call, we
     * iterate through all
     * currently scheduled but not started tasks in the state store.
     *
     * @param scheduledTask is the ::getObjectId() for the TaskScheduleRequest
     *                        that was just
     *                        scheduled.
     * @return True if the task was successfully scheduled, or False if the queue is full.
     */
    public boolean onTaskScheduled(TaskId streamsTaskId, ScheduledTaskModel scheduledTask) {
        // There's two cases here:
        // 1. There are clients waiting for requests, in which case we know that
        // the pendingTaskIds queue/list must be empty.
        // 2. There are no clients waiting for requests. In this case, we just
        // add the task id to the taskid list.
        log.trace(
                "Instance {}: Task scheduled for wfRun {}, queue is empty? {}",
                instanceName,
                LHLibUtil.getWfRunId(scheduledTask.getSource().toProto()),
                hungryClients.isEmpty());

        PollTaskRequestObserver luckyClient = null;
        try {
            lock.lock();
            if (!hungryClients.isEmpty()) {
                // This is case 1.
                if (!pendingTasks.isEmpty()) {
                    throw new RuntimeException("Can't have pending tasks and hungry clients");
                }

                luckyClient = hungryClients.poll();
            } else {
                // case 2
                hasMoreTasksOnDisk =
                        !pendingTasks.offer(new QueueItem(streamsTaskId, scheduledTask)) || hasMoreTasksOnDisk;
                return !hasMoreTasksOnDisk;
            }
        } finally {
            lock.unlock();
        }

        // pull this outside of protected zone for performance.
        if (luckyClient != null) {
            parent.itsAMatch(scheduledTask, luckyClient);
            return true;
        }
        return hungryClients.isEmpty();
    }

    /**
     * Called when a grpc client sends a new PollTaskPb.
     *
     * @param requestObserver is the grpc StreamObserver representing the channel
     *                        that talks to the
     *                        client who made the PollTaskRequest.
     */
    public void onPollRequest(PollTaskRequestObserver requestObserver, RequestExecutionContext requestContext) {

        if (taskDefName == null) {
            taskDefName = requestObserver.getTaskDefId();
        }
        if (!taskDefName.equals(requestObserver.getTaskDefId())) {
            throw new RuntimeException("Not possible, got mismatched taskdef name");
        }

        log.trace("Instance {}: Poll request received for taskDef {}", instanceName, taskDefName);

        // There's two cases here:
        // 1. There are pending Task Id's in the queue, which means that there
        // are no "hungry clients" in the queue.
        // 2. There are no pending Taskid's in the queue, in which case we simply
        // push the request client observer thing onto the back of the
        // `hungryClients` list.
        ScheduledTaskModel nextTask = null;

        try {
            lock.lock();
            if (pendingTasks.isEmpty() && hasMoreTasksOnDisk) {
                rehydrateFromStore(requestContext.getableManager());
            }

            if (!pendingTasks.isEmpty()) {
                // This is case 1.
                if (!hungryClients.isEmpty()) {
                    throw new RuntimeException("Can't have pending tasks and hungry clients");
                }

                nextTask = pendingTasks.poll().scheduledTask();
                lastReturnedTask = nextTask;
            } else {
                // case 2
                hungryClients.add(requestObserver);
            }
        } finally {
            lock.unlock();
        }

        if (nextTask != null) {
            parent.itsAMatch(nextTask, requestObserver);
        }
    }

    /**
     * Can only be called within a lock
     */
    private void rehydrateFromStore(ReadOnlyGetableManager readOnlyGetableManager) {
        log.debug("Rehydrating");
        String startKey = Tag.getAttributeString(
                        GetableClassEnum.TASK_RUN,
                        List.of(
                                new Attribute("taskDefName", taskDefName),
                                new Attribute("status", TaskStatus.TASK_SCHEDULED.name())))
                + "/";
        String endKey = startKey + "~";
        try (LHKeyValueIterator<Tag> result = readOnlyGetableManager.tagScan(startKey, endKey)) {
            boolean queueOutOfCapacity = false;
            while (result.hasNext() && !queueOutOfCapacity) {
                Tag tag = result.next().getValue();
                String describedObjectId = tag.getDescribedObjectId();
                TaskRunIdModel taskRunId =
                        (TaskRunIdModel) TaskRunIdModel.fromString(describedObjectId, TaskRunIdModel.class);
                ScheduledTaskModel scheduledTask = readOnlyGetableManager.getScheduledTask(taskRunId);
                if (scheduledTask != null && notRehydratedYet(scheduledTask)) {
                    if (!hungryClients.isEmpty()) {
                        parent.itsAMatch(scheduledTask, hungryClients.remove());
                    } else {
                        queueOutOfCapacity = !pendingTasks.offer(new QueueItem(null, scheduledTask));
                        if (!queueOutOfCapacity) {
                            lastRehydratedTask = scheduledTask.getCreatedAt();
                        }
                    }
                }
            }
            this.hasMoreTasksOnDisk = queueOutOfCapacity;
        }
    }

    private boolean notRehydratedYet(ScheduledTaskModel scheduledTask) {
        return (lastRehydratedTask == null && !scheduledTask.getTaskRunId().equals(lastReturnedTask.getTaskRunId())
                || (!scheduledTask.getTaskRunId().equals(lastReturnedTask.getTaskRunId())
                        && scheduledTask.getCreatedAt().compareTo(lastRehydratedTask) >= 0));
    }

    public void drainPartition(TaskId partitionToDrain) {
        pendingTasks.removeIf(queueItem -> queueItem.streamsTaskId().equals(partitionToDrain));
    }

    public int size() {
        return pendingTasks.size();
    }

    private record QueueItem(TaskId streamsTaskId, ScheduledTaskModel scheduledTask) {}
}
