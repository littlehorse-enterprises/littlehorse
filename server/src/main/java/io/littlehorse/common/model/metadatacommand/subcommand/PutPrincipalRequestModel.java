package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.proto.Principal;
import io.littlehorse.common.proto.PutPrincipalRequest;
import io.littlehorse.common.proto.ServerACL;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import java.util.ArrayList;
import java.util.List;

public class PutPrincipalRequestModel extends MetadataSubCommand<PutPrincipalRequest> {

    private String id;
    private final List<String> tenantIds = new ArrayList<>();
    private String defaultTenantId;

    private final List<ServerACLModel> acls = new ArrayList<>();

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        PutPrincipalRequest putPrincipalCommand = (PutPrincipalRequest) proto;
        this.id = putPrincipalCommand.getId();
        this.tenantIds.addAll(putPrincipalCommand.getTenantIdList());
        this.defaultTenantId = putPrincipalCommand.getDefaultTenantId();
        for (ServerACL serverACL : putPrincipalCommand.getAclsList()) {
            acls.add(LHSerializable.fromProto(serverACL, ServerACLModel.class));
        }
    }

    @Override
    public PutPrincipalRequest.Builder toProto() {
        PutPrincipalRequest.Builder out = PutPrincipalRequest.newBuilder();
        out.setId(this.id);
        out.setDefaultTenantId(this.defaultTenantId);
        out.addAllTenantId(tenantIds);
        out.addAllAcls(this.acls.stream()
                .map(ServerACLModel::toProto)
                .map(ServerACL.Builder::build)
                .toList());
        return out;
    }

    @Override
    public Class<PutPrincipalRequest> getProtoBaseClass() {
        return PutPrincipalRequest.class;
    }

    @Override
    public Principal process(MetadataProcessorDAO dao, LHServerConfig config) {
        PrincipalModel principalModel = dao.get(new PrincipalIdModel(id));
        if (principalModel == null) {
            PrincipalModel toStore = new PrincipalModel();
            toStore.setId(id);
            toStore.setDefaultTenantId(defaultTenantId);
            dao.put(toStore);
        }
        return null;
    }

    @Override
    public boolean hasResponse() {
        return false;
    }
}
