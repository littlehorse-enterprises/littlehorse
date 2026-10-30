package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.StructOneOfConstraints;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructOneOf;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Maps reflected Java properties to the canonical nested Struct representation. */
public final class LHStructLayout {
    private static final StructField NULL_FIELD = StructField.newBuilder()
            .setValue(VariableValue.getDefaultInstance())
            .build();

    private final InlineStructDef definition;
    private final Map<String, InlineStructDef> groups = new LinkedHashMap<>();
    private final String typeName;

    public LHStructLayout(LHClassType parent, List<LHStructProperty> properties, LHTypeAdapterRegistry registry) {
        typeName = parent.getClassType().getSimpleName();
        LHStructOneOf choice = parent.getClassType().getAnnotation(LHStructOneOf.class);
        if (choice != null && parent.getClassType().isAnnotationPresent(LHStructDef.class)) {
            throw new IllegalArgumentException("Cannot combine type-level @LHStructOneOf with @LHStructDef on "
                    + typeName + "; put the oneof in an inline helper or declare a group on properties");
        }
        if (choice != null && !choice.group().isEmpty()) {
            throw new IllegalArgumentException("@LHStructOneOf group must be omitted on type " + typeName);
        }
        Map<String, StructFieldDef> fields = new LinkedHashMap<>();
        Map<String, InlineStructDef.Builder> groupBuilders = new LinkedHashMap<>();
        for (LHStructProperty property : properties) {
            String name = property.getFieldName();
            if (fields.containsKey(name)) {
                throw new IllegalArgumentException("Field " + name + " must map to exactly one Java property");
            }
            StructFieldDef field = property.toStructFieldDef(registry);
            fields.put(name, field);
            LHStructOneOf group = property.getOneOf();
            if (choice != null && group != null) {
                throw new IllegalArgumentException(
                        "Cannot combine type-level and property-level @LHStructOneOf on " + typeName);
            }
            if (choice != null || group != null) {
                if (property.isPrimitiveType() || field.getIsNullable() || field.hasDefaultValue()) {
                    throw new IllegalArgumentException("Oneof field " + name
                            + " must use a reference type without a default or nullable annotation");
                }
            }
            if (group == null) continue;
            String groupName = group.group();
            if (groupName.isBlank()) {
                throw new IllegalArgumentException("Oneof group name must be nonblank for field " + name);
            }
            if (!groupName.matches("[a-z][a-zA-Z0-9]*")) {
                throw new IllegalArgumentException("Oneof group " + groupName + " must be a valid Struct field name");
            }
            InlineStructDef.Builder groupDef =
                    groupBuilders.computeIfAbsent(groupName, ignored -> InlineStructDef.newBuilder()
                            .setOneofConstraints(
                                    StructOneOfConstraints.newBuilder().setSelectionRequired(group.required())));
            if (groupDef.getOneofConstraints().getSelectionRequired() != group.required()) {
                throw new IllegalArgumentException(
                        "Oneof group " + groupName + " has conflicting required settings; every member must agree");
            }
            groupDef.putFields(name, field);
        }

        InlineStructDef.Builder result = InlineStructDef.newBuilder().putAllFields(fields);
        if (choice != null) {
            requireAlternatives(typeName, fields.size());
            result.setOneofConstraints(StructOneOfConstraints.newBuilder().setSelectionRequired(choice.required()));
        }
        groupBuilders.forEach((name, group) -> {
            if (fields.containsKey(name)) {
                throw new IllegalArgumentException("Oneof group " + name + " conflicts with a serialized property");
            }
            requireAlternatives(name, group.getFieldsCount());
            group.getFieldsMap().keySet().forEach(result::removeFields);
            InlineStructDef schema = group.build();
            groups.put(name, schema);
            result.putFields(
                    name,
                    StructFieldDef.newBuilder()
                            .setFieldType(TypeDefinition.newBuilder().setInlineStructDef(schema))
                            .build());
        });
        definition = result.build();
    }

    public InlineStructDef getDefinition() {
        return definition;
    }

    /** Groups flat properties and omits unselected alternatives; empty groups are emitted. */
    public InlineStruct toWire(InlineStruct properties) {
        InlineStruct.Builder result = properties.toBuilder();
        if (definition.hasOneofConstraints()) {
            properties.getFieldsMap().forEach((name, field) -> {
                if (isNull(field)) result.removeFields(name);
            });
            validateChoice(typeName, definition, result.build());
        }
        groups.forEach((name, schema) -> {
            InlineStruct.Builder selected = InlineStruct.newBuilder();
            schema.getFieldsMap().keySet().forEach(member -> {
                StructField field = properties.getFieldsMap().get(member);
                if (field != null && !isNull(field)) selected.putFields(member, field);
                result.removeFields(member);
            });
            validateChoice(name, schema, selected.build());
            result.putFields(
                    name,
                    StructField.newBuilder()
                            .setValue(VariableValue.newBuilder()
                                    .setStruct(Struct.newBuilder().setStruct(selected)))
                            .build());
        });
        checkChoiceParents(result, false);
        return result.build();
    }

    /** Validates the wire representation and restores flattened Java properties. */
    public InlineStruct fromWire(InlineStruct wire) {
        if (definition.hasOneofConstraints()) validateChoice(typeName, definition, wire);
        InlineStruct.Builder result = wire.toBuilder();
        checkChoiceParents(result, true);
        if (definition.hasOneofConstraints()) {
            definition.getFieldsMap().keySet().forEach(member -> {
                if (!result.containsFields(member)) result.putFields(member, NULL_FIELD);
            });
        }
        groups.forEach((name, schema) -> {
            StructField container = wire.getFieldsMap().get(name);
            if (container == null
                    || container.getValue().getValueCase() != VariableValue.ValueCase.STRUCT
                    || container.getValue().getStruct().hasStructDefId()
                    || !container.getValue().getStruct().hasStruct()) {
                throw new IllegalArgumentException("Oneof group " + name + " must be a nested inline Struct");
            }
            InlineStruct selected = container.getValue().getStruct().getStruct();
            validateChoice(name, schema, selected);
            result.removeFields(name);
            schema.getFieldsMap().keySet().forEach(member -> {
                if (wire.containsFields(member)) {
                    throw new IllegalArgumentException("Field " + member + " must be nested under " + name);
                }
                result.putFields(member, selected.getFieldsMap().getOrDefault(member, NULL_FIELD));
            });
        });
        return result.build();
    }

    private void checkChoiceParents(InlineStruct.Builder fields, boolean reading) {
        definition.getFieldsMap().forEach((name, field) -> {
            TypeDefinition type = field.getFieldType();
            if (!type.hasInlineStructDef()
                    || !type.getInlineStructDef().hasOneofConstraints()
                    || groups.containsKey(name)) return;
            StructField value = fields.getFieldsMap().get(name);
            // An alternative can itself be a choice; its parent rules apply only when selected.
            if (definition.hasOneofConstraints() && value == null) return;
            if (reading && value == null && field.hasDefaultValue()) {
                value = StructField.newBuilder()
                        .setValue(field.getDefaultValue())
                        .build();
                fields.putFields(name, value);
            }
            if (value == null || isNull(value)) {
                if (!field.getIsNullable()) {
                    throw new IllegalArgumentException("Required oneof field " + name + " cannot be absent or null");
                }
                if (reading && value == null) fields.putFields(name, NULL_FIELD);
            } else if (value.getValue().getValueCase() != VariableValue.ValueCase.STRUCT
                    || value.getValue().getStruct().hasStructDefId()
                    || !value.getValue().getStruct().hasStruct()) {
                throw new IllegalArgumentException("Oneof field " + name + " must be a nested inline Struct");
            }
        });
    }

    private static void validateChoice(String name, InlineStructDef schema, InlineStruct value) {
        value.getFieldsMap().forEach((member, field) -> {
            if (!schema.containsFields(member)) {
                throw new IllegalArgumentException("Unknown field " + member + " in oneof " + name);
            }
            if (isNull(field)) throw new IllegalArgumentException("Oneof field " + member + " cannot be null");
        });
        int count = value.getFieldsCount();
        boolean required = schema.getOneofConstraints().getSelectionRequired();
        if (count > 1 || (required && count != 1)) {
            throw new IllegalArgumentException(
                    "Oneof " + name + " requires " + (required ? "exactly" : "at most") + " one field, found " + count);
        }
    }

    private static boolean isNull(StructField field) {
        return field.getValue().getValueCase() == VariableValue.ValueCase.VALUE_NOT_SET;
    }

    private static void requireAlternatives(String name, int count) {
        if (count == 0) throw new IllegalArgumentException("Oneof " + name + " needs at least one field");
    }
}
