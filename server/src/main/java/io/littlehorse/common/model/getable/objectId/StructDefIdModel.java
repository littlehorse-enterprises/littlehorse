package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class StructDefIdModel extends MetadataId<StructDefId, StructDef, StructDefModel> {

    private String name;
    private int version;

    public StructDefIdModel() {}

    public StructDefIdModel(String name) {
        this.name = name;
    }

    @Override
    public StructDefId.Builder toProto() {
        StructDefId.Builder out = StructDefId.newBuilder().setName(name).setVersion(version);

        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        StructDefId proto = (StructDefId) p;
        name = proto.getName();
        version = proto.getVersion();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void initFromString(String key) {
        name = key;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.STRUCT_DEF;
    }

    @Override
    public Class<StructDefId> getProtoBaseClass() {
        return StructDefId.class;
    }
}
