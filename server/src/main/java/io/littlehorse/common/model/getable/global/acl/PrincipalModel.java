package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.proto.Principal;
import io.littlehorse.common.proto.ServerACLs;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@Slf4j
public class PrincipalModel extends GlobalGetable<Principal> {

    private String id;
    private Map<String, ServerACLsModel> perTenantAcls = new HashMap<>();
    private ServerACLsModel globalAcls;
    private Date createdAt;

    public PrincipalModel() {}

    // public PrincipalModel(final String id, final List<ServerACLModel> acls, final List<String> tenantIds) {
    //     this.id = id;
    //     this.acls.addAll(acls);
    //     this.tenantIds = tenantIds;
    // }

    // @Deprecated(forRemoval = true)
    // public static PrincipalModel anonymous() {
    //     List<ACLAction> allActions = List.of(ACLAction.ALL_ACTIONS);
    //     List<ACLResource> allResources = List.of(ACLResource.ALL);
    //     List<ServerACLModel> adminAcls = List.of(new ServerACLModel(allResources, allActions));
    //     return new PrincipalModel("anonymous", adminAcls, null);
    // }

    // @Deprecated(forRemoval = true)
    // public static PrincipalModel anonymousFor(TenantModel tenant) {
    //     if (tenant == null) {
    //         throw new LHApiException(Status.FAILED_PRECONDITION, "Tenant is required");
    //     }
    //     List<ACLAction> allActions = List.of(ACLAction.ALL_ACTIONS);
    //     List<ACLResource> allResources = List.of(ACLResource.ALL);
    //     List<ServerACLModel> adminAcls = List.of(new ServerACLModel(allResources, allActions));
    //     return new PrincipalModel("anonymous", adminAcls, null);
    // }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        Principal principal = (Principal) proto;
        this.id = principal.getId();
        this.globalAcls = LHSerializable.fromProto(principal.getGlobalAcls(), ServerACLsModel.class);
        this.createdAt = LHUtil.fromProtoTs(principal.getCreatedAt());

        for (Map.Entry<String, ServerACLs> tenantAcls :
                principal.getPerTenantAclsMap().entrySet()) {
            perTenantAcls.put(
                    tenantAcls.getKey(), LHSerializable.fromProto(tenantAcls.getValue(), ServerACLsModel.class));
        }
    }

    @Override
    public Principal.Builder toProto() {
        Principal.Builder out = Principal.newBuilder()
                .setId(this.id)
                .setGlobalAcls(globalAcls.toProto())
                .setCreatedAt(LHUtil.fromDate(getCreatedAt()));

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
        return List.of(new GetableIndex<>(
                List.of(
                        Pair.of("tenantId", GetableIndex.ValueType.DYNAMIC),
                        Pair.of("isAdmin", GetableIndex.ValueType.SINGLE)),
                Optional.of(TagStorageType.LOCAL)));
    }

    @Override
    public ObjectIdModel<PrincipalId, Principal, PrincipalModel> getObjectId() {
        return new PrincipalIdModel(id);
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

        return globalAcls.getAcls().stream().anyMatch(serverAcl -> serverAcl.isAdmin());
    }
}
