package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.StructOneOf;
import io.littlehorse.sdk.common.proto.VariableValue.ValueCase;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructOneOf;
import java.util.List;

/** The schema and selection rules for one oneof, declared as a helper type or a group of properties. */
public final class LHStructOneOfType {
    private final String name;
    private final InlineStructDef definition;

    public LHStructOneOfType(String name, InlineStructDef definition) {
        if (!definition.hasOneofConfig()) {
            throw new IllegalArgumentException("Struct " + name + " is not a oneof");
        }
        this.name = name;
        this.definition = definition;
    }

    static LHStructOneOfType fromHelper(
            Class<?> clazz, List<LHStructProperty> alternatives, LHTypeAdapterRegistry registry) {
        String name = clazz.getSimpleName();
        LHStructOneOf annotation = clazz.getAnnotation(LHStructOneOf.class);
        if (clazz.isAnnotationPresent(LHStructDef.class)) {
            throw new IllegalArgumentException("Cannot combine type-level @LHStructOneOf with @LHStructDef on " + name
                    + "; put the oneof in an inline helper or declare a group on properties");
        }
        if (!annotation.group().isEmpty()) {
            throw new IllegalArgumentException("@LHStructOneOf group must be omitted on type " + name);
        }
        for (LHStructProperty property : alternatives) {
            if (property.getOneOf() != null) {
                throw new IllegalArgumentException(
                        "Cannot combine type-level and property-level @LHStructOneOf on " + name);
            }
        }
        return new LHStructOneOfType(name, buildDefinition(name, alternatives, annotation.required(), registry));
    }

    static LHStructOneOfType fromGroup(
            String name, List<LHStructProperty> alternatives, LHTypeAdapterRegistry registry) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Oneof group name must be nonblank");
        }
        if (!name.matches("[a-z][a-zA-Z0-9]*")) {
            throw new IllegalArgumentException("Oneof group " + name + " must be a valid Struct field name");
        }
        boolean required = alternatives.getFirst().getOneOf().required();
        for (LHStructProperty property : alternatives) {
            if (property.getOneOf().required() != required) {
                throw new IllegalArgumentException(
                        "Oneof group " + name + " has conflicting required settings; every member must agree");
            }
        }
        return new LHStructOneOfType(name, buildDefinition(name, alternatives, required, registry));
    }

    private static InlineStructDef buildDefinition(
            String name, List<LHStructProperty> alternatives, boolean required, LHTypeAdapterRegistry registry) {
        if (alternatives.isEmpty()) {
            throw new IllegalArgumentException("Oneof " + name + " needs at least one field");
        }
        InlineStructDef.Builder definition = InlineStructDef.newBuilder()
                .setOneofConfig(StructOneOf.newBuilder().setSelectionRequired(required));
        for (LHStructProperty property : alternatives) {
            String fieldName = property.getFieldName();
            if (definition.containsFields(fieldName)) {
                throw new IllegalArgumentException("Field " + fieldName + " must map to exactly one Java property");
            }
            StructFieldDef field = property.toStructFieldDef(registry);
            if (property.isPrimitiveType() || field.getIsNullable() || field.hasDefaultValue()) {
                throw new IllegalArgumentException("Oneof field " + fieldName
                        + " must use a reference type without a default or nullable annotation");
            }
            definition.putFields(fieldName, field);
        }
        return definition.build();
    }

    public InlineStructDef getInlineStructDef() {
        return definition;
    }

    public void validateSelection(InlineStruct value) {
        for (var entry : value.getFieldsMap().entrySet()) {
            if (!definition.containsFields(entry.getKey())) {
                throw new IllegalArgumentException("Unknown field " + entry.getKey() + " in oneof " + name);
            }
            if (entry.getValue().getValue().getValueCase() == ValueCase.VALUE_NOT_SET) {
                throw new IllegalArgumentException("Oneof field " + entry.getKey() + " cannot be null");
            }
        }
        boolean required = definition.getOneofConfig().getSelectionRequired();
        int count = value.getFieldsCount();
        if (count > 1 || (required && count != 1)) {
            throw new IllegalArgumentException(
                    "Oneof " + name + " requires " + (required ? "exactly" : "at most") + " one field, found " + count);
        }
    }
}
