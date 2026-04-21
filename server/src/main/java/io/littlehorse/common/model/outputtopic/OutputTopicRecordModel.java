package io.littlehorse.common.model.outputtopic;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.CoreOutputTopicGetable;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.externalevent.CorrelatedEventModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.taskrun.CheckpointModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.OutputTopicRecord;
import io.littlehorse.sdk.common.proto.OutputTopicRecord.PayloadCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class OutputTopicRecordModel extends LHSerializable<OutputTopicRecord> {
    private Timestamp timestamp;
    private PayloadCase payloadCase;
    private WfRunModel wfRun;
    private ExternalEventModel externalEvent;
    private WorkflowEventModel workflowEvent;
    private UserTaskRunModel userTaskRun;
    private VariableModel variable;
    private TaskRunModel taskRun;
    private CorrelatedEventModel correlatedEvent;
    private CheckpointModel checkpoint;

    public OutputTopicRecordModel() {
        this.timestamp = LHLibUtil.fromDate(new Date());
    }

    public OutputTopicRecordModel(CoreOutputTopicGetable<?> thing, Date time) {
        this.timestamp = LHLibUtil.fromDate(time);
        setPayload(thing);
    }

    @Override
    public Class<OutputTopicRecord> getProtoBaseClass() {
        return OutputTopicRecord.class;
    }

    @Override
    public OutputTopicRecord.Builder toProto() {
        OutputTopicRecord.Builder out = OutputTopicRecord.newBuilder().setTimestamp(timestamp);
        switch (payloadCase) {
            case WF_RUN:
                out.setWfRun(wfRun.toProto());
                break;
            case USER_TASK_RUN:
                out.setUserTaskRun(userTaskRun.toProto());
                break;
            case EXTERNAL_EVENT:
                out.setExternalEvent(externalEvent.toProto());
                break;
            case TASK_RUN:
                out.setTaskRun(taskRun.toProto());
                break;
            case WORKFLOW_EVENT:
                out.setWorkflowEvent(workflowEvent.toProto());
                break;
            case VARIABLE:
                out.setVariable(variable.toProto());
                break;
            case CORRELATED_EVENT:
                out.setCorrelatedEvent(correlatedEvent.toProto());
                break;
            case TASK_CHECKPOINT:
                out.setTaskCheckpoint(checkpoint.toProto());
                break;
            case PAYLOAD_NOT_SET:
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        OutputTopicRecord p = (OutputTopicRecord) proto;
        timestamp = p.getTimestamp();
        payloadCase = p.getPayloadCase();
        switch (payloadCase) {
            case WF_RUN:
                wfRun = LHSerializable.fromProto(p.getWfRun(), WfRunModel.class, ignored);
                break;
            case USER_TASK_RUN:
                userTaskRun = LHSerializable.fromProto(p.getUserTaskRun(), UserTaskRunModel.class, ignored);
                break;
            case EXTERNAL_EVENT:
                externalEvent = LHSerializable.fromProto(p.getExternalEvent(), ExternalEventModel.class, ignored);
                break;
            case TASK_RUN:
                taskRun = LHSerializable.fromProto(p.getTaskRun(), TaskRunModel.class, ignored);
                break;
            case WORKFLOW_EVENT:
                workflowEvent = LHSerializable.fromProto(p.getWorkflowEvent(), WorkflowEventModel.class, ignored);
                break;
            case VARIABLE:
                variable = LHSerializable.fromProto(p.getVariable(), VariableModel.class, ignored);
                break;
            case CORRELATED_EVENT:
                correlatedEvent = LHSerializable.fromProto(p.getCorrelatedEvent(), CorrelatedEventModel.class, ignored);
                break;
            case TASK_CHECKPOINT:
                checkpoint = LHSerializable.fromProto(p.getTaskCheckpoint(), CheckpointModel.class, ignored);
                break;
            case PAYLOAD_NOT_SET:
        }
    }

    public String getPartitionKey() {
        return getSubrecord().getPartitionKey().get();
    }

    public CoreGetable<?> getSubrecord() {
        switch (payloadCase) {
            case WF_RUN:
                return wfRun;
            case TASK_RUN:
                return taskRun;
            case EXTERNAL_EVENT:
                return externalEvent;
            case USER_TASK_RUN:
                return userTaskRun;
            case WORKFLOW_EVENT:
                return workflowEvent;
            case VARIABLE:
                return variable;
            case CORRELATED_EVENT:
                return correlatedEvent;
            case TASK_CHECKPOINT:
                return checkpoint;
            case PAYLOAD_NOT_SET:
        }
        throw new IllegalStateException("Forgot to add new output topic record type here");
    }

    public void setPayload(CoreOutputTopicGetable<?> thing) {
        if (thing == null) {
            throw new IllegalArgumentException();
        }
        if (WfRunModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.WF_RUN;
            this.wfRun = (WfRunModel) thing;
        } else if (UserTaskRunModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.USER_TASK_RUN;
            this.userTaskRun = (UserTaskRunModel) thing;
        } else if (ExternalEventModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.EXTERNAL_EVENT;
            this.externalEvent = (ExternalEventModel) thing;
        } else if (TaskRunModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.TASK_RUN;
            this.taskRun = (TaskRunModel) thing;
        } else if (WorkflowEventModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.WORKFLOW_EVENT;
            this.workflowEvent = (WorkflowEventModel) thing;
        } else if (VariableModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.VARIABLE;
            this.variable = (VariableModel) thing;
        } else if (CorrelatedEventModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.CORRELATED_EVENT;
            this.correlatedEvent = (CorrelatedEventModel) thing;
        } else if (CheckpointModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.TASK_CHECKPOINT;
            this.checkpoint = (CheckpointModel) thing;
        } else {
            throw new IllegalArgumentException("Unrecognized Output Topic Event thing: " + thing.getClass());
        }
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public PayloadCase getPayloadCase() {
        return this.payloadCase;
    }

    public WfRunModel getWfRun() {
        return this.wfRun;
    }

    public ExternalEventModel getExternalEvent() {
        return this.externalEvent;
    }

    public WorkflowEventModel getWorkflowEvent() {
        return this.workflowEvent;
    }

    public UserTaskRunModel getUserTaskRun() {
        return this.userTaskRun;
    }

    public VariableModel getVariable() {
        return this.variable;
    }

    public TaskRunModel getTaskRun() {
        return this.taskRun;
    }

    public CorrelatedEventModel getCorrelatedEvent() {
        return this.correlatedEvent;
    }

    public CheckpointModel getCheckpoint() {
        return this.checkpoint;
    }

    public void setTimestamp(final Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setPayloadCase(final PayloadCase payloadCase) {
        this.payloadCase = payloadCase;
    }

    public void setWfRun(final WfRunModel wfRun) {
        this.wfRun = wfRun;
    }

    public void setExternalEvent(final ExternalEventModel externalEvent) {
        this.externalEvent = externalEvent;
    }

    public void setWorkflowEvent(final WorkflowEventModel workflowEvent) {
        this.workflowEvent = workflowEvent;
    }

    public void setUserTaskRun(final UserTaskRunModel userTaskRun) {
        this.userTaskRun = userTaskRun;
    }

    public void setVariable(final VariableModel variable) {
        this.variable = variable;
    }

    public void setTaskRun(final TaskRunModel taskRun) {
        this.taskRun = taskRun;
    }

    public void setCorrelatedEvent(final CorrelatedEventModel correlatedEvent) {
        this.correlatedEvent = correlatedEvent;
    }

    public void setCheckpoint(final CheckpointModel checkpoint) {
        this.checkpoint = checkpoint;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof OutputTopicRecordModel)) return false;
        final OutputTopicRecordModel other = (OutputTopicRecordModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$timestamp = this.getTimestamp();
        final Object other$timestamp = other.getTimestamp();
        if (this$timestamp == null ? other$timestamp != null : !this$timestamp.equals(other$timestamp)) return false;
        final Object this$payloadCase = this.getPayloadCase();
        final Object other$payloadCase = other.getPayloadCase();
        if (this$payloadCase == null ? other$payloadCase != null : !this$payloadCase.equals(other$payloadCase))
            return false;
        final Object this$wfRun = this.getWfRun();
        final Object other$wfRun = other.getWfRun();
        if (this$wfRun == null ? other$wfRun != null : !this$wfRun.equals(other$wfRun)) return false;
        final Object this$externalEvent = this.getExternalEvent();
        final Object other$externalEvent = other.getExternalEvent();
        if (this$externalEvent == null ? other$externalEvent != null : !this$externalEvent.equals(other$externalEvent))
            return false;
        final Object this$workflowEvent = this.getWorkflowEvent();
        final Object other$workflowEvent = other.getWorkflowEvent();
        if (this$workflowEvent == null ? other$workflowEvent != null : !this$workflowEvent.equals(other$workflowEvent))
            return false;
        final Object this$userTaskRun = this.getUserTaskRun();
        final Object other$userTaskRun = other.getUserTaskRun();
        if (this$userTaskRun == null ? other$userTaskRun != null : !this$userTaskRun.equals(other$userTaskRun))
            return false;
        final Object this$variable = this.getVariable();
        final Object other$variable = other.getVariable();
        if (this$variable == null ? other$variable != null : !this$variable.equals(other$variable)) return false;
        final Object this$taskRun = this.getTaskRun();
        final Object other$taskRun = other.getTaskRun();
        if (this$taskRun == null ? other$taskRun != null : !this$taskRun.equals(other$taskRun)) return false;
        final Object this$correlatedEvent = this.getCorrelatedEvent();
        final Object other$correlatedEvent = other.getCorrelatedEvent();
        if (this$correlatedEvent == null
                ? other$correlatedEvent != null
                : !this$correlatedEvent.equals(other$correlatedEvent)) return false;
        final Object this$checkpoint = this.getCheckpoint();
        final Object other$checkpoint = other.getCheckpoint();
        if (this$checkpoint == null ? other$checkpoint != null : !this$checkpoint.equals(other$checkpoint))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof OutputTopicRecordModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $timestamp = this.getTimestamp();
        result = result * PRIME + ($timestamp == null ? 43 : $timestamp.hashCode());
        final Object $payloadCase = this.getPayloadCase();
        result = result * PRIME + ($payloadCase == null ? 43 : $payloadCase.hashCode());
        final Object $wfRun = this.getWfRun();
        result = result * PRIME + ($wfRun == null ? 43 : $wfRun.hashCode());
        final Object $externalEvent = this.getExternalEvent();
        result = result * PRIME + ($externalEvent == null ? 43 : $externalEvent.hashCode());
        final Object $workflowEvent = this.getWorkflowEvent();
        result = result * PRIME + ($workflowEvent == null ? 43 : $workflowEvent.hashCode());
        final Object $userTaskRun = this.getUserTaskRun();
        result = result * PRIME + ($userTaskRun == null ? 43 : $userTaskRun.hashCode());
        final Object $variable = this.getVariable();
        result = result * PRIME + ($variable == null ? 43 : $variable.hashCode());
        final Object $taskRun = this.getTaskRun();
        result = result * PRIME + ($taskRun == null ? 43 : $taskRun.hashCode());
        final Object $correlatedEvent = this.getCorrelatedEvent();
        result = result * PRIME + ($correlatedEvent == null ? 43 : $correlatedEvent.hashCode());
        final Object $checkpoint = this.getCheckpoint();
        result = result * PRIME + ($checkpoint == null ? 43 : $checkpoint.hashCode());
        return result;
    }
}
