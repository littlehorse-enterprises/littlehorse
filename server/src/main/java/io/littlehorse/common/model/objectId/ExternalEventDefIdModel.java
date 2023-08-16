package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.ExternalEventDefIdPb;

// Used by ExternalEventDef, TaskDef, and ExternalEventDef
public class ExternalEventDefIdModel
    extends ObjectId<ExternalEventDefIdPb, ExternalEventDef, ExternalEventDefModel> {

    public String name;

    public ExternalEventDefIdModel() {}

    public ExternalEventDefIdModel(String name) {
        this.name = name;
    }

    public Class<ExternalEventDefIdPb> getProtoBaseClass() {
        return ExternalEventDefIdPb.class;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public void initFrom(Message proto) {
        ExternalEventDefIdPb p = (ExternalEventDefIdPb) proto;
        name = p.getName();
    }

    public ExternalEventDefIdPb.Builder toProto() {
        ExternalEventDefIdPb.Builder out = ExternalEventDefIdPb
            .newBuilder()
            .setName(name);
        return out;
    }

    public String getStoreKey() {
        return name;
    }

    public void initFrom(String storeKey) {
        name = storeKey;
    }

    public GetableClassEnumPb getType() {
        return GetableClassEnumPb.WF_SPEC;
    }
}
