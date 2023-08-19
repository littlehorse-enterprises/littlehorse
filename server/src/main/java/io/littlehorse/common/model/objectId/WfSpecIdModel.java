package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;

// Used by WfSpec, TaskDef, and ExternalEventDef
public class WfSpecIdModel extends ObjectId<WfSpecId, WfSpec, WfSpecModel> {

    public String name;
    public int version;

    public WfSpecIdModel() {}

    public WfSpecIdModel(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public Class<WfSpecId> getProtoBaseClass() {
        return WfSpecId.class;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public void initFrom(Message proto) {
        WfSpecId p = (WfSpecId) proto;
        version = p.getVersion();
        name = p.getName();
    }

    public WfSpecId.Builder toProto() {
        WfSpecId.Builder out = WfSpecId.newBuilder().setVersion(version).setName(name);
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

    public GetableClassEnum getType() {
        return GetableClassEnum.WF_SPEC;
    }
}
