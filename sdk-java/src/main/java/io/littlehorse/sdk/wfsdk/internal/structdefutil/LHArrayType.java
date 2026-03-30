package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;

/**
 * Represents a native LittleHorse array type.
 */
public class LHArrayType extends LHClassType {

    private LHClassType componentType;

    public LHArrayType(Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry) {
        super(clazz, typeAdapterRegistry);

        if (!clazz.isArray()) {
            throw new IllegalArgumentException("LHArrayType can only be created from array classes. Provided class: " + clazz.getName() + ". Please add brackets to your class to declare an array type, e.g. MyType[] instead of MyType.");
        }

        this.componentType = LHClassType.fromJavaClass(clazz.getComponentType(), typeAdapterRegistry);
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.INLINE_ARRAY_DEF;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder()
                .setInlineArrayDef(InlineArrayDef.newBuilder().setArrayType(componentType.getTypeDefinition()))
                .build();
    }
}
