package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.List;
import java.util.Map;
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
            structProperties = List.of(Introspector.getBeanInfo(clazz).getPropertyDescriptors()).stream()
                    .filter(property -> !"class".equals(property.getName()))
                    .map(property -> new LHStructProperty(property, this))
                    .filter(property -> !property.isIgnored())
                    .collect(Collectors.toUnmodifiableList());
        }
        return structProperties;
    }

    private InlineStructDef buildInlineStructDef() {
        InlineStructDef.Builder definition = InlineStructDef.newBuilder();
        try {
            for (LHStructProperty property : getStructProperties()) {
                definition.putFields(property.getFieldName(), property.toStructFieldDef(typeAdapterRegistry));
            }
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Cannot build InlineStructDef for type: " + clazz.getName(), e);
        }
        return definition.build();
    }
}
