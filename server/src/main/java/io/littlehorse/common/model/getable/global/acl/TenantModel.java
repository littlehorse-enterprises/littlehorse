package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.proto.Tenant;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TenantModel extends GlobalGetable<Tenant> {

    private String id;

    public TenantModel() {}

    public TenantModel(final String id) {
        this.id = id;
    }

    @Override
    public Tenant.Builder toProto() {
        return Tenant.newBuilder().setId(id);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        Tenant tenant = (Tenant) proto;
        this.id = tenant.getId();
    }

    @Override
    public Class<Tenant> getProtoBaseClass() {
        return Tenant.class;
    }

    @Override
    public Date getCreatedAt() {
        return new Date();
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public TenantIdModel getObjectId() {
        return new TenantIdModel(id);
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return null;
    }
}
