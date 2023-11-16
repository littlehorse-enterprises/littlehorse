package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

// Used by ExternalEventDef, TaskDef, and ExternalEventDef
public class ExternalEventDefIdModel extends MetadataId<ExternalEventDefId, ExternalEventDef, ExternalEventDefModel> {

    public String name;

    public ExternalEventDefIdModel() {}

    public ExternalEventDefIdModel(String name) {
        this.name = name;
    }

    @Override
    public Class<ExternalEventDefId> getProtoBaseClass() {
        return ExternalEventDefId.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ExternalEventDefId p = (ExternalEventDefId) proto;
        name = p.getName();
    }

    @Override
    public ExternalEventDefId.Builder toProto() {
        ExternalEventDefId.Builder out = ExternalEventDefId.newBuilder().setName(name);
        return out;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void initFromString(String storeKey) {
        name = storeKey;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.EXTERNAL_EVENT_DEF;
    }
}
