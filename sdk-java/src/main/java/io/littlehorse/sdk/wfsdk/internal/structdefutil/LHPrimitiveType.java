package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.worker.adapter.LHTypeAdapterRegistry;

public class LHPrimitiveType extends LHClassType {

    private final LHTypeAdapterRegistry typeAdapterRegistry;

    public LHPrimitiveType(Class<?> clazz) {
        this(clazz, LHTypeAdapterRegistry.empty());
    }

    public LHPrimitiveType(Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry) {
        super(clazz);
        this.typeAdapterRegistry = typeAdapterRegistry == null ? LHTypeAdapterRegistry.empty() : typeAdapterRegistry;
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
