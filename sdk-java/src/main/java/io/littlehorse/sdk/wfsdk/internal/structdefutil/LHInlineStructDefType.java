package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class LHInlineStructDefType extends LHClassType {

    private final LHStructLayout structLayout;
    private List<LHStructProperty> structProperties;

    LHInlineStructDefType(
            Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        super(clazz, typeAdapterRegistry, placeholderValues);
        try {
            this.structLayout = new LHStructLayout(this, getStructProperties(), typeAdapterRegistry);
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Cannot build InlineStructDef for " + clazz.getName(), e);
        }
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.INLINE_STRUCT_DEF;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder()
                .setInlineStructDef(structLayout.getDefinition())
                .build();
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

    public LHStructLayout getStructLayout() {
        return structLayout;
    }
}
