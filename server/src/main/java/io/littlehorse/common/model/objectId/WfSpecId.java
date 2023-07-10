package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.WfSpecIdPb;
import io.littlehorse.sdk.common.proto.WfSpecPb;

// Used by WfSpec, TaskDef, and ExternalEventDef
public class WfSpecId extends ObjectId<WfSpecIdPb, WfSpecPb, WfSpec> {

    public String name;
    public int version;

    public WfSpecId() {}

    public WfSpecId(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public Class<WfSpecIdPb> getProtoBaseClass() {
        return WfSpecIdPb.class;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public void initFrom(Message proto) {
        WfSpecIdPb p = (WfSpecIdPb) proto;
        version = p.getVersion();
        name = p.getName();
    }

    public WfSpecIdPb.Builder toProto() {
        WfSpecIdPb.Builder out = WfSpecIdPb
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

    public GetableClassEnumPb getType() {
        return GetableClassEnumPb.WF_SPEC;
    }
}
