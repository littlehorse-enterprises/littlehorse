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
    public int version;
    public Date createdAt;

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
            .setCreatedAt(LHUtil.fromDate(getCreatedAt()));

        return b;
    }

    public void initFrom(Message p) {
        ExternalEventDefPb proto = (ExternalEventDefPb) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
    }

    public static ExternalEventDef fromProto(ExternalEventDefPb p) {
        ExternalEventDef out = new ExternalEventDef();
        out.initFrom(p);
        return out;
    }

    public static ExternalEventDefIdPb parseId(String fullId) {
        String[] split = fullId.split("/");
        return ExternalEventDefIdPb
            .newBuilder()
            .setName(split[0])
            .setVersion(Integer.valueOf(split[1]))
            .build();
    }

    public ExternalEventDefId getObjectId() {
        return new ExternalEventDefId(name, version);
    }
}
