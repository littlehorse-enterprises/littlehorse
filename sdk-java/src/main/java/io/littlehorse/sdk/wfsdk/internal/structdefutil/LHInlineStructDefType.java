package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.worker.LHStructOneOf;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.ArrayList;
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

    // Named and inline Structs share field assembly; oneof rules belong to LHStructOneOfType.
    static InlineStructDef buildInlineStructDef(
            Class<?> clazz, List<LHStructProperty> properties, LHTypeAdapterRegistry registry) {
        if (clazz.isAnnotationPresent(LHStructOneOf.class)) {
            return LHStructOneOfType.fromHelper(clazz, properties, registry).getInlineStructDef();
        }
        Set<String> fieldNames = new HashSet<>();
        InlineStructDef.Builder result = InlineStructDef.newBuilder();
        Map<String, List<LHStructProperty>> groups = new LinkedHashMap<>();
        for (LHStructProperty property : properties) {
            String name = property.getFieldName();
            if (!fieldNames.add(name)) {
                throw new IllegalArgumentException("Field " + name + " must map to exactly one Java property");
            }
            LHStructOneOf group = property.getOneOf();
            if (group == null) {
                result.putFields(name, property.toStructFieldDef(registry));
            } else {
                groups.computeIfAbsent(group.group(), ignored -> new ArrayList<>())
                        .add(property);
            }
        }
        for (var entry : groups.entrySet()) {
            String name = entry.getKey();
            if (fieldNames.contains(name)) {
                throw new IllegalArgumentException("Oneof group " + name + " conflicts with a serialized property");
            }
            LHStructOneOfType group = LHStructOneOfType.fromGroup(name, entry.getValue(), registry);
            result.putFields(
                    name,
                    StructFieldDef.newBuilder()
                            .setFieldType(TypeDefinition.newBuilder().setInlineStructDef(group.getInlineStructDef()))
                            .build());
        }
        return result.build();
    }
}
