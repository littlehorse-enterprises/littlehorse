package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import org.junit.jupiter.api.Test;

public class LHArrayDefTypeTest {
    @Test
    public void getArrayOfPrimitiveTypeDefinition() {
        LHClassType intArrayTypeDef = LHClassType.fromJavaClass(Integer[].class);

        TypeDefinition actualTypeDefinition = intArrayTypeDef.getTypeDefinition();
        TypeDefinition expectedTypeDefinition = TypeDefinition.newBuilder()
                .setInlineArrayDef(InlineArrayDef.newBuilder()
                        .setElementType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT)))
                .build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }

    @Test
    public void get2DArrayOfPrimitiveTypeDefinition() {
        LHClassType intArrayTypeDef = LHClassType.fromJavaClass(Integer[][].class);

        TypeDefinition actualTypeDefinition = intArrayTypeDef.getTypeDefinition();
        TypeDefinition expectedTypeDefinition = TypeDefinition.newBuilder()
                .setInlineArrayDef(InlineArrayDef.newBuilder()
                        .setElementType(TypeDefinition.newBuilder()
                                .setInlineArrayDef(InlineArrayDef.newBuilder()
                                        .setElementType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT)))))
                .build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }
}
