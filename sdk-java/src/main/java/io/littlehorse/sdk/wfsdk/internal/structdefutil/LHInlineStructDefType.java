package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import java.beans.IntrospectionException;
import java.beans.Introspector;
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
                structProperties = java.util.Arrays.stream(clazz.getRecordComponents())
                        .map(component -> new LHStructProperty(component, this))
                        .filter(property -> !property.isIgnored())
                        .toList();
                return structProperties;
            }
            structProperties = getBeanProperties();
        }
        return structProperties;
    }

    public List<LHStructProperty> getRecordReadProperties(Set<String> wireFields) throws IntrospectionException {
        List<LHStructProperty> components = getStructProperties();
        if (components.stream().allMatch(property -> wireFields.contains(property.getFieldName()))) {
            return components;
        }
        if (components.stream().anyMatch(property -> property.isOneofHelper() || property.getOneofGroup() != null)) {
            return components;
        }

        // Earlier SDKs wrote inline records using JavaBean getters. Only use that mapping
        // when the payload has exactly the fields the old writer would have emitted.
        List<LHStructProperty> legacy = getBeanProperties();
        Set<String> legacyNames =
                legacy.stream().map(LHStructProperty::getFieldName).collect(Collectors.toSet());
        return legacyNames.equals(wireFields) ? legacy : components;
    }

    private List<LHStructProperty> getBeanProperties() throws IntrospectionException {
        return List.of(Introspector.getBeanInfo(clazz).getPropertyDescriptors()).stream()
                .filter(property -> !"class".equals(property.getName()))
                .map(property -> new LHStructProperty(property, this))
                .filter(property -> !property.isIgnored())
                .collect(Collectors.toUnmodifiableList());
    }

    private InlineStructDef buildInlineStructDef() {
        try {
            return LHStructSchema.build(clazz, getStructProperties(), typeAdapterRegistry);
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Cannot build InlineStructDef for type: " + clazz.getName(), e);
        }
    }
}
