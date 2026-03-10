package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.worker.adapter.LHTypeAdapterRegistry;
import java.util.Objects;

/**
 * LHPrimitiveType is a class that represents a primitive type in the LittleHorse workflow SDK.
 *
 * It extends LHClassType and provides functionality specific to primitive types, such as generating TypeDefinitions for primitive types and handling type adapters for primitive types.
 */
public class LHPrimitiveType extends LHClassType {

    private final LHTypeAdapterRegistry typeAdapterRegistry;

    public LHPrimitiveType(Class<?> clazz) {
        this(clazz, LHTypeAdapterRegistry.empty());
    }

    public LHPrimitiveType(Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry) {
        super(clazz);
        this.typeAdapterRegistry = Objects.requireNonNull(typeAdapterRegistry, "Type adapter registry cannot be null");
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.PRIMITIVE_TYPE;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder()
                .setPrimitiveType(LHLibUtil.javaClassToLHVarType(this.clazz, typeAdapterRegistry))
                .build();
    }
}
