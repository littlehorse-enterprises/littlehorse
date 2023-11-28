package io.littlehorse.common.model.getable.core.noderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.EntrypointRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExitRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExternalEventRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.SleepNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.StartMultipleThreadsRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.StartThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.TaskNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.UserTaskNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.WaitForThreadsRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@Slf4j
public class NodeRunModel extends CoreGetable<NodeRun> {

    public String wfRunId;
    public int threadRunNumber;
    public int position;

    public LHStatus status;

    public Date arrivalTime;
    public Date endTime;

    public WfSpecIdModel wfSpecId;
    public String wfSpecName;
    public String threadSpecName;
    public String nodeName;

    public String errorMessage;

    public List<FailureModel> failures = new ArrayList<>();

    public ExternalEventRunModel externalEventRun;
    public TaskNodeRunModel taskRun;
    public NodeTypeCase type;
    public ExitRunModel exitRun;
    public EntrypointRunModel entrypointRun;
    public StartThreadRunModel startThreadRun;
    private StartMultipleThreadsRunModel startMultipleThreadsRun;
    public WaitForThreadsRunModel waitThreadsRun;
    public SleepNodeRunModel sleepNodeRun;
    public UserTaskNodeRunModel userTaskRun;

    public List<Integer> failureHandlerIds = new ArrayList<>();
    private ExecutionContext executionContext;

    public NodeRunModel() {}

    public NodeRunModel(ProcessorExecutionContext processorContext) {
        this.executionContext = processorContext;
    }

    public Object getEntrypointRunForJacksonOnly() {
        if (entrypointRun != null) {
            return new HashMap<>();
        }
        return null;
    }

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ThreadRunModel threadRunModelDoNotUseMe;

    public ThreadRunModel getThreadRun() {
        if (threadRunModelDoNotUseMe == null) {
            ProcessorExecutionContext processorContext =
                    executionContext.castOnSupport(ProcessorExecutionContext.class);
            WfRunModel wfRunModel = processorContext.getableManager().get(new WfRunIdModel(wfRunId));
            threadRunModelDoNotUseMe = wfRunModel.getThreadRuns().get(threadRunNumber);
        }
        return threadRunModelDoNotUseMe;
    }

    public void setThreadRun(ThreadRunModel threadRunModel) {
        threadRunModelDoNotUseMe = threadRunModel;
    }

    public FailureModel getLatestFailure() {
        if (failures.size() == 0) return null;
        return failures.get(failures.size() - 1);
    }

    public NodeRunIdModel getObjectId() {
        return new NodeRunIdModel(wfRunId, threadRunNumber, position);
    }

    public Class<NodeRun> getProtoBaseClass() {
        return NodeRun.class;
    }

    public Date getCreatedAt() {
        return arrivalTime;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(new GetableIndex<NodeRunModel>(
                List.of(
                        Pair.of("status", GetableIndex.ValueType.SINGLE),
                        Pair.of("type", GetableIndex.ValueType.SINGLE)),
                Optional.of(TagStorageType.LOCAL)));
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "status" -> {
                return List.of(new IndexedField(key, this.getStatus().toString(), TagStorageType.LOCAL));
            }
            case "type" -> {
                return List.of(new IndexedField(key, this.getType().toString(), TagStorageType.LOCAL));
            }
        }
        log.warn("Tried to get value for unknown index field {}", key);
        return List.of();
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        NodeRun proto = (NodeRun) p;
        wfRunId = proto.getWfRunId();
        threadRunNumber = proto.getThreadRunNumber();
        position = proto.getPosition();

        arrivalTime = LHUtil.fromProtoTs(proto.getArrivalTime());
        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }

        wfSpecId = LHSerializable.fromProto(proto.getWfSpecId(), WfSpecIdModel.class, context);
        threadSpecName = proto.getThreadSpecName();
        nodeName = proto.getNodeName();
        status = proto.getStatus();

        if (proto.hasErrorMessage()) errorMessage = proto.getErrorMessage();

        type = proto.getNodeTypeCase();
        switch (type) {
            case TASK:
                taskRun = LHSerializable.fromProto(proto.getTask(), TaskNodeRunModel.class, context);
                break;
            case EXTERNAL_EVENT:
                externalEventRun = ExternalEventRunModel.fromProto(proto.getExternalEvent(), context);
                break;
            case EXIT:
                exitRun = ExitRunModel.fromProto(proto.getExit(), context);
                break;
            case ENTRYPOINT:
                entrypointRun = EntrypointRunModel.fromProto(proto.getEntrypoint(), context);
                break;
            case START_THREAD:
                startThreadRun = StartThreadRunModel.fromProto(proto.getStartThread(), context);
                break;
            case WAIT_THREADS:
                waitThreadsRun = WaitForThreadsRunModel.fromProto(proto.getWaitThreads(), context);
                break;
            case SLEEP:
                sleepNodeRun = SleepNodeRunModel.fromProto(proto.getSleep(), context);
                break;
            case USER_TASK:
                userTaskRun = LHSerializable.fromProto(proto.getUserTask(), UserTaskNodeRunModel.class, context);
                break;
            case START_MULTIPLE_THREADS:
                startMultipleThreadsRun = LHSerializable.fromProto(
                        proto.getStartMultipleThreads(), StartMultipleThreadsRunModel.class, context);
                break;
            case NODETYPE_NOT_SET:
                throw new RuntimeException("Not possible");
        }

        for (Failure failure : proto.getFailuresList()) {
            failures.add(FailureModel.fromProto(failure, context));
        }
        for (int handlerId : proto.getFailureHandlerIdsList()) {
            failureHandlerIds.add(handlerId);
        }
        this.executionContext = context;
        getSubNodeRun().setNodeRunModel(this);
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
            case WAIT_THREADS:
                return waitThreadsRun;
            case START_THREAD:
                return startThreadRun;
            case SLEEP:
                return sleepNodeRun;
            case USER_TASK:
                return userTaskRun;
            case START_MULTIPLE_THREADS:
                return startMultipleThreadsRun;
            case NODETYPE_NOT_SET:
        }
        throw new RuntimeException("Not possible");
    }

    public void setSubNodeRun(SubNodeRun<?> snr) {
        Class<?> cls = snr.getClass();
        if (cls.equals(TaskNodeRunModel.class)) {
            type = NodeTypeCase.TASK;
            taskRun = (TaskNodeRunModel) snr;
        } else if (cls.equals(EntrypointRunModel.class)) {
            type = NodeTypeCase.ENTRYPOINT;
            entrypointRun = (EntrypointRunModel) snr;
        } else if (cls.equals(ExitRunModel.class)) {
            type = NodeTypeCase.EXIT;
            exitRun = (ExitRunModel) snr;
        } else if (cls.equals(ExternalEventRunModel.class)) {
            type = NodeTypeCase.EXTERNAL_EVENT;
            externalEventRun = (ExternalEventRunModel) snr;
        } else if (cls.equals(StartThreadRunModel.class)) {
            type = NodeTypeCase.START_THREAD;
            startThreadRun = (StartThreadRunModel) snr;
        } else if (cls.equals(WaitForThreadsRunModel.class)) {
            type = NodeTypeCase.WAIT_THREADS;
            waitThreadsRun = (WaitForThreadsRunModel) snr;
        } else if (cls.equals(SleepNodeRunModel.class)) {
            type = NodeTypeCase.SLEEP;
            sleepNodeRun = (SleepNodeRunModel) snr;
        } else if (cls.equals(UserTaskNodeRunModel.class)) {
            type = NodeTypeCase.USER_TASK;
            userTaskRun = (UserTaskNodeRunModel) snr;
        } else if (cls.equals(StartMultipleThreadsRunModel.class)) {
            type = NodeTypeCase.START_MULTIPLE_THREADS;
            startMultipleThreadsRun = (StartMultipleThreadsRunModel) snr;
        } else {
            throw new RuntimeException("Didn't recognize " + snr.getClass());
        }

        snr.nodeRunModel = this;
    }

    public NodeRun.Builder toProto() {
        NodeRun.Builder out = NodeRun.newBuilder()
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
            case WAIT_THREADS:
                out.setWaitThreads(waitThreadsRun.toProto());
                break;
            case SLEEP:
                out.setSleep(sleepNodeRun.toProto());
                break;
            case USER_TASK:
                out.setUserTask(userTaskRun.toProto());
                break;
            case START_MULTIPLE_THREADS:
                out.setStartMultipleThreads(startMultipleThreadsRun.toProto());
            case NODETYPE_NOT_SET:
        }

        for (FailureModel failure : failures) {
            out.addFailures(failure.toProto());
        }
        for (Integer id : failureHandlerIds) {
            out.addFailureHandlerIds(id);
        }

        return out;
    }

    public boolean isInProgress() {
        return (status != LHStatus.COMPLETED && status != LHStatus.HALTED && status != LHStatus.ERROR);
    }

    public boolean isCompletedOrRecoveredFromFailure() {
        if (status == LHStatus.COMPLETED) {
            return true;
        }

        if (status == LHStatus.ERROR || status == LHStatus.HALTED || status == LHStatus.EXCEPTION) {
            if (failureHandlerIds.size() == failures.size()) {
                if (failures.size() == 0) {
                    log.warn("Somehow failed with no failures.");
                    return false;
                }
                for (int handlerId : failureHandlerIds) {
                    ThreadRunModel handler = getThreadRun().wfRun.getThreadRun(handlerId);
                    if (handler.status != LHStatus.COMPLETED) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public NodeModel getNode() {
        return getThreadRun().getThreadSpecModel().nodes.get(nodeName);
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

    public void complete(VariableValueModel output, Date time) {
        endTime = time;
        status = LHStatus.COMPLETED;
        getThreadRun().completeCurrentNode(output, time);
    }

    public void fail(FailureModel failure, Date time) {
        this.failures.add(failure);
        endTime = time;
        status = failure.getStatus();
        errorMessage = failure.message;
        getThreadRun().fail(failure, time);
    }

    public void failWithoutGrace(FailureModel failure, Date time) {
        this.failures.add(failure);
        endTime = time;
        status = failure.getStatus();
        errorMessage = failure.message;
        getThreadRun().failWithoutGrace(failure, time);
    }

    public void halt() {
        if (!isInProgress()) {
            return;
        }

        status = LHStatus.HALTED;
        getSubNodeRun().halt();
    }
}
