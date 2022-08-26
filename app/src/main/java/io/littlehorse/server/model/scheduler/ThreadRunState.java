package io.littlehorse.server.model.scheduler;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.event.TaskResultEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.TaskStartedEvent;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.Edge;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.observability.ObservabilityEvent;
import io.littlehorse.common.model.observability.TaskResultOe;
import io.littlehorse.common.model.observability.TaskScheduledOe;
import io.littlehorse.common.model.observability.TaskStartOe;
import io.littlehorse.common.model.observability.ThreadStatusChangeOe;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.wfspec.NodePb.NodeCase;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.common.proto.scheduler.ThreadRunStatePb;
import io.littlehorse.common.proto.scheduler.ThreadRunStatePbOrBuilder;
import io.littlehorse.common.proto.scheduler.WfRunEventPb.EventCase;


public class ThreadRunState {
    public String threadSpecName;
    public LHStatusPb status;
    public NodeRunState currentNodeRun;

    // Below is just implementation details
    @JsonIgnore public int threadRunNumber;

    public ThreadRunStatePb.Builder toProto() {
        ThreadRunStatePb.Builder b = ThreadRunStatePb.newBuilder()
            .setThreadSpecName(threadSpecName)
            .setStatus(status);

        if (currentNodeRun != null) {
            b.setCurrentNodeRun(currentNodeRun.toProtoBuilder());
        }

        return b;
    }

    public static ThreadRunState fromProto(ThreadRunStatePbOrBuilder proto) {
        ThreadRunState out = new ThreadRunState();
        out.threadSpecName = proto.getThreadSpecName();
        out.status = proto.getStatus();
        if (proto.hasCurrentNodeRun()) {
            out.currentNodeRun = NodeRunState.fromProto(proto.getCurrentNodeRun());
        }
        return out;
    }

    // Implementation details below.
    @JsonIgnore public WfRunState wfRun;
    @JsonIgnore private ThreadSpec threadSpec;

    @JsonIgnore public ThreadSpec getThreadSpec() {
        if (threadSpec == null) {
            threadSpec = wfRun.wfSpec.threadSpecs.get(threadSpecName);
        }
        return threadSpec;
    }

    @JsonIgnore public Node getCurrentNode() {
        if (currentNodeRun == null) {
            return getThreadSpec().nodes.get(getThreadSpec().entrypointNodeName);
        } else {
            return getThreadSpec().nodes.get(currentNodeRun.nodeName);
        }
    }

    @JsonIgnore public void advance() {
        if (status != LHStatusPb.RUNNING) {
            if (status == LHStatusPb.HALTED) {
                throw new RuntimeException("Tried to advance HALTED thread");
            }
            if (status == LHStatusPb.HALTING) {
                status = LHStatusPb.HALTED;
            }
            if (status == LHStatusPb.COMPLETED || status == LHStatusPb.ERROR) {
                throw new RuntimeException("Tried to advance COMPLETED or ERROR thread");
            }
            return;
        }

        Node curNode = getCurrentNode();

        if (currentNodeRun == null) {
            // activate entrypoint node
            advanceFrom(curNode);
        } else if (currentNodeRun.status == LHStatusPb.COMPLETED) {
            // activate next node
            advanceFrom(curNode);
        } else if (currentNodeRun.status == LHStatusPb.ERROR) {
            // determine whether to retry or fail
            if (shouldRetry(curNode, currentNodeRun)) {
                scheduleRetry(curNode, currentNodeRun);
            } else {
                LHUtil.log("The node is failed and not retryable. Failing thread now.");
                setStatus(LHStatusPb.ERROR);
            }
        } else if (currentNodeRun.status == LHStatusPb.RUNNING) {
            // Nothing to do, just wait for next event to come in.
        } else if (currentNodeRun.status == LHStatusPb.STARTING) {
            // Nothing to do.
            // This is possible if we have scheduled a retry due to timeout and then an old
            // event just came in, or (to be implemented) if an external event comes in, or if
            // another thread notifies this thread that it's no longer blocked, etc.
        } else {
            throw new RuntimeException("Unexpected state for noderun: " + currentNodeRun.status);
        }
    }

    private boolean shouldRetry(Node curNode, NodeRunState currNodeRun) {
        if (curNode.type != NodeCase.TASK) return false;

        return currNodeRun.attemptNumber < curNode.taskNode.retries;
    }

    private void scheduleRetry(Node curNode, NodeRunState curNodeRun) {
        scheduleTaskNode(curNode, curNodeRun.attemptNumber + 1);
    }

    private void advanceFrom(Node curNode) {
        Node nextNode = null;
        for (Edge e: curNode.outgoingEdges) {
            if (evaluateEdge(e)) {
                nextNode = e.getSinkNode();
                break;
            }
        }
        if (nextNode == null) {
            throw new RuntimeException("Not possible to have a node with zero activated edges");
        }

        activateNode(nextNode);
    }

    private void activateNode(Node node) {
        switch (node.type) {
        case ENTRYPOINT:
            throw new RuntimeException("Not possible.");
        case TASK:
            scheduleTaskNode(node);
            break;
        case EXIT:
            setStatus(LHStatusPb.COMPLETED);
            break;
        case NODE_NOT_SET:
            throw new RuntimeException("Invalid nodetype.");
        }
    }

    // TODO: Do some conditional logic processing here.
    private boolean evaluateEdge(Edge e) {
        return true;
    }

    private void scheduleTaskNode(Node node) {
        scheduleTaskNode(node, 0);
    }

    private void scheduleTaskNode(Node node, int attemptNumber) {
        if (node.type != NodeCase.TASK) {
            throw new RuntimeException("Yikerz");
        }

        if (currentNodeRun == null) {
            currentNodeRun = new NodeRunState();
            if (attemptNumber > 0) {
                throw new RuntimeException("Not possible.");
            }

        } else {
            // Regardless of whether retry or not, actual position in the list increases.
            currentNodeRun.position++;

            // If we're doing a retry, it's the same logical number, so don't increment.
            if (attemptNumber == 0) {
                currentNodeRun.number++;
            }
        }

        currentNodeRun.nodeName = node.name;
        currentNodeRun.attemptNumber = attemptNumber;
        currentNodeRun.status = LHStatusPb.STARTING;

        TaskScheduleRequest tsr = new TaskScheduleRequest();

        // TODO: Add a TaskDefProcessor.
        tsr.replyKafkaTopic = LHConstants.WF_RUN_EVENT_TOPIC;
        tsr.taskDefId = node.taskNode.taskDefName;
        tsr.taskDefName = node.taskNode.taskDefName;
        tsr.taskRunNumber = currentNodeRun.number;
        tsr.taskRunPosition = currentNodeRun.position;
        tsr.threadRunNumber = threadRunNumber;
        tsr.wfRunId = wfRun.id;
        tsr.wfSpecId = wfRun.wfSpecId;
        tsr.nodeName = node.name;

        wfRun.oEvents.add(new ObservabilityEvent(
            new TaskScheduledOe(tsr),
            new Date()
        ));

        wfRun.tasksToSchedule.add(tsr);
    }

    private void setStatus(LHStatusPb newStatus) {
        status = newStatus;

        Date time = new Date();
        wfRun.oEvents.add(
            new ObservabilityEvent(
                new ThreadStatusChangeOe(threadRunNumber, status),
                time
            )
        );

        wfRun.handleThreadStatus(threadRunNumber, time, newStatus);
        
        if (newStatus == LHStatusPb.COMPLETED) {
            LHUtil.log(wfRun.id, "COMPLETED at", wfRun.endTime.getTime() - wfRun.startTime.getTime());
        }
    }

    public void processStartedEvent(WfRunEvent we) {
        wfRun.oEvents.add(
            new ObservabilityEvent(
                new TaskStartOe(we.startedEvent, currentNodeRun.nodeName),
                we.time
            )
        );
        TaskStartedEvent se = we.startedEvent;

        if (currentNodeRun.position != se.taskRunPosition) {
            // Out-of-order event due to race conditions between task worker
            // transactional producer and regular producer
            return;
        }
        currentNodeRun.status = LHStatusPb.RUNNING;

        // set timer for TimeOut
        WfRunEvent timerEvt = new WfRunEvent();
        timerEvt.wfRunId = wfRun.id;
        timerEvt.wfSpecId = wfRun.wfSpecId;
        Node node = getCurrentNode();
        SchedulerTimer timer = new SchedulerTimer();
        timer.wfRunId = wfRun.id;

        timer.event = timerEvt;
        timerEvt.type = EventCase.TASK_RESULT;
        timerEvt.taskResult = new TaskResultEvent();
        timerEvt.taskResult.resultCode = TaskResultCodePb.TIMEOUT;
        timerEvt.taskResult.taskRunNumber = currentNodeRun.number;
        timerEvt.taskResult.taskRunPosition = currentNodeRun.position;
        timerEvt.taskResult.threadRunNumber = threadRunNumber;
        timerEvt.time = timer.maturationTime = timerEvt.taskResult.time
            = new Date(new Date().getTime() + (1000 * node.taskNode.timeoutSeconds));

        wfRun.timersToSchedule.add(timer);
        currentNodeRun.timerKeys.add(timer.getStoreKey());
}

    public void processCompletedEvent(WfRunEvent we) {
        wfRun.oEvents.add(new ObservabilityEvent(
            new TaskResultOe(we.taskResult, currentNodeRun.nodeName),
            we.time
        ));
        TaskResultEvent ce = we.taskResult;
        if (currentNodeRun.position > ce.taskRunPosition) {
            // TODO: Determine if this is theoretically impossible.
            // If it's impossible, throw exception to prevent silent bugs.

            // Update 8/25: I think this is legally possible eg when a task gets timed out but
            // then the event comes in later. Perhaps if the producer retry timeout on the task
            // worker is longer than the task timeout...

            // default delivery.timeout.ms is 2 minutes.
            // TODO: As an experiment, see what happens when we reduce that to something less than
            // the task timeout (which we have at 10 seconds). If our theory is correct, then
            // we shouldn't get any warnings for stale task timeouts.

            // Also of note is the default `request.timeout.ms` set to 30 seconds. Also greater than
            // our task timeout.
            LHUtil.log("Warning: Got stale task timeout.");
            return;
        }

        if (currentNodeRun.position < ce.taskRunPosition) {
            throw new RuntimeException("Caught a message from the future!");
        }

        if (currentNodeRun.number != ce.taskRunNumber) {
            // Out-of-order event due to race conditions between task worker
            // transactional producer and regular producer
            return;
        }

        for (String timerKey: currentNodeRun.timerKeys) {
            wfRun.timersToClear.add(timerKey);
        }

        switch (ce.resultCode) {
            case SUCCESS:
                currentNodeRun.status = LHStatusPb.COMPLETED;
                break;

            case TIMEOUT:
                LHUtil.log(
                    "evt number:", ce.taskRunNumber, " vs ", currentNodeRun.number,
                    "evt position: ", ce.taskRunPosition, " vs ", currentNodeRun.position
                );
            case TASK_FAILURE:
                currentNodeRun.status = LHStatusPb.ERROR;
                break;

            case UNRECOGNIZED:
                throw new RuntimeException("Unrecognized TaskResultCode: " + ce.resultCode);
        }
    }
}
