package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;

/**
 * Represents a native LittleHorse array type.
 *
 * <p>Java's type erasure prevents inferring T from {@code LHArray<T>} using only a {@code Class<?>}. For now,
 * we default the element type to {@code JSON_OBJ}; callers can override once element-type annotations are added.
 */
public class LHArrayType extends LHClassType {

    private LHClassType componentType;

    public LHArrayType(Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry) {
        super(clazz, typeAdapterRegistry);
    }

    private void initializeComponentType() {
        if (clazz.getComponentType() != null) {
            throw new LHSerdeException(String.format(
                    "Cannot create LHArrayType, class %s does not have a component type.", clazz.getComponentType()));
        }

        this.componentType = LHClassType.fromJavaClass(clazz.getComponentType(), typeAdapterRegistry);
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.INLINE_ARRAY_DEF;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        if (componentType == null) {
            initializeComponentType();
        }

        return TypeDefinition.newBuilder()
                .setInlineArrayDef(InlineArrayDef.newBuilder().setArrayType(componentType.getTypeDefinition()))
                .build();
    }
}
