package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;

public class LHStructDefId extends LHClassType {

    private final String structDefIdName;
    private final int version;

    public LHStructDefId(String structDefIdName) {
        this(structDefIdName, -1);
    }

    public LHStructDefId(String structDefIdName, int version) {
        this.structDefIdName = structDefIdName;
        this.version = version;
    }

    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder()
                .setStructDefId(StructDefId.newBuilder()
                        .setName(structDefIdName)
                        .setVersion(version)
                        .build())
                .build();
    }

    @Override
    public TypeDefinition.DefinedTypeCase getDefinedTypeCase() {
        return TypeDefinition.DefinedTypeCase.STRUCT_DEF_ID;
    }
}
