package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.ExternalEventDefIdPb;
import io.littlehorse.jlib.common.proto.ExternalEventDefPb;
import io.littlehorse.jlib.common.proto.ExternalEventDefPbOrBuilder;
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

    public String getObjectId() {
        return ExternalEventDef.getObjectId(name, version);
    }

    public static String getFullPrefixByName(String name) {
        return StoreUtils.getFullStoreKey(name + "/", ExternalEventDef.class);
    }

    public static String getObjectId(String name, int version) {
        return LHUtil.getCompositeId(name, LHUtil.toLHDbVersionFormat(version));
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
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

    public void initFrom(MessageOrBuilder p) {
        ExternalEventDefPbOrBuilder proto = (ExternalEventDefPbOrBuilder) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
    }

    public static ExternalEventDef fromProto(ExternalEventDefPbOrBuilder p) {
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

    public static String getObjectId(ExternalEventDefIdPb id) {
        return getObjectId(id.getName(), id.getVersion());
    }
}
