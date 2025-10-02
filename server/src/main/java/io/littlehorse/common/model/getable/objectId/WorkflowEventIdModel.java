package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.proto.WorkflowEventId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;

@Getter
public class WorkflowEventIdModel extends CoreObjectId<WorkflowEventId, WorkflowEvent, WorkflowEventModel> {
    private WfRunIdModel wfRunId;
    private WorkflowEventDefIdModel workflowEventDefId;
    private int id;

    public WorkflowEventIdModel() {}

    public WorkflowEventIdModel(WfRunIdModel wfRunId, WorkflowEventDefIdModel workflowEventDefId, int id) {
        this.wfRunId = wfRunId;
        this.workflowEventDefId = workflowEventDefId;
        this.id = id;
    }

    @Override
    public WorkflowEventId.Builder toProto() {
        return WorkflowEventId.newBuilder()
                .setWfRunId(wfRunId.toProto())
                .setWorkflowEventDefId(workflowEventDefId.toProto())
                .setNumber(id);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        WorkflowEventId p = (WorkflowEventId) proto;
        this.wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        this.workflowEventDefId =
                LHSerializable.fromProto(p.getWorkflowEventDefId(), WorkflowEventDefIdModel.class, context);
        this.id = p.getNumber();
    }

    @Override
    public Class<WorkflowEventId> getProtoBaseClass() {
        return WorkflowEventId.class;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfRunId.toString(), workflowEventDefId.toString(), String.valueOf(id));
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        this.wfRunId = (WfRunIdModel) WfRunIdModel.fromString(split[0], WfRunIdModel.class);
        this.workflowEventDefId =
                (WorkflowEventDefIdModel) WorkflowEventDefIdModel.fromString(split[1], WorkflowEventDefIdModel.class);
        this.id = Integer.parseInt(split[2]);
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.WORKFLOW_EVENT;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return wfRunId.getPartitionKey();
    }
}
