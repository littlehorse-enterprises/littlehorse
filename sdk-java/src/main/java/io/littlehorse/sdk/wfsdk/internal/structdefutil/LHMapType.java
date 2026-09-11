package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineMapDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import java.util.Map;

/**
 * Represents a native LittleHorse map type with typed keys and values.
 */
public final class LHMapType extends LHClassType {

    private final LHClassType keyType;
    private final LHClassType valueType;

    /** Creates a map whose values use Struct-member resolution. Keys always use value resolution. */
    public LHMapType(Class<?> keyClazz, Class<?> valueClazz, LHTypeAdapterRegistry typeAdapterRegistry) {
        this(keyClazz, valueClazz, typeAdapterRegistry, Map.of(), ResolutionContext.STRUCT_MEMBER);
    }

    /** Creates a map whose values use Struct-member resolution. Keys always use value resolution. */
    public LHMapType(
            Class<?> keyClazz,
            Class<?> valueClazz,
            LHTypeAdapterRegistry typeAdapterRegistry,
            Map<String, String> placeholderValues) {
        this(keyClazz, valueClazz, typeAdapterRegistry, placeholderValues, ResolutionContext.STRUCT_MEMBER);
    }

    public LHMapType(
            Class<?> keyClazz,
            Class<?> valueClazz,
            LHTypeAdapterRegistry typeAdapterRegistry,
            Map<String, String> placeholderValues,
            ResolutionContext valueResolutionContext) {
        super(Map.class, typeAdapterRegistry, placeholderValues);

        LHClassType resolvedKeyType =
                LHClassType.resolve(keyClazz, typeAdapterRegistry, this.placeholderValues, ResolutionContext.VALUE);
        if (resolvedKeyType.getDefinedTypeCase() != DefinedTypeCase.PRIMITIVE_TYPE) {
            throw new IllegalArgumentException(
                    "Map key type must resolve to a primitive VariableType. Provided key class: " + keyClazz.getName()
                            + " resolves to " + resolvedKeyType.getDefinedTypeCase()
                            + ". Only primitive types (STR, INT, DOUBLE, BOOL, BYTES, WF_RUN_ID, TIMESTAMP) are allowed as map keys.");
        }
        this.keyType = resolvedKeyType;

        if (valueClazz.isArray()) {
            this.valueType =
                    new LHArrayType(valueClazz, typeAdapterRegistry, this.placeholderValues, valueResolutionContext);
        } else {
            this.valueType = LHClassType.resolve(
                    valueClazz, typeAdapterRegistry, this.placeholderValues, valueResolutionContext);
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

    public Class<?> getKeyClass() {
        return keyType.getClassType();
    }

    public Class<?> getValueClass() {
        return valueType.getClassType();
    }

    LHClassType getResolvedValueType() {
        return valueType;
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
