package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;

/**
 * Represents a native LittleHorse array type.
 */
public final class LHArrayType extends LHClassType {

    private final LHClassType componentType;

    public LHArrayType(Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry) {
        super(clazz, typeAdapterRegistry);

        if (!clazz.isArray()) {
            throw new IllegalArgumentException(
                    "LHArrayType can only be created from array classes. Provided class: " + clazz.getName()
                            + ". Please add brackets to your class to declare an array type, e.g. MyType[] instead of MyType.");
        }

        Class<?> componentClass = clazz.getComponentType();

        if (componentClass.isArray()) {
            this.componentType = new LHArrayType(componentClass, typeAdapterRegistry);
        } else {
            this.componentType = LHClassType.fromJavaClass(componentClass, typeAdapterRegistry);
        }

        try {
            LHTypeConstraintValidator.ensureNoJsonPrimitiveTypes(this.componentType.getTypeDefinition());
        } catch (ForbiddenJsonTypeException ex) {
            throw new IllegalArgumentException(
                    String.format(
                            "InlineArrayDef element type %s for Java array %s resolves to forbidden type %s. Within StructDefs, use native equivalents such as StructDefs for nested object types and Java arrays for native LH arrays. You can also opt to use a Type Adapter and map your class to a non-JSON primitive type.",
                            componentClass.getCanonicalName(), clazz.getCanonicalName(), ex.getForbiddenType()),
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
