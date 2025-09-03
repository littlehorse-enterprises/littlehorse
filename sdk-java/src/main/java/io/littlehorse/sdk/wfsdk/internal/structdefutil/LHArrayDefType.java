package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;

public class LHArrayDefType extends LHClassType {

    public LHArrayDefType(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.INLINE_ARRAY_DEF;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        TypeDefinition.Builder typeDef = TypeDefinition.newBuilder();
        Class<?> componentType = this.clazz.getComponentType();
        LHClassType lhClassType = LHClassType.fromJavaClass(componentType);
        typeDef.setInlineArrayDef(InlineArrayDef.newBuilder().setElementType(lhClassType.getTypeDefinition()));
        return typeDef.build();
    }
}
