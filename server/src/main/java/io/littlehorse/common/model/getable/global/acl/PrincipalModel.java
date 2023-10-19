package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.proto.Principal;
import io.littlehorse.common.proto.ServerACL;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrincipalModel extends GlobalGetable<Principal> {

    private String id;
    private final List<ServerACLModel> acls = new ArrayList<>();

    private final List<String> tenantIds = new ArrayList<>();
    private String defaultTenantId;

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        Principal principal = (Principal) proto;
        this.id = principal.getId();
        for (ServerACL serverACL : principal.getAclsList()) {
            acls.add(LHSerializable.fromProto(serverACL, ServerACLModel.class));
        }
        this.tenantIds.addAll(principal.getTenantIdsList());
        this.defaultTenantId = principal.getDefaultTenantId();
    }

    @Override
    public Principal.Builder toProto() {
        Principal.Builder out = Principal.newBuilder();
        out.setId(this.id);
        out.addAllAcls(acls.stream()
                .map(ServerACLModel::toProto)
                .map(ServerACL.Builder::build)
                .toList());
        out.addAllTenantIds(this.tenantIds);
        out.setDefaultTenantId(this.defaultTenantId);
        return out;
    }

    @Override
    public Class<Principal> getProtoBaseClass() {
        return Principal.class;
    }

    @Override
    public Date getCreatedAt() {
        return null;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return new ArrayList<>();
    }

    @Override
    public ObjectIdModel<PrincipalId, Principal, PrincipalModel> getObjectId() {
        return new PrincipalIdModel(id);
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return null;
    }
}
