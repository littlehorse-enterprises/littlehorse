package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.objectId.ExternalEventDefId;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.ExternalEventDefIdPb;
import io.littlehorse.jlib.common.proto.ExternalEventDefPb;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import java.util.Date;

public class ExternalEventDef extends GETable<ExternalEventDefPb> {

    public String name;
    public Date createdAt;
    public Integer retentionHours;

    public ExternalEventDef() {}

    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public static String getFullPrefixByName(String name) {
        return StoreUtils.getFullStoreKey(name + "/", ExternalEventDef.class);
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
}
