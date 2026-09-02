package io.littlehorse.common.model.getable.global.wfspec;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.common.model.getable.global.structdef.InlineMapDefModel;
import io.littlehorse.sdk.common.proto.VariableType;
import org.junit.jupiter.api.Test;

class TypeDefinitionModelTest {

    @Test
    public void shouldCheckCompatibilityForMaskedFields() {
        TypeDefinitionModel jsonTypeDefinition1 = new TypeDefinitionModel(VariableType.JSON_OBJ, true);
        TypeDefinitionModel jsonTypeDefinition2 = new TypeDefinitionModel(VariableType.JSON_OBJ, false);
        assertThat(jsonTypeDefinition1.isCompatibleWith(jsonTypeDefinition2)).isTrue();
    }

    @Test
    public void shouldAcceptFullyTypedNativeMap() {
        TypeDefinitionModel mapType = new TypeDefinitionModel(new InlineMapDefModel(
                new TypeDefinitionModel(VariableType.STR), new TypeDefinitionModel(VariableType.INT)));
        assertThatCode(mapType::validateMapKeyTypes).doesNotThrowAnyException();
    }

    @Test
    public void shouldRejectWildcardMapKeyOrValue() {
        TypeDefinitionModel wildcardMap =
                new TypeDefinitionModel(new InlineMapDefModel(new TypeDefinitionModel(), new TypeDefinitionModel()));
        assertThatThrownBy(wildcardMap::validateMapKeyTypes).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldRejectNonPrimitiveMapKey() {
        TypeDefinitionModel structKeyMap = new TypeDefinitionModel(new InlineMapDefModel(
                new TypeDefinitionModel(VariableType.JSON_OBJ), new TypeDefinitionModel(VariableType.INT)));
        assertThatThrownBy(structKeyMap::validateMapKeyTypes)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("primitive");
    }

    @Test
    public void shouldRejectJsonMapValue() {
        TypeDefinitionModel jsonValueMap = new TypeDefinitionModel(new InlineMapDefModel(
                new TypeDefinitionModel(VariableType.STR), new TypeDefinitionModel(VariableType.JSON_OBJ)));
        assertThatThrownBy(jsonValueMap::validateMapKeyTypes)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JSON");
    }
}
