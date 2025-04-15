package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLsModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.PutPrincipalRequest;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
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
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        PutPrincipalRequest p = (PutPrincipalRequest) proto;
        this.id = p.getId();
        this.globalAcls = LHSerializable.fromProto(p.getGlobalAcls(), ServerACLsModel.class, context);

        for (Map.Entry<String, ServerACLs> tenantAcls : p.getPerTenantAclsMap().entrySet()) {
            perTenantAcls.put(
                    tenantAcls.getKey(),
                    LHSerializable.fromProto(tenantAcls.getValue(), ServerACLsModel.class, context));
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
    public Principal process(MetadataCommandExecution context) {
        MetadataManager metadataManager = context.metadataManager();
        PrincipalModel oldPrincipal = context.service().getPrincipal(new PrincipalIdModel(id));
        PrincipalModel requester =
                context.service().getPrincipal(context.authorization().principalId());
        PrincipalModel toSave = new PrincipalModel();
        toSave.setId(new PrincipalIdModel(id));

        char[] disallowedCharacters = {'/', '\\'};
        // Check if the ID contains any disallowed characters
        for (char disallowedChar : disallowedCharacters) {
            if (id.contains(String.valueOf(disallowedChar))) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Principal ID cannot contain slashes or backslashes.");
            }
        }

        if (oldPrincipal != null) {
            if (!overwrite) {
                throw new LHApiException(
                        Status.ALREADY_EXISTS,
                        "Must set overwrite == true to modify existing Principal %s".formatted(id));
            }

            // Here, are overwriting an old. Must ensure that we don't lock everyone out
            // of the cluster: ensure that after this request is processed, there should
            // still be a Principal with Cluster Admin privileges.
            ensureThatThereIsStillAnAdminPrincipal(oldPrincipal, context);

            toSave.setCreatedAt(oldPrincipal.getCreatedAt());
        }
        boolean canWriteAdminPrincipals = requester.isAdmin();
        if (!globalAcls.getAcls().isEmpty() && !canWriteAdminPrincipals) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Only admin users can create a principal with global privileges");
        }

        if (!requester.hasPermissionToEditPrincipals()) {
            throw new LHApiException(
                    Status.PERMISSION_DENIED,
                    String.format(
                            "Missing permission %s over resource %s.",
                            ACLAction.WRITE_METADATA, ACLResource.ACL_PRINCIPAL));
        }

        validateIfPerTenantACLHasClusterScopedResources();

        for (Map.Entry<String, ServerACLsModel> perTenantAcl : perTenantAcls.entrySet()) {
            TenantIdModel tenantId = new TenantIdModel(perTenantAcl.getKey());
            ServerACLsModel acls = perTenantAcl.getValue();
            TenantModel tenant = metadataManager.get(tenantId);
            if (tenant == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "PutPrincipalRequest specified ACL's for a nonexistent tenant: %s".formatted(tenantId));
            }
            toSave.getPerTenantAcls().put(perTenantAcl.getKey(), acls);
        }

        toSave.setGlobalAcls(globalAcls);

        metadataManager.put(toSave);
        return toSave.toProto().build();
    }

    /**
     * Validates whether the perTenantACLs contain any cluster-scoped resources like Tenants or Principals.
     */
    private void validateIfPerTenantACLHasClusterScopedResources() {
        if (hasClusterScopedResource()) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "PutPrincipalRequest does not allow Per-Tenant ACLs containing permissions over Tenants or Principals.");
        }
    }

    private boolean hasClusterScopedResource() {
        return !perTenantAcls.isEmpty()
                && perTenantAcls.values().stream().anyMatch(mappedACL -> mappedACL.getAcls().stream()
                        .anyMatch(actualACL -> actualACL.getResources().stream()
                                .anyMatch(aclResource -> aclResource.equals(ACLResource.ACL_TENANT)
                                        || aclResource.equals(ACLResource.ACL_PRINCIPAL))));
    }

    private void ensureThatThereIsStillAnAdminPrincipal(PrincipalModel old, MetadataCommandExecution context) {
        if (!old.isAdmin()) {
            // If the old isn't admin, then we aren't taking away admin privileges, so we
            // don't need to worry.
            return;
        }

        if (globalAcls.getAcls().stream().anyMatch(ServerACLModel::isAdmin)) {
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
        if (!context.service().adminPrincipalIds().stream()
                .anyMatch(adminPrincipalId -> !Objects.equals(adminPrincipalId.getId(), this.id))) {
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
