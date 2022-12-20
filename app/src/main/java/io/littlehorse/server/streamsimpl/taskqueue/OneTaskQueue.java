package io.littlehorse.server.streamsimpl.taskqueue;

import io.littlehorse.common.util.LHUtil;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// One instance of this class is responsible for coordinating the grpc backend for
// one specific TaskDef on one LH Server host.
public class OneTaskQueue {

    private LinkedList<TaskQueueStreamObserver> hungryClients;
    private Lock lock;

    private LinkedList<String> pendingTaskIds;
    private TaskQueueManager parent;

    private String taskDefName;

    public OneTaskQueue(String taskDefName, TaskQueueManager parent) {
        this.taskDefName = taskDefName;
        this.pendingTaskIds = new LinkedList<>();
        this.hungryClients = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.parent = parent;
    }

    /**
     * Called when a gRPC client (and its StreamObserver) disconnect, whether due
     * to a clean shutdown (onCompleted()) or connection error (onError()).
     * @param observer is the TaskQueueStreamObserver for the client whose connection
     * is now gone.
     */
    public void onRequestDisconnected(TaskQueueStreamObserver toRemove) {
        // Remove the request listener when the gRPC stream is completed (i.e.
        // graceful shutdown) or when the connection is broken.

        try {
            lock.lock();
            hungryClients.removeIf(thing -> thing.equals(toRemove));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called in two places:
     * 1. In the CommandProcessorDaoImpl::scheduleTask()
     * 2. In the CommandProcessor::init().
     *
     * Item 1) is quite self-explanatory.
     *
     * For Item 2), remember that the Task Queue Manager system is only in-memory.
     * Upon a restart or rebalance, we need to rebuild that state. During the
     * init() call, we iterate through all currently scheduled but not started tasks
     * in the state store.
     * @param tsrId is the ::getObjectId() for the TaskScheduleRequest that was
     * just scheduled.
     */
    public void onTaskScheduled(String tsrId) {
        // There's two cases here:
        // 1. There are clients waiting for requests, in which case we know that
        //    the pendingTaskIds queue/list must be empty.
        // 2. There are no clients waiting for requests. In this case, we just
        //    add the task id to the taskid list.

        try {
            lock.lock();
            if (!hungryClients.isEmpty()) {
                // This is case 1.
                if (!pendingTaskIds.isEmpty()) {
                    throw new RuntimeException(
                        "Can't have pending tasks and hungry clients"
                    );
                }

                TaskQueueStreamObserver luckyClient = hungryClients.poll();
                parent.itsAMatch(tsrId, luckyClient);
            } else {
                // case 2
                pendingTaskIds.push(tsrId);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called when a grpc client sends a new PollTaskPb.
     * @param requestThing is the grpc StreamObserver thing.
     */
    public void onPollRequest(TaskQueueStreamObserver requestThing) {
        if (taskDefName == null) {
            taskDefName = requestThing.getTaskDefName();
        }
        if (!taskDefName.equals(requestThing.getTaskDefName())) {
            throw new RuntimeException("Not possible, got mismatched taskdef name");
        }
        LHUtil.log(
            "Enqueueing observer with id",
            requestThing.getClientId(),
            "taskDef",
            taskDefName
        );

        // There's two cases here:
        // 1. There are pending Task Id's in the queue, which means that there
        //    are no "hungry clients" in the queue.
        // 2. There are no pending Taskid's in the queue, in which case we simply
        //    push the request client observer thing onto the back of the
        //    `hungryClients` list.

        try {
            lock.lock();
            if (!pendingTaskIds.isEmpty()) {
                // This is case 1.
                if (!hungryClients.isEmpty()) {
                    throw new RuntimeException(
                        "Can't have pending tasks and hungry clients"
                    );
                }

                String nextTaskId = pendingTaskIds.poll();
                parent.itsAMatch(nextTaskId, requestThing);
            } else {
                // case 2
                hungryClients.push(requestThing);
            }
        } finally {
            lock.unlock();
        }
    }
}
