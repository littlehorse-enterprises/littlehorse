package io.littlehorse.common.model.outputtopic;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.WorkflowEventUpdateRecord;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class WorkflowEventUpdateRecordModel extends LHSerializable<WorkflowEventUpdateRecord>
        implements GenericOutputTopicRecordModel {

    private WorkflowEventModel getable;
    private WfSpecIdModel wfSpecId;

    @Override
    public Class<WorkflowEventUpdateRecord> getProtoBaseClass() {
        return WorkflowEventUpdateRecord.class;
    }

    @Override
    public WorkflowEventUpdateRecord.Builder toProto() {
        WorkflowEventUpdateRecord.Builder result = WorkflowEventUpdateRecord.newBuilder();

        result.setWorkflowEvent(getable.toProto());
        result.setWfSpecId(wfSpecId.toProto());

        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        WorkflowEventUpdateRecord p = (WorkflowEventUpdateRecord) proto;

        this.getable = LHSerializable.fromProto(p.getWorkflowEvent(), WorkflowEventModel.class, ignored);
        this.wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, ignored);
    }

    @Override
    public String getPartitionKey() {
        return getable.getPartitionKey().get();
    }
}
