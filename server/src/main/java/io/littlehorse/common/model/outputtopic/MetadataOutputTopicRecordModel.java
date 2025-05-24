package io.littlehorse.common.model.outputtopic;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.global.events.WorkflowEventDefModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.sdk.common.proto.MetadataOutputTopicRecord;
import io.littlehorse.sdk.common.proto.MetadataOutputTopicRecord.MetadataRecordCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class MetadataOutputTopicRecordModel extends LHSerializable<MetadataOutputTopicRecord> {

    private MetadataRecordCase recordCase;
    private WfSpecModel wfSpec;
    private TaskDefModel taskDef;
    private ExternalEventDefModel externalEventDef;
    private WorkflowEventDefModel workflowEventDef;
    private UserTaskDefModel userTaskDef;

    public MetadataOutputTopicRecordModel() {}

    public MetadataOutputTopicRecordModel(MetadataGetable<?> thing) {
        setSubRecord(thing);
    }

    @Override
    public Class<MetadataOutputTopicRecord> getProtoBaseClass() {
        return MetadataOutputTopicRecord.class;
    }

    @Override
    public MetadataOutputTopicRecord.Builder toProto() {
        MetadataOutputTopicRecord.Builder result = MetadataOutputTopicRecord.newBuilder();

        switch (recordCase) {
            case WF_SPEC:
                result.setWfSpec(wfSpec.toProto());
                break;
            case TASK_DEF:
                result.setTaskDef(taskDef.toProto());
                break;
            case USER_TASK_DEF:
                result.setUserTaskDef(userTaskDef.toProto());
                break;
            case EXTERNAL_EVENT_DEF:
                result.setExternalEventDef(externalEventDef.toProto());
                break;
            case WORKFLOW_EVENT_DEF:
                result.setWorkflowEventDef(workflowEventDef.toProto());
                break;
            case METADATARECORD_NOT_SET:
        }

        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        MetadataOutputTopicRecord p = (MetadataOutputTopicRecord) proto;
        recordCase = p.getMetadataRecordCase();
        switch (recordCase) {
            case WF_SPEC:
                this.wfSpec = LHSerializable.fromProto(p.getWfSpec(), WfSpecModel.class, ignored);
                break;
            case TASK_DEF:
                this.taskDef = LHSerializable.fromProto(p.getTaskDef(), TaskDefModel.class, ignored);
                break;
            case USER_TASK_DEF:
                this.userTaskDef = LHSerializable.fromProto(p.getUserTaskDef(), UserTaskDefModel.class, ignored);
                break;
            case EXTERNAL_EVENT_DEF:
                this.externalEventDef =
                        LHSerializable.fromProto(p.getExternalEventDef(), ExternalEventDefModel.class, ignored);
                break;
            case WORKFLOW_EVENT_DEF:
                this.workflowEventDef =
                        LHSerializable.fromProto(p.getWorkflowEventDef(), WorkflowEventDefModel.class, ignored);
                break;
            case METADATARECORD_NOT_SET:
        }
    }

    public MetadataGetable<?> getSubrecord() {
        switch (recordCase) {
            case WF_SPEC:
                return wfSpec;
            case TASK_DEF:
                return taskDef;
            case USER_TASK_DEF:
                return userTaskDef;
            case EXTERNAL_EVENT_DEF:
                return externalEventDef;
            case WORKFLOW_EVENT_DEF:
                return workflowEventDef;
            case METADATARECORD_NOT_SET:
        }
        throw new IllegalStateException();
    }

    public void setSubRecord(MetadataGetable<?> thing) {
        if (WfSpecModel.class.isAssignableFrom(thing.getClass())) {
            recordCase = MetadataRecordCase.WF_SPEC;
            this.wfSpec = (WfSpecModel) thing;
        } else if (TaskDefModel.class.isAssignableFrom(thing.getClass())) {
            recordCase = MetadataRecordCase.TASK_DEF;
            this.taskDef = (TaskDefModel) thing;
        } else if (UserTaskDefModel.class.isAssignableFrom(thing.getClass())) {
            recordCase = MetadataRecordCase.USER_TASK_DEF;
            this.userTaskDef = (UserTaskDefModel) thing;
        } else if (ExternalEventDefModel.class.isAssignableFrom(thing.getClass())) {
            recordCase = MetadataRecordCase.EXTERNAL_EVENT_DEF;
            this.externalEventDef = (ExternalEventDefModel) thing;
        } else if (WorkflowEventDefModel.class.isAssignableFrom(thing.getClass())) {
            recordCase = MetadataRecordCase.WORKFLOW_EVENT_DEF;
            this.workflowEventDef = (WorkflowEventDefModel) thing;
        } else {
            throw new IllegalStateException("Unknown type: " + thing.getClass());
        }
    }
}
