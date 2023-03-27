package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.ExternalEventDefIdPb;
import io.littlehorse.jlib.common.proto.ExternalEventDefPb;

// Used by ExternalEventDef, TaskDef, and ExternalEventDef
public class ExternalEventDefId
    extends ObjectId<ExternalEventDefIdPb, ExternalEventDefPb, ExternalEventDef> {

    public String name;
    public int version;

    public ExternalEventDefId() {}

    public ExternalEventDefId(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public Class<ExternalEventDefIdPb> getProtoBaseClass() {
        return ExternalEventDefIdPb.class;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public void initFrom(Message proto) {
        ExternalEventDefIdPb p = (ExternalEventDefIdPb) proto;
        version = p.getVersion();
        name = p.getName();
    }

    public ExternalEventDefIdPb.Builder toProto() {
        ExternalEventDefIdPb.Builder out = ExternalEventDefIdPb
            .newBuilder()
            .setVersion(version)
            .setName(name);
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(name, LHUtil.toLHDbVersionFormat(version));
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        name = split[0];
        version = Integer.valueOf(split[1]);
    }

    public GETableClassEnumPb getType() {
        return GETableClassEnumPb.WF_SPEC;
    }
}
