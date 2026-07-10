package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineMapDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;

/**
 * Represents a native LittleHorse map type with typed keys and values.
 */
public final class LHMapType extends LHClassType {

    private final LHClassType keyType;
    private final LHClassType valueType;

    public LHMapType(Class<?> keyClazz, Class<?> valueClazz, LHTypeAdapterRegistry typeAdapterRegistry) {
        super(java.util.Map.class, typeAdapterRegistry);

        LHClassType resolvedKeyType = LHClassType.fromJavaClass(keyClazz, typeAdapterRegistry);
        if (resolvedKeyType.getDefinedTypeCase() != DefinedTypeCase.PRIMITIVE_TYPE) {
            throw new IllegalArgumentException(
                    "Map key type must resolve to a primitive VariableType. Provided key class: " + keyClazz.getName()
                            + " resolves to " + resolvedKeyType.getDefinedTypeCase()
                            + ". Only primitive types (STR, INT, DOUBLE, BOOL, BYTES, WF_RUN_ID, TIMESTAMP) are allowed as map keys.");
        }
        this.keyType = resolvedKeyType;

        if (valueClazz.isArray()) {
            this.valueType = new LHArrayType(valueClazz, typeAdapterRegistry);
        } else {
            this.valueType = LHClassType.fromJavaClass(valueClazz, typeAdapterRegistry);
        }

        try {
            LHTypeConstraintValidator.ensureNoJsonPrimitiveTypes(this.keyType.getTypeDefinition());
        } catch (ForbiddenJsonTypeException ex) {
            throw new IllegalArgumentException(
                    String.format("InlineMapDef key type %s: %s", keyClazz.getCanonicalName(), ex.getMessage()), ex);
        }

        try {
            LHTypeConstraintValidator.ensureNoJsonPrimitiveTypes(this.valueType.getTypeDefinition());
        } catch (ForbiddenJsonTypeException ex) {
            throw new IllegalArgumentException(
                    String.format("InlineMapDef value type %s: %s", valueClazz.getCanonicalName(), ex.getMessage()),
                    ex);
        }
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.INLINE_MAP_DEF;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder()
                .setInlineMapDef(InlineMapDef.newBuilder()
                        .setKeyType(keyType.getTypeDefinition())
                        .setValueType(valueType.getTypeDefinition()))
                .build();
    }
}
