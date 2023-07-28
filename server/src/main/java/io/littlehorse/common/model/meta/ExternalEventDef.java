package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.ExternalEventDefId;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEventDefIdPb;
import io.littlehorse.sdk.common.proto.ExternalEventDefPb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ExternalEventDef extends Getable<ExternalEventDefPb> {

    public String name;
    public Date createdAt;
    public Integer retentionHours;

    public ExternalEventDef() {}

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

    public Class<ExternalEventDefPb> getProtoBaseClass() {
        return ExternalEventDefPb.class;
    }

    public ExternalEventDefPb.Builder toProto() {
        ExternalEventDefPb.Builder b = ExternalEventDefPb
            .newBuilder()
            .setName(name)
            .setRetentionHours(retentionHours)
            .setCreatedAt(LHUtil.fromDate(getCreatedAt()));
        return b;
    }

    public void initFrom(Message p) {
        ExternalEventDefPb proto = (ExternalEventDefPb) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        retentionHours = proto.getRetentionHours();
    }

    public static ExternalEventDef fromProto(ExternalEventDefPb p) {
        ExternalEventDef out = new ExternalEventDef();
        out.initFrom(p);
        return out;
    }

    public static ExternalEventDefIdPb parseId(String fullId) {
        return ExternalEventDefIdPb.newBuilder().setName(fullId).build();
    }

    public ExternalEventDefId getObjectId() {
        return new ExternalEventDefId(name);
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageTypePb> tagStorageTypePb
    ) {
        return List.of();
    }
}
