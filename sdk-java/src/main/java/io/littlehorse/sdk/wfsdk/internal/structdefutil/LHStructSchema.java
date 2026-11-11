package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructOneOf;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Shared schema and selection rules for named Structs and embedded Java types. */
public final class LHStructSchema {
    private LHStructSchema() {}

    public static void validateGroupName(String name) {
        if (!name.matches("[a-z][a-zA-Z0-9]*")) {
            throw new IllegalArgumentException(
                    "Invalid oneof group '" + name + "': use a lowercase initial letter and alphanumeric camelCase");
        }
    }

    public static InlineStructDef definition(LHClassType type) {
        return type instanceof LHStructDefType
                ? ((LHStructDefType) type).getInlineStructDef()
                : type.getTypeDefinition().getInlineStructDef();
    }

    public static InlineStructDef build(
            Class<?> type, List<LHStructProperty> properties, LHTypeAdapterRegistry registry) {
        LHStructOneOf marker = type.getAnnotation(LHStructOneOf.class);
        if (marker != null
                && (type.isAnnotationPresent(LHStructDef.class)
                        || !marker.group().isEmpty())) {
            throw new IllegalArgumentException(
                    "Oneof helper " + type.getName() + " cannot have a StructDef name or a type-level group name");
        }
        if (marker != null) {
            if (!java.lang.reflect.Modifier.isPublic(type.getModifiers())) {
                throw new IllegalArgumentException("Oneof helper must be public: " + type.getName());
            }
            if (!type.isRecord()) {
                try {
                    type.getConstructor();
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException(
                            "Oneof helper requires a public no-arg constructor: " + type.getName(), e);
                }
            }
        }
        InlineStructDef.Builder result = InlineStructDef.newBuilder();
        Map<String, String> owners = new HashMap<>();
        for (LHStructProperty property : properties) {
            property.getOneofGroup(); // Validate declarations even on helper properties.
            if (property.isOneofHelper()) {
                validateGroupName(property.getFieldName());
                if (property.isNullable())
                    throw new IllegalArgumentException("Oneof helper " + property + " cannot be nullable");
                if (property.getDefaultValue(registry).isPresent()) {
                    throw new IllegalArgumentException("Oneof helper " + property + " cannot have a default");
                }
                InlineStructDef helper = definition(property.getPropertyType(registry));
                helper.getFieldsMap()
                        .forEach((name, field) -> put(
                                result,
                                owners,
                                type,
                                property,
                                name,
                                field.toBuilder()
                                        .setOneofGroup(property.getFieldName())
                                        .build()));
            } else {
                put(result, owners, type, property, property.getFieldName(), property.toStructFieldDef(registry));
            }
        }
        return result.build();
    }

    private static void put(
            InlineStructDef.Builder result,
            Map<String, String> owners,
            Class<?> type,
            LHStructProperty property,
            String name,
            StructFieldDef field) {
        String previous = owners.putIfAbsent(name, property.getPropertyName());
        if (previous != null) {
            throw new IllegalArgumentException("Cannot declare property '" + property.getPropertyName() + "' in "
                    + type.getName() + ": field '" + name + "' is already declared by property '" + previous
                    + "'. Rename one of the serialized fields, or use field groups for this choice.");
        }
        result.putFields(name, field);
    }

    public static void validateSelections(InlineStruct value, InlineStructDef definition) {
        Map<String, String> selected = new HashMap<>();
        definition.getFieldsMap().forEach((name, field) -> {
            if (!field.hasOneofGroup() || !value.containsFields(name)) return;
            if (value.getFieldsOrThrow(name).getValue().getValueCase() == VariableValue.ValueCase.VALUE_NOT_SET) {
                throw new LHSerdeException("Oneof alternative '" + name + "' cannot contain an explicit null");
            }
            String previous = selected.putIfAbsent(field.getOneofGroup(), name);
            if (previous != null) {
                throw new LHSerdeException("Oneof group '" + field.getOneofGroup() + "' selects both '" + previous
                        + "' and '" + name + "'");
            }
        });
    }
}
