package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;

public class LHPrimitiveType extends LHClassType {

    public LHPrimitiveType(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.PRIMITIVE_TYPE;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder()
                .setPrimitiveType(LHLibUtil.javaClassToLHVarType(this.clazz))
                .build();
    }
}
