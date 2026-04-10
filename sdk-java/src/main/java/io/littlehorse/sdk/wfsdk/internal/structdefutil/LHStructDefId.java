package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;

public class LHStructDefId extends LHClassType {

    private final String structDefIdName;

    public LHStructDefId(String structDefIdName) {
        this.structDefIdName = structDefIdName;
    }

    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder()
                .setStructDefId(
                        StructDefId.newBuilder().setName(structDefIdName).build())
                .build();
    }

    @Override
    public TypeDefinition.DefinedTypeCase getDefinedTypeCase() {
        return TypeDefinition.DefinedTypeCase.STRUCT_DEF_ID;
    }
}
