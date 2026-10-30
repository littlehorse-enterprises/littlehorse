package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.StructOneOf;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructOneOf;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class LHInlineStructDefType extends LHClassType {

    private final InlineStructDef inlineStructDef;
    private List<LHStructProperty> structProperties;

    LHInlineStructDefType(
            Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        super(clazz, typeAdapterRegistry, placeholderValues);
        this.inlineStructDef = buildInlineStructDef();
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.INLINE_STRUCT_DEF;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder().setInlineStructDef(inlineStructDef).build();
    }

    public List<LHStructProperty> getStructProperties() throws IntrospectionException {
        if (structProperties == null) {
            if (clazz.isRecord()) {
                structProperties = List.of(clazz.getRecordComponents()).stream()
                        .map(component -> new LHStructProperty(component, this))
                        .filter(property -> !property.isIgnored())
                        .collect(Collectors.toUnmodifiableList());
                return structProperties;
            }
            structProperties = List.of(Introspector.getBeanInfo(clazz).getPropertyDescriptors()).stream()
                    .filter(property -> !"class".equals(property.getName()))
                    .map(property -> new LHStructProperty(property, this))
                    .filter(property -> !property.isIgnored())
                    .collect(Collectors.toUnmodifiableList());
        }
        return structProperties;
    }

    private InlineStructDef buildInlineStructDef() {
        try {
            return buildInlineStructDef(clazz, getStructProperties(), typeAdapterRegistry);
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Cannot build InlineStructDef for type: " + clazz.getName(), e);
        }
    }

    // Named and inline Structs use the same field definitions and oneof rules.
    static InlineStructDef buildInlineStructDef(
            Class<?> clazz, List<LHStructProperty> properties, LHTypeAdapterRegistry registry) {
        String typeName = clazz.getSimpleName();
        LHStructOneOf choice = clazz.getAnnotation(LHStructOneOf.class);
        if (choice != null && clazz.isAnnotationPresent(LHStructDef.class)) {
            throw new IllegalArgumentException("Cannot combine type-level @LHStructOneOf with @LHStructDef on "
                    + typeName + "; put the oneof in an inline helper or declare a group on properties");
        }
        if (choice != null && !choice.group().isEmpty()) {
            throw new IllegalArgumentException("@LHStructOneOf group must be omitted on type " + typeName);
        }
        Set<String> fieldNames = new HashSet<>();
        InlineStructDef.Builder result = InlineStructDef.newBuilder();
        Map<String, InlineStructDef.Builder> groupBuilders = new LinkedHashMap<>();
        for (LHStructProperty property : properties) {
            String name = property.getFieldName();
            if (!fieldNames.add(name)) {
                throw new IllegalArgumentException("Field " + name + " must map to exactly one Java property");
            }
            StructFieldDef field = property.toStructFieldDef(registry);
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
            if (group == null) {
                result.putFields(name, field);
                continue;
            }
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

        if (choice != null) {
            if (result.getFieldsCount() == 0) {
                throw new IllegalArgumentException("Oneof " + typeName + " needs at least one field");
            }
            result.setOneofConfig(StructOneOf.newBuilder().setSelectionRequired(choice.required()));
        }
        for (var entry : groupBuilders.entrySet()) {
            String name = entry.getKey();
            InlineStructDef.Builder group = entry.getValue();
            if (fieldNames.contains(name)) {
                throw new IllegalArgumentException("Oneof group " + name + " conflicts with a serialized property");
            }
            InlineStructDef schema = group.build();
            result.putFields(
                    name,
                    StructFieldDef.newBuilder()
                            .setFieldType(TypeDefinition.newBuilder().setInlineStructDef(schema))
                            .build());
        }
        return result.build();
    }
}
