package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.model.wfrun.subnoderun.EntrypointRun;
import io.littlehorse.common.model.wfrun.subnoderun.ExitRun;
import io.littlehorse.common.model.wfrun.subnoderun.ExternalEventRun;
import io.littlehorse.common.model.wfrun.subnoderun.SleepNodeRun;
import io.littlehorse.common.model.wfrun.subnoderun.StartThreadRun;
import io.littlehorse.common.model.wfrun.subnoderun.TaskNodeRun;
import io.littlehorse.common.model.wfrun.subnoderun.UserTaskRun;
import io.littlehorse.common.model.wfrun.subnoderun.WaitThreadRun;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.FailurePb;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.NodePb.NodeCase;
import io.littlehorse.jlib.common.proto.NodeRunPb;
import io.littlehorse.jlib.common.proto.NodeRunPb.NodeTypeCase;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class NodeRun extends Getable<NodeRunPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int position;

    public LHStatusPb status;

    public Date arrivalTime;
    public Date endTime;

    public WfSpecId wfSpecId;
    public String wfSpecName;
    public String threadSpecName;
    public String nodeName;

    public String errorMessage;

    public List<Failure> failures;

    public ExternalEventRun externalEventRun;
    public TaskNodeRun taskRun;
    public NodeTypeCase type;
    public ExitRun exitRun;
    public EntrypointRun entrypointRun;
    public StartThreadRun startThreadRun;
    public WaitThreadRun waitThreadRun;
    public SleepNodeRun sleepNodeRun;
    public UserTaskRun userTaskRun;

    public List<Integer> failureHandlerIds;

    public NodeRun() {
        failures = new ArrayList<>();
        failureHandlerIds = new ArrayList<>();
    }

    public Object getEntrypointRunForJacksonOnly() {
        if (entrypointRun != null) {
            return new HashMap<>();
        }
        return null;
    }

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ThreadRun threadRunDoNotUseMe;

    public ThreadRun getThreadRun() {
        if (threadRunDoNotUseMe == null) {
            WfRun wfRun = getDao().getWfRun(wfRunId);
            threadRunDoNotUseMe = wfRun.getThreadRuns().get(threadRunNumber);
        }
        return threadRunDoNotUseMe;
    }

    public void setThreadRun(ThreadRun threadRun) {
        threadRunDoNotUseMe = threadRun;
    }

    public Failure getLatestFailure() {
        if (failures.size() == 0) return null;
        return failures.get(failures.size() - 1);
    }

    public NodeRunId getObjectId() {
        return new NodeRunId(wfRunId, threadRunNumber, position);
    }

    public Class<NodeRunPb> getProtoBaseClass() {
        return NodeRunPb.class;
    }

    public Date getCreatedAt() {
        return arrivalTime;
    }

    @Override
    public List<GetableIndex> getIndexes() {
        // TODO
        return new ArrayList<>();
    }

    public void initFrom(Message p) {
        NodeRunPb proto = (NodeRunPb) p;
        wfRunId = proto.getWfRunId();
        threadRunNumber = proto.getThreadRunNumber();
        position = proto.getPosition();

        arrivalTime = LHUtil.fromProtoTs(proto.getArrivalTime());
        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }

        wfSpecId = LHSerializable.fromProto(proto.getWfSpecId(), WfSpecId.class);
        threadSpecName = proto.getThreadSpecName();
        nodeName = proto.getNodeName();
        status = proto.getStatus();

        if (proto.hasErrorMessage()) errorMessage = proto.getErrorMessage();

        type = proto.getNodeTypeCase();
        switch (type) {
            case TASK:
                taskRun =
                    LHSerializable.fromProto(proto.getTask(), TaskNodeRun.class);
                break;
            case EXTERNAL_EVENT:
                externalEventRun =
                    ExternalEventRun.fromProto(proto.getExternalEvent());
                break;
            case EXIT:
                exitRun = ExitRun.fromProto(proto.getExit());
                break;
            case ENTRYPOINT:
                entrypointRun = EntrypointRun.fromProto(proto.getEntrypoint());
                break;
            case START_THREAD:
                startThreadRun = StartThreadRun.fromProto(proto.getStartThread());
                break;
            case WAIT_THREAD:
                waitThreadRun = WaitThreadRun.fromProto(proto.getWaitThread());
                break;
            case SLEEP:
                sleepNodeRun = SleepNodeRun.fromProto(proto.getSleep());
                break;
            case USER_TASK:
                userTaskRun =
                    LHSerializable.fromProto(proto.getUserTask(), UserTaskRun.class);
                break;
            case NODETYPE_NOT_SET:
                throw new RuntimeException("Not possible");
        }

        for (FailurePb failure : proto.getFailuresList()) {
            failures.add(Failure.fromProto(failure));
        }
        for (int handlerId : proto.getFailureHandlerIdsList()) {
            failureHandlerIds.add(handlerId);
        }

        getSubNodeRun().setNodeRun(this);
    }

    public SubNodeRun<?> getSubNodeRun() {
        switch (type) {
            case TASK:
                return taskRun;
            case EXTERNAL_EVENT:
                return externalEventRun;
            case ENTRYPOINT:
                return entrypointRun;
            case EXIT:
                return exitRun;
            case WAIT_THREAD:
                return waitThreadRun;
            case START_THREAD:
                return startThreadRun;
            case SLEEP:
                return sleepNodeRun;
            case USER_TASK:
                return userTaskRun;
            case NODETYPE_NOT_SET:
        }
        throw new RuntimeException("Not possible");
    }

    public void setSubNodeRun(SubNodeRun<?> snr) {
        Class<?> cls = snr.getClass();
        if (cls.equals(TaskNodeRun.class)) {
            type = NodeTypeCase.TASK;
            taskRun = (TaskNodeRun) snr;
        } else if (cls.equals(EntrypointRun.class)) {
            type = NodeTypeCase.ENTRYPOINT;
            entrypointRun = (EntrypointRun) snr;
        } else if (cls.equals(ExitRun.class)) {
            type = NodeTypeCase.EXIT;
            exitRun = (ExitRun) snr;
        } else if (cls.equals(ExternalEventRun.class)) {
            type = NodeTypeCase.EXTERNAL_EVENT;
            externalEventRun = (ExternalEventRun) snr;
        } else if (cls.equals(StartThreadRun.class)) {
            type = NodeTypeCase.START_THREAD;
            startThreadRun = (StartThreadRun) snr;
        } else if (cls.equals(WaitThreadRun.class)) {
            type = NodeTypeCase.WAIT_THREAD;
            waitThreadRun = (WaitThreadRun) snr;
        } else if (cls.equals(SleepNodeRun.class)) {
            type = NodeTypeCase.SLEEP;
            sleepNodeRun = (SleepNodeRun) snr;
        } else if (cls.equals(UserTaskRun.class)) {
            type = NodeTypeCase.USER_TASK;
            userTaskRun = (UserTaskRun) snr;
        } else {
            throw new RuntimeException("Didn't recognize " + snr.getClass());
        }

        snr.nodeRun = this;
    }

    public NodeRunPb.Builder toProto() {
        NodeRunPb.Builder out = NodeRunPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setPosition(position)
            .setStatus(status)
            .setArrivalTime(LHUtil.fromDate(arrivalTime))
            .setWfSpecId(wfSpecId.toProto())
            .setThreadSpecName(threadSpecName)
            .setNodeName(nodeName);

        if (endTime != null) out.setEndTime(LHUtil.fromDate(endTime));

        if (errorMessage != null) out.setErrorMessage(errorMessage);

        switch (type) {
            case TASK:
                out.setTask(taskRun.toProto());
                break;
            case EXTERNAL_EVENT:
                out.setExternalEvent(externalEventRun.toProto());
                break;
            case ENTRYPOINT:
                out.setEntrypoint(entrypointRun.toProto());
                break;
            case EXIT:
                out.setExit(exitRun.toProto());
                break;
            case START_THREAD:
                out.setStartThread(startThreadRun.toProto());
                break;
            case WAIT_THREAD:
                out.setWaitThread(waitThreadRun.toProto());
                break;
            case SLEEP:
                out.setSleep(sleepNodeRun.toProto());
                break;
            case USER_TASK:
                out.setUserTask(userTaskRun.toProto());
                break;
            case NODETYPE_NOT_SET:
        }

        for (Failure failure : failures) {
            out.addFailures(failure.toProto());
        }
        for (Integer id : failureHandlerIds) {
            out.addFailureHandlerIds(id);
        }

        return out;
    }

    public boolean isInProgress() {
        return (
            status != LHStatusPb.COMPLETED &&
            status != LHStatusPb.HALTED &&
            status != LHStatusPb.ERROR
        );
    }

    public boolean isCompletedOrRecoveredFromFailure() {
        if (status == LHStatusPb.COMPLETED) {
            return true;
        }

        if (status == LHStatusPb.ERROR) {
            if (failureHandlerIds.size() == failures.size()) {
                if (failures.size() == 0) {
                    log.warn("Somehow failed with no failures.");
                    return false;
                }
                for (int handlerId : failureHandlerIds) {
                    ThreadRun handler = getThreadRun()
                        .wfRun.threadRuns.get(handlerId);
                    if (handler.status != LHStatusPb.COMPLETED) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public Node getNode() {
        return getThreadRun().getThreadSpec().nodes.get(nodeName);
    }

    public NodeCase getNodeType() {
        return getNode().type;
    }

    /*
     * Returns whether it's currently safe to start an interrupt thread on this
     * NodeRun. The default answer is, "Is the node currently Running? If so, then
     * nope, else yes". However,
     */
    public boolean canBeInterrupted() {
        return getSubNodeRun().canBeInterrupted();
    }

    public boolean advanceIfPossible(Date time) {
        if (isCompletedOrRecoveredFromFailure()) {
            getThreadRun().advanceFrom(getNode());
            return true;
        } else {
            return getSubNodeRun().advanceIfPossible(time);
        }
    }

    public void complete(VariableValue output, Date time) {
        endTime = time;
        status = LHStatusPb.COMPLETED;
        getThreadRun().completeCurrentNode(output, time);
    }

    public void fail(Failure failure, Date time) {
        this.failures.add(failure);
        endTime = time;
        status = LHStatusPb.ERROR;
        errorMessage = failure.message;
        getThreadRun().fail(failure, time);
    }
}
