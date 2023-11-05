package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.proto.DeletePrincipalRequest;
import io.littlehorse.sdk.common.exception.LHSerdeError;

public class DeletePrincipalRequestModel extends MetadataSubCommand<DeletePrincipalRequest>
        implements ClusterLevelCommand {

    private String id;

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        DeletePrincipalRequest deleteRequest = (DeletePrincipalRequest) proto;
        this.id = deleteRequest.getId();
    }

    @Override
    public DeletePrincipalRequest.Builder toProto() {
        return DeletePrincipalRequest.newBuilder().setId(id);
    }

    @Override
    public Class<DeletePrincipalRequest> getProtoBaseClass() {
        return DeletePrincipalRequest.class;
    }

    @Override
    public boolean hasResponse() {
        return false;
    }

    @Override
    public Empty process(MetadataProcessorDAO dao, LHServerConfig config) {
        if (id.equals(LHConstants.ANONYMOUS_PRINCIPAL)) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Deleting the anonymous principal is not allowed. However, "
                            + "you can remove its permissions via the PutPrincipal request.");
        }

        dao.delete(new PrincipalIdModel(id));
        return Empty.getDefaultInstance();
    }
}
