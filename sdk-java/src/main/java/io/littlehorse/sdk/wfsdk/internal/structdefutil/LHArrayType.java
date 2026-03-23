package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableType;

/**
 * Represents a native LittleHorse array type.
 *
 * <p>Java's type erasure prevents inferring T from {@code LHArray<T>} using only a {@code Class<?>}. For now,
 * we default the element type to {@code JSON_OBJ}; callers can override once element-type annotations are added.
 */
public class LHArrayType extends LHClassType {

    public LHArrayType(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.INLINE_ARRAY_DEF;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder()
                .setInlineArrayDef(InlineArrayDef.newBuilder()
                        .setArrayType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.JSON_OBJ)))
                .build();
    }
}
