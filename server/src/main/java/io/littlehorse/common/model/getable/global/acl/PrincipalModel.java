package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.proto.ACLAction;
import io.littlehorse.common.proto.ACLResource;
import io.littlehorse.common.proto.Principal;
import io.littlehorse.common.proto.ServerACL;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
public class PrincipalModel extends GlobalGetable<Principal> {

    private String id;
    private final List<ServerACLModel> acls = new ArrayList<>();

    private List<String> tenantIds;

    public PrincipalModel() {}

    public PrincipalModel(final String id, final List<ServerACLModel> acls, final List<String> tenantIds) {
        this.id = id;
        this.acls.addAll(acls);
        this.tenantIds = tenantIds;
    }

    public static PrincipalModel anonymous() {
        List<ACLAction> allActions = List.of(ACLAction.ALL_ACTIONS);
        List<ACLResource> allResources = List.of(ACLResource.ALL);
        List<ServerACLModel> adminAcls = List.of(new ServerACLModel(allResources, allActions));
        return new PrincipalModel("anonymous", adminAcls, null);
    }

    public static PrincipalModel anonymousFor(TenantModel tenant) {
        if (tenant == null) {
            throw new LHApiException(Status.FAILED_PRECONDITION, "Tenant is required");
        }
        List<ACLAction> allActions = List.of(ACLAction.ALL_ACTIONS);
        List<ACLResource> allResources = List.of(ACLResource.ALL);
        List<ServerACLModel> adminAcls = List.of(new ServerACLModel(allResources, allActions));
        return new PrincipalModel("anonymous", adminAcls, null);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        Principal principal = (Principal) proto;
        this.id = principal.getId();
        for (ServerACL serverACL : principal.getAclsList()) {
            acls.add(LHSerializable.fromProto(serverACL, ServerACLModel.class));
        }
        this.id = principal.getId();
        this.tenantIds = principal.getTenantIdList();
    }

    @Override
    public Principal.Builder toProto() {
        Principal.Builder out = Principal.newBuilder();
        out.setId(this.id);
        out.addAllAcls(acls.stream()
                .map(ServerACLModel::toProto)
                .map(ServerACL.Builder::build)
                .toList());
        out.addAllTenantId(this.tenantIds);
        return out;
    }

    @Override
    public Class<Principal> getProtoBaseClass() {
        return Principal.class;
    }

    @Override
    public Date getCreatedAt() {
        return new Date();
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
            List<IndexedField> result = new ArrayList<>();
            for (String tenantId : tenantIds) {
                result.add(new IndexedField(key, tenantId, TagStorageType.LOCAL));
            }
            return result;
        }
        return List.of();
    }

    public boolean isAdmin() {
        return acls.stream().anyMatch(ServerACLModel::isAdmin);
    }
}
