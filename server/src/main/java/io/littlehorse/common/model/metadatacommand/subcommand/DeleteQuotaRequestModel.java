package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.objectId.QuotaIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.DeleteQuotaRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;

public class DeleteQuotaRequestModel extends MetadataSubCommand<DeleteQuotaRequest> implements ClusterLevelCommand {

    private QuotaIdModel id;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        DeleteQuotaRequest request = (DeleteQuotaRequest) proto;
        if (!request.hasId()) {
            throw new LHSerdeException("DeleteQuotaRequest is missing id");
        }
        id = LHSerializable.fromProto(request.getId(), QuotaIdModel.class, context);
    }

    @Override
    public DeleteQuotaRequest.Builder toProto() {
        return DeleteQuotaRequest.newBuilder().setId(id.toProto());
    }

    @Override
    public Class<DeleteQuotaRequest> getProtoBaseClass() {
        return DeleteQuotaRequest.class;
    }

    @Override
    public Empty process(MetadataProcessorContext context) {
        PrincipalModel caller = context.service().getPrincipal(context.authorization().principalId());
        if (!caller.canEditQuotas()) {
            throw new LHApiException(
                    Status.PERMISSION_DENIED,
                    String.format(
                            "Missing permission %s over resource %s.",
                            ACLAction.WRITE_METADATA, ACLResource.ACL_QUOTA));
        }

        context.metadataManager().delete(id);
        return Empty.getDefaultInstance();
    }
}
