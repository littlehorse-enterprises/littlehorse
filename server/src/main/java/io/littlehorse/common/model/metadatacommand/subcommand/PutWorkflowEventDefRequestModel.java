package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.getable.global.events.WorkflowEventDefModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WorkflowEventDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import lombok.Getter;

@Getter
public class PutWorkflowEventDefRequestModel extends MetadataSubCommand<PutWorkflowEventDefRequest>
        implements ClusterLevelCommand {

    private String name;
    private VariableType type;

    public PutWorkflowEventDefRequestModel() {
        // used by LHSerializable
    }

    public PutWorkflowEventDefRequestModel(String name, VariableType contentType) {
        this.name = name;
        this.type = contentType;
    }

    @Override
    public PutWorkflowEventDefRequest.Builder toProto() {
        return PutWorkflowEventDefRequest.newBuilder().setName(name).setType(type);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        PutWorkflowEventDefRequest p = (PutWorkflowEventDefRequest) proto;
        name = p.getName();
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
        WorkflowEventDefIdModel id = new WorkflowEventDefIdModel(name);

        WorkflowEventDefModel old = executionContext.metadataManager().get(id);
        if (old != null) {
            if (old.getType() != type) {
                throw new LHApiException(
                        Status.ALREADY_EXISTS,
                        "WorkflowEventDef with name %s already exists with different type!".formatted(name));
            }
            return old.toProto().build();
        }

        WorkflowEventDefModel newEventDef = new WorkflowEventDefModel(id, type);
        executionContext.metadataManager().put(newEventDef);
        return newEventDef.toProto().build();
    }
}
