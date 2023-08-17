package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;

// Used by ExternalEventDef, TaskDef, and ExternalEventDef
public class ExternalEventDefIdModel
    extends ObjectId<ExternalEventDefId, ExternalEventDef, ExternalEventDefModel> {

    public String name;

    public ExternalEventDefIdModel() {}

    public ExternalEventDefIdModel(String name) {
        this.name = name;
    }

    public Class<ExternalEventDefId> getProtoBaseClass() {
        return ExternalEventDefId.class;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public void initFrom(Message proto) {
        ExternalEventDefId p = (ExternalEventDefId) proto;
        name = p.getName();
    }

    public ExternalEventDefId.Builder toProto() {
        ExternalEventDefId.Builder out = ExternalEventDefId
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

    public GetableClassEnum getType() {
        return GetableClassEnum.WF_SPEC;
    }
}
