package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.ClusterMetadataGetable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.OutputTopicConfigModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class TenantModel extends ClusterMetadataGetable<Tenant> {

    private TenantIdModel id;
    private String name;
    private Date createdAt;
    private OutputTopicConfigModel outputTopicConfig;

    public TenantModel() {}

    public TenantModel(final String id) {
        this.id = new TenantIdModel(id);
    }

    public TenantModel(final TenantIdModel id) {
        this.id = id;
    }

    public TenantModel(final TenantIdModel id, OutputTopicConfigModel outputTopicConfig) {
        this.id = id;
        this.outputTopicConfig = outputTopicConfig;
    }

    @Override
    public Tenant.Builder toProto() {
        Tenant.Builder result = Tenant.newBuilder().setId(id.toProto()).setCreatedAt(LHUtil.fromDate(getCreatedAt()));

        if (outputTopicConfig != null) {
            result.setOutputTopicConfig(outputTopicConfig.toProto());
        }

        if (name != null) result.setName(name);

        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Tenant tenant = (Tenant) proto;
        this.id = LHSerializable.fromProto(tenant.getId(), TenantIdModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(tenant.getCreatedAt());

        this.name = tenant.hasName() ? tenant.getName() : null;
        if (tenant.hasOutputTopicConfig()) {
            this.outputTopicConfig =
                    LHSerializable.fromProto(tenant.getOutputTopicConfig(), OutputTopicConfigModel.class, context);
        }
    }

    @Override
    public Class<Tenant> getProtoBaseClass() {
        return Tenant.class;
    }

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(new GetableIndex<TenantModel>(
                List.of(Pair.of("name", GetableIndex.ValueType.SINGLE)),
                TagStorageType.LOCAL,
                tenant -> tenant.getName() != null && !tenant.getName().isEmpty()));
    }

    @Override
    public TenantIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        if (key.equals("name") && name != null && !name.isEmpty()) {
            return List.of(new IndexedField(key, name, TagStorageType.LOCAL));
        }
        return List.of();
    }
}
