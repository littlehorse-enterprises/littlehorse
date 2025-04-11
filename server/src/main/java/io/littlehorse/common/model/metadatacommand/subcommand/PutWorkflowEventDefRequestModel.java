package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.getable.global.events.WorkflowEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.WorkflowEventDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import lombok.Getter;

@Getter
public class PutWorkflowEventDefRequestModel extends MetadataSubCommand<PutWorkflowEventDefRequest>
        implements ClusterLevelCommand {

    private String name;
    private ReturnTypeModel contentType;

    public PutWorkflowEventDefRequestModel() {
        // used by LHSerializable
    }

    public PutWorkflowEventDefRequestModel(String name, ReturnTypeModel contentType) {
        this.name = name;
        this.contentType = contentType;
    }

    @Override
    public PutWorkflowEventDefRequest.Builder toProto() {
        return PutWorkflowEventDefRequest.newBuilder().setName(name).setContentType(contentType.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PutWorkflowEventDefRequest p = (PutWorkflowEventDefRequest) proto;
        name = p.getName();
        this.contentType = LHSerializable.fromProto(p.getContentType(), ReturnTypeModel.class, context);
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
            if (!old.getContentType().equals(contentType)) {
                throw new LHApiException(
                        Status.ALREADY_EXISTS,
                        "WorkflowEventDef with name %s already exists with different type!".formatted(name));
            }
            return old.toProto().build();
        }

        WorkflowEventDefModel newEventDef = new WorkflowEventDefModel(id, contentType);
        executionContext.metadataManager().put(newEventDef);
        return newEventDef.toProto().build();
    }
}
