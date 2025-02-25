package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.MetricSpec;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

public class MetricSpecModel extends MetadataGetable<MetricSpec> {

    private MetricSpecIdModel id;
    private Date createdAt;

    @Getter
    private Duration windowLength;

    public MetricSpecModel() {}

    public MetricSpecModel(MetricSpecIdModel id, Duration windowLength) {
        this.id = id;
        this.createdAt = new Date();
        this.windowLength = windowLength;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        MetricSpec p = (MetricSpec) proto;
        this.id = LHSerializable.fromProto(p.getId(), MetricSpecIdModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        this.windowLength = Duration.ofSeconds(p.getWindowLength().getSeconds());
    }

    @Override
    public MetricSpec.Builder toProto() {
        return MetricSpec.newBuilder()
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setWindowLength(com.google.protobuf.Duration.newBuilder()
                        .setSeconds(this.windowLength.getSeconds())
                        .build());
    }

    @Override
    public MetricSpecIdModel getObjectId() {
        return id;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    @Override
    public Class<MetricSpec> getProtoBaseClass() {
        return MetricSpec.class;
    }
}
