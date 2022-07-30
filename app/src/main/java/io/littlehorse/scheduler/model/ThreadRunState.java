package io.littlehorse.scheduler.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.event.TaskCompletedEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.TaskStartedEvent;
import io.littlehorse.common.model.event.WFRunEvent;
import io.littlehorse.common.model.meta.Edge;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.observability.ObservabilityEvent;
import io.littlehorse.common.model.observability.TaskCompleteOe;
import io.littlehorse.common.model.observability.TaskScheduledOe;
import io.littlehorse.common.model.observability.TaskStartOe;
import io.littlehorse.common.model.observability.ThreadStatusChangeOe;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.NodeTypePb;
import io.littlehorse.common.proto.ThreadRunStatePb;
import io.littlehorse.common.proto.ThreadRunStatePbOrBuilder;


public class ThreadRunState {
    public String threadSpecName;
    public LHStatusPb status;
    public NodeRunState currentNodeRun;

    // Below is just implementation details
    @JsonIgnore public int threadRunNumber;

    public ThreadRunStatePb.Builder toProtoBuilder() {
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

    @JsonIgnore public void advance() {
        if (status != LHStatusPb.RUNNING) {
            return;
        }

        Node nextNode = getNextNode();

        while (nextNode != null) {
            activateNode(nextNode);
            nextNode = getNextNode();
        }
    }

    private void activateNode(Node node) {
        switch (node.type) {
        case ENTRYPOINT:
            throw new RuntimeException("Not possible.");
        case TASK:
            activateTaskNode(node);
            break;
        case EXIT:
            completeThread();
            break;
        case UNRECOGNIZED:
            throw new RuntimeException("Invalid nodetype.");
        }
    }

    /**
     * Returns the next node to activate, or null if currently blocked by some
     * other thread (i.e. for variable contention).
     * @param current The currently running node.
     * @return the next Node to activate, or null if blocked. TODO: Maybe will need
     *         to return info about the thread which is blocking us, or set that
     *         info somewhere.
     */
    public Node getNextNode() {
        if (status != LHStatusPb.RUNNING) {
            return null;
        }

        if (currentNodeRun != null && currentNodeRun.status != LHStatusPb.COMPLETED) {
            return null;
        }

        Node current;
        if (currentNodeRun == null) {
            current = getThreadSpec().nodes.get(getThreadSpec().entrypointNodeName);
        } else {
            current = getThreadSpec().nodes.get(currentNodeRun.nodeName);
        }

        if (current.type == NodeTypePb.EXIT) {
            return null;
        }

        for (Edge e: current.outgoingEdges) {
            if (evaluateEdge(e)) {
                return e.getSinkNode();
            }
        }
        throw new RuntimeException("Not possible");
    }

    // TODO: Do some conditional logic processing here.
    private boolean evaluateEdge(Edge e) {
        return true;
    }

    private void activateTaskNode(Node node) {
        if (node.type != NodeTypePb.TASK) {
            throw new RuntimeException("Yikerz");
        }

        if (currentNodeRun == null) {
            currentNodeRun = new NodeRunState();
        } else {
            currentNodeRun.number++;
            currentNodeRun.position++;
        }
        currentNodeRun.nodeName = node.name;
        currentNodeRun.attemptNumber = 0;
        currentNodeRun.status = LHStatusPb.STARTING;

        TaskScheduleRequest tsr = new TaskScheduleRequest();

        // TODO: Add a TaskDefProcessor.
        tsr.replyKafkaTopic = LHConstants.WF_RUN_EVENT_TOPIC;
        tsr.taskDefId = node.taskDefName;
        tsr.taskDefName = node.taskDefName;
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

        wfRun.toSchedule.add(tsr);
    }

    private void completeThread() {
        status = LHStatusPb.COMPLETED;

        Date time = new Date();
        wfRun.oEvents.add(
            new ObservabilityEvent(
                new ThreadStatusChangeOe(threadRunNumber, status),
                time
            )
        );

        wfRun.complete(time);
        System.out.println(wfRun.endTime.getTime() - wfRun.startTime.getTime());
    }

    public void processStartedEvent(WFRunEvent we) {
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
        if (currentNodeRun.number != se.taskRunNumber) {
            // Out-of-order event due to race conditions between task worker
            // transactional producer and regular producer
            return;
        }
        currentNodeRun.status = LHStatusPb.RUNNING;
    }

    public void processCompletedEvent(WFRunEvent we) {
        wfRun.oEvents.add(new ObservabilityEvent(
            new TaskCompleteOe(we.completedEvent, currentNodeRun.nodeName),
            we.time
        ));
        TaskCompletedEvent ce = we.completedEvent;
        if (currentNodeRun.position != ce.taskRunPosition) {
            // Out-of-order event due to race conditions between task worker
            // transactional producer and regular producer
            return;
        }
        if (currentNodeRun.number != ce.taskRunNumber) {
            // Out-of-order event due to race conditions between task worker
            // transactional producer and regular producer
            return;
        }

        currentNodeRun.status = LHStatusPb.COMPLETED;
    }
}
