package io.littlehorse.common.model.getable.global.wfspec;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.proto.VariableType;
import org.junit.jupiter.api.Test;

class TypeDefinitionModelTest {

    @Test
    public void shouldCheckCompatibilityForMaskedFields() {
        TypeDefinitionModel jsonTypeDefinition1 = new TypeDefinitionModel(VariableType.JSON_OBJ, true);
        TypeDefinitionModel jsonTypeDefinition2 = new TypeDefinitionModel(VariableType.JSON_OBJ, false);
        assertThat(jsonTypeDefinition1.isCompatibleWith(jsonTypeDefinition2)).isTrue();
    }
}
