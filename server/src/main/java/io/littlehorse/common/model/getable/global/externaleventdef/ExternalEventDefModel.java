package io.littlehorse.common.model.getable.global.externaleventdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

@Getter
public class ExternalEventDefModel extends MetadataGetable<ExternalEventDef> {

    public String name;
    public Date createdAt;
    private ExternalEventRetentionPolicyModel retentionPolicy;

    public ExternalEventDefModel() {
        this.retentionPolicy = new ExternalEventRetentionPolicyModel();
    }

    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    public String getName() {
        return name;
    }

    public Class<ExternalEventDef> getProtoBaseClass() {
        return ExternalEventDef.class;
    }

    public ExternalEventDef.Builder toProto() {
        ExternalEventDef.Builder b = ExternalEventDef.newBuilder()
                .setName(name)
                .setCreatedAt(LHUtil.fromDate(getCreatedAt()))
                .setRetentionPolicy(retentionPolicy.toProto());
        return b;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        ExternalEventDef proto = (ExternalEventDef) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        retentionPolicy =
                LHSerializable.fromProto(proto.getRetentionPolicy(), ExternalEventRetentionPolicyModel.class, context);
    }

    public static ExternalEventDefModel fromProto(ExternalEventDef p, ExecutionContext context) {
        ExternalEventDefModel out = new ExternalEventDefModel();
        out.initFrom(p, context);
        return out;
    }

    public static ExternalEventDefId parseId(String fullId) {
        return ExternalEventDefId.newBuilder().setName(fullId).build();
    }

    public ExternalEventDefIdModel getObjectId() {
        return new ExternalEventDefIdModel(name);
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }
}
