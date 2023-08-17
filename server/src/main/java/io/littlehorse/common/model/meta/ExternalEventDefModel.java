package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ExternalEventDefModel extends Getable<ExternalEventDef> {

    public String name;
    public Date createdAt;
    public Integer retentionHours;

    public ExternalEventDefModel() {}

    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of();
    }

    public String getName() {
        return name;
    }

    public Class<ExternalEventDef> getProtoBaseClass() {
        return ExternalEventDef.class;
    }

    public ExternalEventDef.Builder toProto() {
        ExternalEventDef.Builder b = ExternalEventDef
            .newBuilder()
            .setName(name)
            .setRetentionHours(retentionHours)
            .setCreatedAt(LHUtil.fromDate(getCreatedAt()));
        return b;
    }

    public void initFrom(Message p) {
        ExternalEventDef proto = (ExternalEventDef) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        retentionHours = proto.getRetentionHours();
    }

    public static ExternalEventDefModel fromProto(ExternalEventDef p) {
        ExternalEventDefModel out = new ExternalEventDefModel();
        out.initFrom(p);
        return out;
    }

    public static ExternalEventDefId parseId(String fullId) {
        return ExternalEventDefId.newBuilder().setName(fullId).build();
    }

    public ExternalEventDefIdModel getObjectId() {
        return new ExternalEventDefIdModel(name);
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageType> tagStorageTypePb
    ) {
        return List.of();
    }
}
