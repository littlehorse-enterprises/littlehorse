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

    private ExternalEventDefIdModel id;
    private Date createdAt;
    private ExternalEventRetentionPolicyModel retentionPolicy;

    public ExternalEventDefModel() {
        this.retentionPolicy = new ExternalEventRetentionPolicyModel();
    }

    public ExternalEventDefModel(String name) {
        this();
        this.id = new ExternalEventDefIdModel(name);
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
        return id.getName();
    }

    public Class<ExternalEventDef> getProtoBaseClass() {
        return ExternalEventDef.class;
    }

    public ExternalEventDef.Builder toProto() {
        ExternalEventDef.Builder b = ExternalEventDef.newBuilder()
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(getCreatedAt()))
                .setRetentionPolicy(retentionPolicy.toProto());
        return b;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        ExternalEventDef proto = (ExternalEventDef) p;
        id = LHSerializable.fromProto(proto.getId(), ExternalEventDefIdModel.class, context);
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
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }
}
