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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
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
}
