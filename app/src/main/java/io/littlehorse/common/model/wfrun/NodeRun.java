package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.model.wfrun.subnoderun.EntrypointRun;
import io.littlehorse.common.model.wfrun.subnoderun.ExitRun;
import io.littlehorse.common.model.wfrun.subnoderun.ExternalEventRun;
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
import org.apache.commons.lang3.tuple.Pair;

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

    @JsonIgnore
    public EntrypointRun entrypointRun;

    public StartThreadRun startThreadRun;
    public WaitThreadRun waitThreadRun;

    public NodeRun() {
        failures = new ArrayList<>();
    }

    @JsonProperty("entrypointRun")
    public Object getEntrypointRunForJacksonOnly() {
        if (entrypointRun != null) {
            return new HashMap<>();
        }
        return null;
    }

    @JsonIgnore
    public ThreadRun threadRun;

    @JsonIgnore
    public Failure getLatestFailure() {
        if (failures.size() == 0) return null;
        return failures.get(failures.size() - 1);
    }

    public String getSubKey() {
        return NodeRun.getStoreKey(wfRunId, threadRunNumber, position);
    }

    public static String getStoreKey(String wfRunId, int threadNum, int position) {
        return wfRunId + "-" + threadNum + "-" + position;
    }

    @JsonIgnore
    public String getPartitionKey() {
        return wfRunId;
    }

    @JsonIgnore
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
            case NODETYPE_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }

        for (FailurePb failure : proto.getFailuresList()) {
            failures.add(Failure.fromProto(failure));
        }

        getSubNodeRun().setNodeRun(this);
    }

    @JsonIgnore
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
            case NODETYPE_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }
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
            case NODETYPE_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }

        for (Failure failure : failures) {
            out.addFailures(failure.toProto());
        }

        return out;
    }

    @JsonIgnore
    public List<Tag> getTags() {
        List<Tag> out = new ArrayList<>();

        out.add(
            new Tag(
                this,
                Pair.of("type", type.toString()),
                Pair.of("status", status.toString())
            )
        );

        if (type == NodeTypeCase.TASK) {
            out.addAll(taskRun.getTags(this));
        } else if (type == NodeTypeCase.EXTERNAL_EVENT) {
            out.addAll(externalEventRun.getTags(this));
        }

        return out;
    }

    @JsonIgnore
    public boolean isInProgress() {
        return (
            status != LHStatusPb.COMPLETED &&
            status != LHStatusPb.HALTED &&
            status != LHStatusPb.ERROR
        );
    }

    @JsonIgnore
    public Node getNode() {
        return threadRun.getThreadSpec().nodes.get(nodeName);
    }

    @JsonIgnore
    public NodeCase getNodeType() {
        return getNode().type;
    }

    /*
     * Returns whether it's currently safe to start an interrupt thread on this
     * NodeRun. The default answer is, "Is the node currently Running? If so, then
     * nope, else yes". However,
     */
    @JsonIgnore
    public boolean canBeInterrupted() {
        return getSubNodeRun().canBeInterrupted();
    }

    public boolean advanceIfPossible(Date time) {
        if (status == LHStatusPb.COMPLETED) {
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

    // public void fail(TaskResultCodePb resultCode, String message, Date time) {
    //     endTime = time;
    //     this.resultCode = resultCode;
    //     this.errorMessage = message;
    //     threadRun.fail(resultCode, message, time);
    // }

    public void fail(Failure failure, Date time) {
        this.failures.add(failure);
        endTime = time;
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
