package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLsModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.proto.Principal;
import io.littlehorse.common.proto.PutPrincipalRequest;
import io.littlehorse.common.proto.ServerACLs;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutPrincipalRequestModel extends MetadataSubCommand<PutPrincipalRequest> implements ClusterLevelCommand {

    private String id;
    private Map<String, ServerACLsModel> perTenantAcls = new HashMap<>();
    private ServerACLsModel globalAcls;

    private boolean overwrite;

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        PutPrincipalRequest p = (PutPrincipalRequest) proto;
        this.id = p.getId();
        this.globalAcls = LHSerializable.fromProto(p.getGlobalAcls(), ServerACLsModel.class);

        for (Map.Entry<String, ServerACLs> tenantAcls : p.getPerTenantAclsMap().entrySet()) {
            perTenantAcls.put(
                    tenantAcls.getKey(), LHSerializable.fromProto(tenantAcls.getValue(), ServerACLsModel.class));
        }
        this.overwrite = p.getOverwrite();
    }

    @Override
    public PutPrincipalRequest.Builder toProto() {
        PutPrincipalRequest.Builder out = PutPrincipalRequest.newBuilder();
        out.setId(this.id);
        for (Map.Entry<String, ServerACLsModel> perTenantACL : perTenantAcls.entrySet()) {
            out.putPerTenantAcls(
                    perTenantACL.getKey(), perTenantACL.getValue().toProto().build());
        }
        out.setGlobalAcls(globalAcls.toProto());
        out.setOverwrite(overwrite);
        return out;
    }

    @Override
    public Class<PutPrincipalRequest> getProtoBaseClass() {
        return PutPrincipalRequest.class;
    }

    @Override
    public Principal process(MetadataProcessorDAO dao, LHServerConfig config) {
        PrincipalModel oldPrincipal = dao.get(new PrincipalIdModel(id));

        PrincipalModel toSave = new PrincipalModel();
        if (oldPrincipal != null) {
            if (!overwrite) {
                throw new LHApiException(
                        Status.ALREADY_EXISTS,
                        "Must set overwrite == true to modify existing Principal %s".formatted(id));
            }

            // Here, are overwriting an old. Must ensure that we don't lock everyone out
            // of the cluster: ensure that after this request is processed, there should
            // still be a Principal with Cluster Admin privileges.
            ensureThatThereIsStillAnAdminPrincipal(oldPrincipal, dao);

            toSave.setCreatedAt(oldPrincipal.getCreatedAt());
        }

        if (perTenantAcls.isEmpty()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Must provide list of tenants");
        }

        for (Map.Entry<String, ServerACLsModel> perTenantAcl : perTenantAcls.entrySet()) {
            String tenantId = perTenantAcl.getKey();
            ServerACLsModel acls = perTenantAcl.getValue();
            TenantModel tenant = dao.getTenant(tenantId);
            if (tenant == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "PutPrincipalRequest specified ACL's for a nonexistent tenant: %s".formatted(tenantId));
            }
            toSave.getPerTenantAcls().put(tenantId, acls);
        }

        toSave.setGlobalAcls(globalAcls);

        dao.put(toSave);
        return toSave.toProto().build();
    }

    private void ensureThatThereIsStillAnAdminPrincipal(PrincipalModel old, MetadataProcessorDAO dao) {
        if (!old.isAdmin()) {
            // If the old isn't admin, then we aren't taking away admin privileges, so we
            // don't need to worry.
            return;
        }

        if (globalAcls.getAcls().stream().anyMatch(acl -> acl.isAdmin())) {
            // then the resulting principal after this request is Admin, so we don't
            // need to worry about losing the Last Admin.
            return;
        }

        // At this point, we know that:
        // 1. There used to be an Admin Principal with this ID
        // 2. After this request, that Principal will no longer be Admin
        //
        // Therefore, we need to check that there is still some other Admin Principal
        // somewhere before we remove it.
        if (!dao.adminPrincipalIds().stream()
                .anyMatch(adminPrincipalId -> !Objects.equals(adminPrincipalId, this.id))) {
            throw new LHApiException(
                    Status.FAILED_PRECONDITION,
                    "Cannot remove admin privileges from Principal %s: %s is the last Admin left.".formatted(id, id));
        }
    }

    @Override
    public boolean hasResponse() {
        return true;
    }
}
