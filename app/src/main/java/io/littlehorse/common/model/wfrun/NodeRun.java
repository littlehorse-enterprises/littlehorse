package io.littlehorse.common.model.wfrun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.wfrun.subnoderun.EntrypointRun;
import io.littlehorse.common.model.wfrun.subnoderun.ExitRun;
import io.littlehorse.common.model.wfrun.subnoderun.ExternalEventRun;
import io.littlehorse.common.model.wfrun.subnoderun.SleepNodeRun;
import io.littlehorse.common.model.wfrun.subnoderun.StartThreadRun;
import io.littlehorse.common.model.wfrun.subnoderun.TaskRun;
import io.littlehorse.common.model.wfrun.subnoderun.WaitThreadRun;
import io.littlehorse.common.proto.FailurePb;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.NodePb.NodeCase;
import io.littlehorse.common.proto.NodeRunPb;
import io.littlehorse.common.proto.NodeRunPb.NodeTypeCase;
import io.littlehorse.common.proto.NodeRunPbOrBuilder;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class NodeRun extends GETable<NodeRunPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int position;

    public int attemptNumber;
    public int number;
    public LHStatusPb status;

    public Date arrivalTime;
    public Date endTime;

    public String wfSpecId;
    public String wfSpecName;
    public String threadSpecName;
    public String nodeName;

    public TaskResultCodePb resultCode;
    public String errorMessage;
    public NodeTypeCase type;

    public TaskRun taskRun;
    public ExternalEventRun externalEventRun;

    public List<Failure> failures;

    public ExitRun exitRun;
    public EntrypointRun entrypointRun;
    public StartThreadRun startThreadRun;
    public WaitThreadRun waitThreadRun;
    public SleepNodeRun sleepNodeRun;

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

    public ThreadRun threadRun;

    public Failure getLatestFailure() {
        if (failures.size() == 0) return null;
        return failures.get(failures.size() - 1);
    }

    public String getObjectId() {
        return NodeRun.getStoreKey(wfRunId, threadRunNumber, position);
    }

    public static String getStoreKey(String wfRunId, int threadNum, int position) {
        return wfRunId + "-" + threadNum + "-" + position;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public Class<NodeRunPb> getProtoBaseClass() {
        return NodeRunPb.class;
    }

    public Date getCreatedAt() {
        return arrivalTime;
    }

    public void initFrom(MessageOrBuilder p) {
        NodeRunPbOrBuilder proto = (NodeRunPbOrBuilder) p;
        wfRunId = proto.getWfRunId();
        threadRunNumber = proto.getThreadRunNumber();
        position = proto.getPosition();

        number = proto.getNumber();

        arrivalTime = LHUtil.fromProtoTs(proto.getArrivalTime());
        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }

        wfSpecId = proto.getWfSpecId();
        threadSpecName = proto.getThreadSpecName();
        nodeName = proto.getNodeName();
        status = proto.getStatus();
        attemptNumber = proto.getAttemptNumber();

        if (proto.hasResultCode()) resultCode = proto.getResultCode();

        if (proto.hasErrorMessage()) errorMessage = proto.getErrorMessage();

        type = proto.getNodeTypeCase();
        switch (type) {
            case TASK:
                taskRun = TaskRun.fromProto(proto.getTask());
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
                sleepNodeRun = SleepNodeRun.fromProto(proto.getSleepOrBuilder());
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
            case NODETYPE_NOT_SET:
        }
        throw new RuntimeException("Not possible");
    }

    public void setSubNodeRun(SubNodeRun<?> snr) {
        Class<?> cls = snr.getClass();
        if (cls.equals(TaskRun.class)) {
            type = NodeTypeCase.TASK;
            taskRun = (TaskRun) snr;
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
            .setNumber(number)
            .setStatus(status)
            .setArrivalTime(LHUtil.fromDate(arrivalTime))
            .setWfSpecId(wfSpecId)
            .setThreadSpecName(threadSpecName)
            .setNodeName(nodeName)
            .setAttemptNumber(attemptNumber);

        if (endTime != null) out.setEndTime(LHUtil.fromDate(endTime));

        if (resultCode != null) out.setResultCode(resultCode);

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
                    LHUtil.log("WARN: somehow failed with no failures.");
                    return false;
                }
                for (int handlerId : failureHandlerIds) {
                    ThreadRun handler = threadRun.wfRun.threadRuns.get(handlerId);
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
        return threadRun.getThreadSpec().nodes.get(nodeName);
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
            threadRun.advanceFrom(getNode());
            return true;
        } else {
            return getSubNodeRun().advanceIfPossible(time);
        }
    }

    public void complete(VariableValue output, Date time) {
        endTime = time;
        status = LHStatusPb.COMPLETED;
        threadRun.completeCurrentNode(output, time);
    }

    public void fail(Failure failure, Date time) {
        this.failures.add(failure);
        endTime = time;
        status = LHStatusPb.ERROR;
        resultCode = failure.failureCode;
        errorMessage = failure.message;
        threadRun.fail(failure, time);
    }

    public void doRetry(TaskResultCodePb resultCode, String message, Date time) {
        endTime = time;
        errorMessage = "Doing a retry: " + message;
        this.resultCode = resultCode;

        LHUtil.log("Doing retry");

        threadRun.activateNode(getNode());
    }
}
