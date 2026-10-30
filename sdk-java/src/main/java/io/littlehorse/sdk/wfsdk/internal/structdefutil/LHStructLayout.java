package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.StructOneOf;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructOneOf;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Maps reflected Java properties to the canonical nested Struct representation. */
public final class LHStructLayout {
    private static final StructField NULL_FIELD = StructField.newBuilder()
            .setValue(VariableValue.getDefaultInstance())
            .build();

    private final List<LHStructProperty> properties;
    private final InlineStructDef definition;
    private final Map<String, InlineStructDef> groups = new LinkedHashMap<>();
    private final String typeName;

    LHStructLayout(LHClassType parent, LHTypeAdapterRegistry registry) throws IntrospectionException {
        properties = discoverProperties(parent);
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
                            .setOneofConfig(StructOneOf.newBuilder().setSelectionRequired(group.required())));
            if (groupDef.getOneofConfig().getSelectionRequired() != group.required()) {
                throw new IllegalArgumentException(
                        "Oneof group " + groupName + " has conflicting required settings; every member must agree");
            }
            groupDef.putFields(name, field);
        }

        InlineStructDef.Builder result = InlineStructDef.newBuilder().putAllFields(fields);
        if (choice != null) {
            if (fields.isEmpty()) {
                throw new IllegalArgumentException("Oneof " + typeName + " needs at least one field");
            }
            result.setOneofConfig(StructOneOf.newBuilder().setSelectionRequired(choice.required()));
        }
        for (var entry : groupBuilders.entrySet()) {
            String name = entry.getKey();
            InlineStructDef.Builder group = entry.getValue();
            if (fields.containsKey(name)) {
                throw new IllegalArgumentException("Oneof group " + name + " conflicts with a serialized property");
            }
            group.getFieldsMap().keySet().forEach(result::removeFields);
            InlineStructDef schema = group.build();
            groups.put(name, schema);
            result.putFields(
                    name,
                    StructFieldDef.newBuilder()
                            .setFieldType(TypeDefinition.newBuilder().setInlineStructDef(schema))
                            .build());
        }
        definition = result.build();
    }

    private static List<LHStructProperty> discoverProperties(LHClassType parent) throws IntrospectionException {
        Class<?> clazz = parent.getClassType();
        if (clazz.isRecord()) {
            return Arrays.stream(clazz.getRecordComponents())
                    .map(component -> new LHStructProperty(component, parent))
                    .filter(property -> !property.isIgnored())
                    .toList();
        }
        return Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors())
                .filter(property -> !"class".equals(property.getName()))
                .map(property -> new LHStructProperty(property, parent))
                .filter(property -> !property.isIgnored())
                .toList();
    }

    public List<LHStructProperty> getProperties() {
        return properties;
    }

    public InlineStructDef getDefinition() {
        return definition;
    }

    /** Groups annotated properties and omits unselected alternatives. */
    public InlineStruct toWire(InlineStruct properties) {
        InlineStruct.Builder result = properties.toBuilder();
        if (definition.hasOneofConfig()) {
            for (var entry : properties.getFieldsMap().entrySet()) {
                if (isNull(entry.getValue())) {
                    result.removeFields(entry.getKey());
                }
            }
            validateChoice(typeName, definition, result.build());
        }
        for (var entry : groups.entrySet()) {
            String name = entry.getKey();
            InlineStructDef schema = entry.getValue();
            InlineStruct.Builder selected = InlineStruct.newBuilder();
            for (String member : schema.getFieldsMap().keySet()) {
                StructField field = properties.getFieldsMap().get(member);
                if (field != null && !isNull(field)) {
                    selected.putFields(member, field);
                }
                result.removeFields(member);
            }
            validateChoice(name, schema, selected.build());
            result.putFields(
                    name,
                    StructField.newBuilder()
                            .setValue(VariableValue.newBuilder()
                                    .setStruct(Struct.newBuilder().setStruct(selected)))
                            .build());
        }
        validateOneofFields(result, false);
        return result.build();
    }

    /** Validates received values and restores unselected Java properties to null. */
    public InlineStruct fromWire(InlineStruct wire) {
        if (definition.hasOneofConfig()) {
            validateChoice(typeName, definition, wire);
        }
        InlineStruct.Builder result = wire.toBuilder();
        validateOneofFields(result, true);
        if (definition.hasOneofConfig()) {
            for (String member : definition.getFieldsMap().keySet()) {
                if (!result.containsFields(member)) {
                    result.putFields(member, NULL_FIELD);
                }
            }
        }
        for (var entry : groups.entrySet()) {
            String name = entry.getKey();
            InlineStructDef schema = entry.getValue();
            InlineStruct selected = getInlineStruct(name, wire.getFieldsMap().get(name));
            validateChoice(name, schema, selected);
            result.removeFields(name);
            for (String member : schema.getFieldsMap().keySet()) {
                if (wire.containsFields(member)) {
                    throw new IllegalArgumentException("Field " + member + " must be nested under " + name);
                }
                result.putFields(member, selected.getFieldsMap().getOrDefault(member, NULL_FIELD));
            }
        }
        return result.build();
    }

    // A helper's selection rule applies only when its containing field is present.
    // Missing fields use the containing field's default or nullability when deserializing.
    private void validateOneofFields(InlineStruct.Builder fields, boolean deserializing) {
        for (var entry : definition.getFieldsMap().entrySet()) {
            String name = entry.getKey();
            StructFieldDef field = entry.getValue();
            TypeDefinition type = field.getFieldType();
            if (!type.hasInlineStructDef() || !type.getInlineStructDef().hasOneofConfig() || groups.containsKey(name)) {
                continue;
            }
            StructField value = fields.getFieldsMap().get(name);
            // An unselected alternative can itself be a oneof helper.
            if (definition.hasOneofConfig() && value == null) {
                continue;
            }
            if (deserializing && value == null && field.hasDefaultValue()) {
                value = StructField.newBuilder()
                        .setValue(field.getDefaultValue())
                        .build();
                fields.putFields(name, value);
            }
            if (value == null || isNull(value)) {
                if (!field.getIsNullable()) {
                    throw new IllegalArgumentException("Required oneof field " + name + " cannot be absent or null");
                }
                if (deserializing && value == null) {
                    fields.putFields(name, NULL_FIELD);
                }
            } else {
                getInlineStruct(name, value);
            }
        }
    }

    private static InlineStruct getInlineStruct(String name, StructField field) {
        if (field == null || !field.getValue().hasStruct()) {
            throw new IllegalArgumentException("Oneof field " + name + " must be a nested inline Struct");
        }
        Struct struct = field.getValue().getStruct();
        if (struct.hasStructDefId() || !struct.hasStruct()) {
            throw new IllegalArgumentException("Oneof field " + name + " must be a nested inline Struct");
        }
        return struct.getStruct();
    }

    private static void validateChoice(String name, InlineStructDef schema, InlineStruct value) {
        for (var entry : value.getFieldsMap().entrySet()) {
            String member = entry.getKey();
            if (!schema.containsFields(member)) {
                throw new IllegalArgumentException("Unknown field " + member + " in oneof " + name);
            }
            if (isNull(entry.getValue())) {
                throw new IllegalArgumentException("Oneof field " + member + " cannot be null");
            }
        }
        int count = value.getFieldsCount();
        boolean required = schema.getOneofConfig().getSelectionRequired();
        if (count > 1 || (required && count != 1)) {
            throw new IllegalArgumentException(
                    "Oneof " + name + " requires " + (required ? "exactly" : "at most") + " one field, found " + count);
        }
    }

    private static boolean isNull(StructField field) {
        return field.getValue().getValueCase() == VariableValue.ValueCase.VALUE_NOT_SET;
    }
}
