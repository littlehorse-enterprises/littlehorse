package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableType;

/**
 * LHPrimitiveType is a class that represents a primitive type in the LittleHorse workflow SDK.
 *
 * It extends LHClassType and provides functionality specific to primitive types, such as generating TypeDefinitions for primitive types and handling type adapters for primitive types.
 */
public class LHPrimitiveType extends LHClassType {

    private final VariableType primitiveType;

    public LHPrimitiveType(Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry) {
        super(clazz, typeAdapterRegistry);
        this.primitiveType = LHLibUtil.javaClassToLHVarType(this.clazz, typeAdapterRegistry);
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.PRIMITIVE_TYPE;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder().setPrimitiveType(this.primitiveType).build();
    }
}
