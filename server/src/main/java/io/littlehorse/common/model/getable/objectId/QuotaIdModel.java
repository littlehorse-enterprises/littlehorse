package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.ClusterMetadataId;
import io.littlehorse.common.model.getable.global.acl.QuotaModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Quota;
import io.littlehorse.sdk.common.proto.QuotaId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuotaIdModel extends ClusterMetadataId<QuotaId, Quota, QuotaModel> {

    private TenantIdModel tenant;
    private PrincipalIdModel principal;

    public QuotaIdModel() {}

    public QuotaIdModel(TenantIdModel tenant) {
        this(tenant, null);
    }

    public QuotaIdModel(TenantIdModel tenant, PrincipalIdModel principal) {
        this.tenant = tenant;
        this.principal = principal;
    }

    @Override
    public QuotaId.Builder toProto() {
        QuotaId.Builder builder = QuotaId.newBuilder().setTenant(tenant.toProto());
        if (principal != null) {
            builder.setPrincipal(principal.toProto());
        }
        return builder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        QuotaId quotaId = (QuotaId) proto;
        tenant = LHSerializable.fromProto(quotaId.getTenant(), TenantIdModel.class, context);
        if (quotaId.hasPrincipal()) {
            principal = LHSerializable.fromProto(quotaId.getPrincipal(), PrincipalIdModel.class, context);
        }
    }

    @Override
    public Class<QuotaId> getProtoBaseClass() {
        return QuotaId.class;
    }

    @Override
    public String toString() {
        if (principal == null) {
            return tenant.toString();
        }
        return tenant + "/" + principal;
    }

    @Override
    public void initFromString(String storeKey) {
        String[] parts = storeKey.split("/", 2);
        tenant = new TenantIdModel(parts[0]);
        if (parts.length > 1 && !parts[1].isBlank()) {
            principal = new PrincipalIdModel(parts[1]);
        }
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.QUOTA;
    }

    public boolean hasPrincipal() {
        return principal != null;
    }
}
