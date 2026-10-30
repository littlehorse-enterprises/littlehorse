package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import java.beans.IntrospectionException;
import java.util.List;
import java.util.Map;

public final class LHInlineStructDefType extends LHClassType {

    private final LHStructLayout structLayout;

    LHInlineStructDefType(
            Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        super(clazz, typeAdapterRegistry, placeholderValues);
        try {
            this.structLayout = new LHStructLayout(this, typeAdapterRegistry);
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
        return structLayout.getProperties();
    }

    public LHStructLayout getStructLayout() {
        return structLayout;
    }
}
