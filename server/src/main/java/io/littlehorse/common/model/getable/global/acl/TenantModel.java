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
import io.littlehorse.sdk.common.proto.MetricRecordingLevel;
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

@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class TenantModel extends ClusterMetadataGetable<Tenant> {

    private TenantIdModel id;
    private Date createdAt;
    private OutputTopicConfigModel outputTopicConfig;
    private MetricRecordingLevel metricsLevel;

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

        if (metricsLevel != null) {
            result.setMetricsLevel(metricsLevel);
        }

        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Tenant tenant = (Tenant) proto;
        this.id = LHSerializable.fromProto(tenant.getId(), TenantIdModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(tenant.getCreatedAt());

        if (tenant.hasOutputTopicConfig()) {
            this.outputTopicConfig =
                    LHSerializable.fromProto(tenant.getOutputTopicConfig(), OutputTopicConfigModel.class, context);
        }

        if (tenant.hasMetricsLevel()) {
            this.metricsLevel = tenant.getMetricsLevel();
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
        return List.of();
    }

    @Override
    public TenantIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return null;
    }
}
