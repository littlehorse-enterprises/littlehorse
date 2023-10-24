package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.ServerSubCommand;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.proto.DeletePrincipalRequest;
import io.littlehorse.sdk.common.exception.LHSerdeError;

public class DeletePrincipalRequestModel extends MetadataSubCommand<DeletePrincipalRequest>
        implements ServerSubCommand {

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
    public Message process(MetadataProcessorDAO dao, LHServerConfig config) {
        dao.delete(new PrincipalIdModel(id));
        return null;
    }
}
