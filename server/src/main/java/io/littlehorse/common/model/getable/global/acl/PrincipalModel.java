package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.ClusterMetadataGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@Slf4j
public class PrincipalModel extends ClusterMetadataGetable<Principal> {

    private PrincipalIdModel id;
    private Map<String, ServerACLsModel> perTenantAcls = new HashMap<>();
    private ServerACLsModel globalAcls;
    private Date createdAt;

    public PrincipalModel() {}

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        Principal principal = (Principal) proto;
        this.id = LHSerializable.fromProto(principal.getId(), PrincipalIdModel.class, context);
        this.globalAcls = LHSerializable.fromProto(principal.getGlobalAcls(), ServerACLsModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(principal.getCreatedAt());

        for (Map.Entry<String, ServerACLs> tenantAcls :
                principal.getPerTenantAclsMap().entrySet()) {
            perTenantAcls.put(
                    tenantAcls.getKey(),
                    LHSerializable.fromProto(tenantAcls.getValue(), ServerACLsModel.class, context));
        }
    }

    @Override
    public Principal.Builder toProto() {

        Principal.Builder out =
                Principal.newBuilder().setId(this.id.toProto()).setCreatedAt(LHUtil.fromDate(getCreatedAt()));

        if (globalAcls != null) {
            out.setGlobalAcls(globalAcls.toProto());
        }

        for (Map.Entry<String, ServerACLsModel> perTenantACL : perTenantAcls.entrySet()) {
            String tenantId = perTenantACL.getKey();
            ServerACLs acls = perTenantACL.getValue().toProto().build();
            out.putPerTenantAcls(tenantId, acls);
        }

        return out;
    }

    @Override
    public Class<Principal> getProtoBaseClass() {
        return Principal.class;
    }

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(
                new GetableIndex<>(
                        List.of(Pair.of("tenantId", GetableIndex.ValueType.DYNAMIC)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(
                                Pair.of("tenantId", GetableIndex.ValueType.DYNAMIC),
                                Pair.of("isAdmin", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(Pair.of("isAdmin", GetableIndex.ValueType.SINGLE)), Optional.of(TagStorageType.LOCAL)));
    }

    @Override
    public ObjectIdModel<PrincipalId, Principal, PrincipalModel> getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        if (key.equals("isAdmin")) {
            return List.of(new IndexedField(key, this.isAdmin(), TagStorageType.LOCAL));
        } else if (key.equals("tenantId")) {
            return perTenantAcls.keySet().stream()
                    .map(tenantId -> new IndexedField(key, tenantId, TagStorageType.LOCAL))
                    .toList();
        }

        log.warn("Unrecognized index key for PrincipalModel: {}", key);
        return List.of();
    }

    public boolean isAdmin() {
        // to be admin, you need:
        // - a global ACL with ALL_ACTIONS over ACL_ALL
        if (globalAcls == null) {
            return false;
        }
        return globalAcls.getAcls().stream().anyMatch(ServerACLModel::isAdmin);
    }

    /**
     * Returns whether this Principal has permissions to edit (Put or Delete) Principals in the specified
     * tenants.
     * @param tenants are the affected tenants.
     * @return true if this Principal can modify Principals in the specified tenants.
     */
    public boolean hasPermissionToEditPrincipalsIn(List<TenantIdModel> tenants) {
        if (isAdmin()) return true;

        boolean allowsGlobalPrincipalCreation =
                this.getGlobalAcls().allows(ACLResource.ACL_PRINCIPAL, ACLAction.WRITE_METADATA);

        Predicate<TenantIdModel> canEditPrincipalsInTenant = (tenantId) -> {
            ServerACLsModel aclsForTenant = perTenantAcls.get(tenantId.getId());
            return aclsForTenant != null && aclsForTenant.allows(ACLResource.ACL_PRINCIPAL, ACLAction.WRITE_METADATA);
        };

        return allowsGlobalPrincipalCreation || tenants.stream().allMatch(canEditPrincipalsInTenant);
    }

    /**
     * Returns the List of all TenantId's that this Principal has effect over. Note that this is only useful
     * if this is not an admin Principal.
     */
    public List<TenantIdModel> getTenantsThatPrincipalHasPermissionOver() {
        return perTenantAcls.keySet().stream().map(TenantIdModel::new).toList();
    }

    public boolean canCreateTenants() {
        return globalAcls.allows(ACLResource.ACL_TENANT, ACLAction.WRITE_METADATA);
    }
}
