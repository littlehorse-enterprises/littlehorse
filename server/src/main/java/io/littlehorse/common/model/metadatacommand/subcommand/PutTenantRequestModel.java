package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.proto.PutTenantRequest;
import io.littlehorse.common.proto.Tenant;
import io.littlehorse.sdk.common.exception.LHSerdeError;

public class PutTenantRequestModel extends MetadataSubCommand<PutTenantRequest> implements ClusterLevelCommand {

    private String id;

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        PutTenantRequest putTenantRequest = (PutTenantRequest) proto;
        this.id = putTenantRequest.getId();
    }

    @Override
    public PutTenantRequest.Builder toProto() {
        return PutTenantRequest.newBuilder().setId(id);
    }

    @Override
    public Class<PutTenantRequest> getProtoBaseClass() {
        return PutTenantRequest.class;
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public Tenant process(MetadataProcessorDAO dao, LHServerConfig config) {
        if (dao.get(new TenantIdModel(id)) == null) {
            TenantModel toSave = new TenantModel(id);
            toSave.setCreatedAt(dao.getCommand().getTime());
            dao.put(toSave);
            return toSave.toProto().build();
        } else {
            throw new LHApiException(Status.ALREADY_EXISTS, "Tenant %s already exists".formatted(id));
        }
    }
}
