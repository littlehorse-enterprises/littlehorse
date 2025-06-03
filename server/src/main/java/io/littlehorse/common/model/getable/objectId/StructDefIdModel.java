package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

public class StructDefIdModel extends MetadataId<StructDefId, StructDef, StructDefModel> {

    @Getter
    private String name;

    @Getter
    @Setter
    private int version;

    public StructDefIdModel() {}

    public StructDefIdModel(String name, int version) {
        this.name = name;
        this.version = version;
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

    // TODO: This leaks from Storeable.java
    public static String getPrefix(String name) {
        return GetableClassEnum.STRUCT_DEF.getNumber() + "/" + name + "/";
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(name, LHUtil.toLHDbVersionFormat(version));
    }

    @Override
    public void initFromString(String key) {
        String[] split = key.split("/");
        name = split[0];
        version = Integer.valueOf(split[1]);
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
