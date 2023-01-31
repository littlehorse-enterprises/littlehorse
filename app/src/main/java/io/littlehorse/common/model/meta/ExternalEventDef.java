package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.ExternalEventDefPb;
import io.littlehorse.common.proto.ExternalEventDefPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import java.util.Date;

public class ExternalEventDef extends GETable<ExternalEventDefPbOrBuilder> {

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
        return ExternalEventDef.getSubKey(name, version);
    }

    public static String getFullPrefixByName(String name) {
        return StoreUtils.getFullStoreKey(name + "/", ExternalEventDef.class);
    }

    public static String getFullKey(String name, int version) {
        return StoreUtils.getFullStoreKey(name + "/", ExternalEventDef.class);
    }

    public static String getSubKey(String name, int version) {
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
}
