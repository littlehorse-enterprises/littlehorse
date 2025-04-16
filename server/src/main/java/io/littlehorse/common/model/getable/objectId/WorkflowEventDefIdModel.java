package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.events.WorkflowEventDefModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.WorkflowEventDef;
import io.littlehorse.sdk.common.proto.WorkflowEventDefId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class WorkflowEventDefIdModel extends MetadataId<WorkflowEventDefId, WorkflowEventDef, WorkflowEventDefModel> {
    private String name;

    public WorkflowEventDefIdModel() {}

    public WorkflowEventDefIdModel(String name) {
        this.name = name;
    }

    @Override
    public WorkflowEventDefId.Builder toProto() {
        return WorkflowEventDefId.newBuilder().setName(name);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        WorkflowEventDefId p = (WorkflowEventDefId) proto;
        this.name = p.getName();
    }

    @Override
    public Class<WorkflowEventDefId> getProtoBaseClass() {
        return WorkflowEventDefId.class;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void initFromString(String storeKey) {
        this.name = storeKey;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.WORKFLOW_EVENT_DEF;
    }
}
