package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.ServerSubCommand;
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
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutPrincipalRequestModel extends MetadataSubCommand<PutPrincipalRequest> implements ServerSubCommand {

    private String id;
    private final List<String> tenantIds = new ArrayList<>();
    private String defaultTenantId;

    private final List<ServerACLModel> acls = new ArrayList<>();
    private boolean overwrite;

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        PutPrincipalRequest putPrincipalCommand = (PutPrincipalRequest) proto;
        this.id = putPrincipalCommand.getId();
        this.tenantIds.addAll(putPrincipalCommand.getTenantIdList());
        this.defaultTenantId = putPrincipalCommand.getDefaultTenantId();
        for (ServerACL serverACL : putPrincipalCommand.getAclsList()) {
            acls.add(LHSerializable.fromProto(serverACL, ServerACLModel.class));
        }
        this.overwrite = putPrincipalCommand.getOverwrite();
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
        out.setOverwrite(overwrite);
        return out;
    }

    @Override
    public Class<PutPrincipalRequest> getProtoBaseClass() {
        return PutPrincipalRequest.class;
    }

    @Override
    public Principal process(MetadataProcessorDAO dao, LHServerConfig config) {
        PrincipalModel existingPrincipal = dao.get(new PrincipalIdModel(id));
        PrincipalModel toSave;
        if (existingPrincipal == null) {
            toSave = new PrincipalModel();
            toSave.setId(id);
            toSave.getAcls().addAll(acls);
        } else if (overwrite) {
            toSave = existingPrincipal;
            toSave.getAcls().clear();
            toSave.getAcls().addAll(acls);
        } else {
            throw new IllegalArgumentException("Trying to overwrite existing principal");
        }
        final String contextTenantId = dao.context().tenantId();
        final String principalTenantId = defaultTenantId == null ? contextTenantId : defaultTenantId;

        if (!toSave.isAdmin()) {
            List<String> adminPrincipalIds = dao.adminPrincipalIdsFor(principalTenantId).stream()
                    .filter(adminPrincipalId -> !Objects.equals(adminPrincipalId, id))
                    .toList();
            if (adminPrincipalIds.isEmpty()) {
                throw new IllegalArgumentException("At least one admin level principal is required");
            }
        }
        toSave.setDefaultTenantId(principalTenantId);
        toSave.getTenantIds().add(principalTenantId);
        dao.put(toSave);
        return toSave.toProto().build();
    }

    @Override
    public boolean hasResponse() {
        return true;
    }
}
