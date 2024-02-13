package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.ClusterMetadataId;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.sdk.common.proto.TenantId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class TenantIdModel extends ClusterMetadataId<TenantId, Tenant, TenantModel> {

    private String id;

    public TenantIdModel() {}

    public TenantIdModel(final String id) {
        this.id = id;
    }

    @Override
    public TenantId.Builder toProto() {
        return TenantId.newBuilder().setId(id);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        TenantId tenantId = (TenantId) proto;
        this.id = tenantId.getId();
    }

    @Override
    public Class<TenantId> getProtoBaseClass() {
        return TenantId.class;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public void initFromString(String storeKey) {
        this.id = storeKey;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.TENANT;
    }
}
