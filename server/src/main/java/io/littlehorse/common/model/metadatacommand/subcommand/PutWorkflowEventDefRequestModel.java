package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.getable.global.events.WorkflowEventDefModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WorkflowEventDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;

public class PutWorkflowEventDefRequestModel extends MetadataSubCommand<PutWorkflowEventDefRequest>
        implements ClusterLevelCommand {
    private WorkflowEventDefIdModel id;
    private VariableType type;

    public PutWorkflowEventDefRequestModel() {
        // used by LHSerializable
    }

    public PutWorkflowEventDefRequestModel(WorkflowEventDefIdModel id, VariableType contentType) {
        this.id = id;
        this.type = contentType;
    }

    @Override
    public PutWorkflowEventDefRequest.Builder toProto() {
        return PutWorkflowEventDefRequest.newBuilder().setId(id.toProto()).setType(type);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PutWorkflowEventDefRequest p = (PutWorkflowEventDefRequest) proto;
        this.id = LHSerializable.fromProto(p.getId(), WorkflowEventDefIdModel.class, context);
        this.type = p.getType();
    }

    @Override
    public Class<PutWorkflowEventDefRequest> getProtoBaseClass() {
        return PutWorkflowEventDefRequest.class;
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public WorkflowEventDef process(MetadataCommandExecution executionContext) {
        WorkflowEventDefModel eventDef = new WorkflowEventDefModel(id, type);
        executionContext.metadataManager().put(eventDef);
        return eventDef.toProto().build();
    }
}
