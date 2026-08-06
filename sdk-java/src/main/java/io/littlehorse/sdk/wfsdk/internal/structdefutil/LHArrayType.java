package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import java.util.Map;

/**
 * Represents a native LittleHorse array type.
 */
public final class LHArrayType extends LHClassType {

    private final LHClassType componentType;

    public LHArrayType(Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry) {
        this(clazz, typeAdapterRegistry, Map.of());
    }

    public LHArrayType(
            Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        super(clazz, typeAdapterRegistry, placeholderValues);

        if (!clazz.isArray()) {
            throw new IllegalArgumentException(
                    "LHArrayType can only be created from array classes. Provided class: " + clazz.getName()
                            + ". Please add brackets to your class to declare an array type, e.g. MyType[] instead of MyType.");
        }
        if (byte[].class.equals(clazz)) {
            throw new IllegalArgumentException(
                    "byte[] is not supported as an array type in LittleHorse. This type has special handling as a primitive type. If you want an array of multiple bytes items, consider using `byte[][]`.");
        }

        Class<?> componentClass = clazz.getComponentType();

        if (componentClass.isArray()) {
            this.componentType = new LHArrayType(componentClass, typeAdapterRegistry, this.placeholderValues);
        } else {
            this.componentType = LHClassType.fromJavaClass(componentClass, typeAdapterRegistry, this.placeholderValues);
        }

        try {
            LHTypeConstraintValidator.ensureNoJsonPrimitiveTypes(this.componentType.getTypeDefinition());
        } catch (ForbiddenJsonTypeException ex) {
            throw new IllegalArgumentException(
                    String.format(
                            "InlineArrayDef element type %s for Java array %s: %s",
                            componentClass.getCanonicalName(), clazz.getCanonicalName(), ex.getMessage()),
                    ex);
        }
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
