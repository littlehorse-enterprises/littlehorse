package io.littlehorse.common.model.outputtopic;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.littlehorse.common.LHSerializable;
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
    private WfRunUpdateRecordModel wfRunUpdate;
    private ExternalEventUpdateRecordModel externalEventUpdate;
    private WorkflowEventUpdateRecordModel workflowEventUpdate;
    private UserTaskRunUpdateRecordModel userTaskRunUpdate;
    private VariableUpdateRecordModel variableUpdate;
    private TaskRunExecutedRecordModel taskRunUpdate;

    public OutputTopicRecordModel() {
        this.timestamp = LHLibUtil.fromDate(new Date());
    }

    public OutputTopicRecordModel(GenericOutputTopicRecordModel thing) {
        this();
        setThing(thing);
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
                out.setWfRun(wfRunUpdate.toProto());
                break;
            case USER_TASK_RUN:
                out.setUserTaskRun(userTaskRunUpdate.toProto());
                break;
            case EXTERNAL_EVENT:
                out.setExternalEvent(externalEventUpdate.toProto());
                break;
            case TASK_RUN_EXECUTED:
                out.setTaskRunExecuted(taskRunUpdate.toProto());
                break;
            case WORKFLOW_EVENT:
                out.setWorkflowEvent(workflowEventUpdate.toProto());
                break;
            case VARIABLE_UPDATE:
                out.setVariableUpdate(variableUpdate.toProto());
                break;
            case PAYLOAD_NOT_SET:
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        OutputTopicRecord p = (OutputTopicRecord) proto;

        timestamp = p.getTimestamp();

        switch (p.getPayloadCase()) {
            case WF_RUN:
                payloadCase = PayloadCase.WF_RUN;
                wfRunUpdate = LHSerializable.fromProto(p.getWfRun(), WfRunUpdateRecordModel.class, ignored);
                break;
            case USER_TASK_RUN:
                payloadCase = PayloadCase.USER_TASK_RUN;
                userTaskRunUpdate =
                        LHSerializable.fromProto(p.getUserTaskRun(), UserTaskRunUpdateRecordModel.class, ignored);
                break;
            case EXTERNAL_EVENT:
                payloadCase = PayloadCase.EXTERNAL_EVENT;
                externalEventUpdate =
                        LHSerializable.fromProto(p.getExternalEvent(), ExternalEventUpdateRecordModel.class, ignored);
                break;
            case TASK_RUN_EXECUTED:
                payloadCase = PayloadCase.TASK_RUN_EXECUTED;
                taskRunUpdate =
                        LHSerializable.fromProto(p.getTaskRunExecuted(), TaskRunExecutedRecordModel.class, ignored);
                break;
            case WORKFLOW_EVENT:
                payloadCase = PayloadCase.WORKFLOW_EVENT;
                workflowEventUpdate =
                        LHSerializable.fromProto(p.getWorkflowEvent(), WorkflowEventUpdateRecordModel.class, ignored);
                break;
            case VARIABLE_UPDATE:
                payloadCase = PayloadCase.VARIABLE_UPDATE;
                variableUpdate =
                        LHSerializable.fromProto(p.getVariableUpdate(), VariableUpdateRecordModel.class, ignored);
                break;
            case PAYLOAD_NOT_SET:
        }
    }

    public String getPartitionKey() {
        return getSubrecord().getPartitionKey();
    }

    public GenericOutputTopicRecordModel getSubrecord() {
        switch (payloadCase) {
            case WF_RUN:
                return wfRunUpdate;
            case TASK_RUN_EXECUTED:
                return taskRunUpdate;
            case EXTERNAL_EVENT:
                return externalEventUpdate;
            case USER_TASK_RUN:
                return userTaskRunUpdate;
            case WORKFLOW_EVENT:
                return workflowEventUpdate;
            case VARIABLE_UPDATE:
                return variableUpdate;
            case PAYLOAD_NOT_SET:
        }
        throw new IllegalStateException("Forgot to add new output topic record type here");
    }

    public void setThing(GenericOutputTopicRecordModel thing) {
        if (thing == null) {
            throw new IllegalArgumentException();
        }

        if (WfRunUpdateRecordModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.WF_RUN;
            this.wfRunUpdate = (WfRunUpdateRecordModel) thing;
        } else if (UserTaskRunUpdateRecordModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.USER_TASK_RUN;
            this.userTaskRunUpdate = (UserTaskRunUpdateRecordModel) thing;
        } else if (ExternalEventUpdateRecordModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.EXTERNAL_EVENT;
            this.externalEventUpdate = (ExternalEventUpdateRecordModel) thing;
        } else if (TaskRunExecutedRecordModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.TASK_RUN_EXECUTED;
            this.taskRunUpdate = (TaskRunExecutedRecordModel) thing;
        } else if (WorkflowEventUpdateRecordModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.WORKFLOW_EVENT;
            this.workflowEventUpdate = (WorkflowEventUpdateRecordModel) thing;
        } else if (VariableUpdateRecordModel.class.isAssignableFrom(thing.getClass())) {
            this.payloadCase = PayloadCase.VARIABLE_UPDATE;
            this.variableUpdate = (VariableUpdateRecordModel) thing;
        } else {
            throw new IllegalArgumentException("Unrecognized Output Topic Event thing: " + thing.getClass());
        }
    }
}
