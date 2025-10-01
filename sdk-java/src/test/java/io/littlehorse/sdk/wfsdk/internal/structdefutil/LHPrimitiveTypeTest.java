package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import org.junit.jupiter.api.Test;

public class LHPrimitiveTypeTest {
    @Test
    public void testGetTypeDefinitionWithNonPrimitiveClass() {
        LHPrimitiveType intType = new LHPrimitiveType(Integer.class);

        TypeDefinition actualTypeDefinition = intType.getTypeDefinition();
        TypeDefinition expectedTypeDefinition =
                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT).build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }

    @Test
    public void testGetTypeDefinitionWithPrimitiveClass() {
        LHPrimitiveType intType = new LHPrimitiveType(int.class);

        TypeDefinition actualTypeDefinition = intType.getTypeDefinition();
        TypeDefinition expectedTypeDefinition =
                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT).build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }

    @Test
    public void testGetTypeDefinitionWithWfRunId() {
        LHPrimitiveType intType = new LHPrimitiveType(WfRunId.class);

        TypeDefinition actualTypeDefinition = intType.getTypeDefinition();
        TypeDefinition expectedTypeDefinition = TypeDefinition.newBuilder()
                .setPrimitiveType(VariableType.WF_RUN_ID)
                .build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }
}
