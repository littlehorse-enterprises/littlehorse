package io.littlehorse.common.model.getable.global.wfspec;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.common.model.getable.global.structdef.InlineArrayDefModel;
import io.littlehorse.common.model.getable.global.structdef.InlineMapDefModel;
import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefValidationException;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import org.junit.jupiter.api.Test;

class TypeDefinitionModelTest {

    @Test
    public void shouldCheckCompatibilityForMaskedFields() {
        TypeDefinitionModel jsonTypeDefinition1 = new TypeDefinitionModel(VariableType.JSON_OBJ, true);
        TypeDefinitionModel jsonTypeDefinition2 = new TypeDefinitionModel(VariableType.JSON_OBJ, false);
        assertThat(jsonTypeDefinition1.isCompatibleWith(jsonTypeDefinition2)).isTrue();
    }

    @Test
    public void shouldRoundTripInlineStructDefinition() {
        InlineStructDef inlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "street",
                        StructFieldDef.newBuilder()
                                .setFieldType(new TypeDefinitionModel(VariableType.STR).toProto())
                                .build())
                .build();

        TypeDefinitionModel typeDefinition =
                new TypeDefinitionModel(InlineStructDefModel.fromProto(inlineStructDef, null));

        assertThat(typeDefinition.toProto().build())
                .isEqualTo(new TypeDefinitionModel(typeDefinition).toProto().build());
    }

    @Test
    public void shouldValidateAnonymousStructAgainstInlineDefinition() {
        InlineStructDef inlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "street",
                        StructFieldDef.newBuilder()
                                .setFieldType(new TypeDefinitionModel(VariableType.STR).toProto())
                                .build())
                .build();
        TypeDefinitionModel typeDefinition =
                new TypeDefinitionModel(InlineStructDefModel.fromProto(inlineStructDef, null));
        VariableValue value = VariableValue.newBuilder()
                .setStruct(Struct.newBuilder()
                        .setStruct(InlineStruct.newBuilder()
                                .putFields(
                                        "street",
                                        StructField.newBuilder()
                                                .setValue(VariableValue.newBuilder()
                                                        .setStr("Main St")
                                                        .build())
                                                .build())
                                .build())
                        .build())
                .build();

        assertThatCode(() -> typeDefinition.validateCompatibility(
                        io.littlehorse.common.model.getable.core.variable.VariableValueModel.fromProto(value, null),
                        null))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldValidateAnonymousInlineStructInsideNativeMap() {
        InlineStructDef inlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "street",
                        StructFieldDef.newBuilder()
                                .setFieldType(new TypeDefinitionModel(VariableType.STR).toProto())
                                .build())
                .build();
        TypeDefinitionModel inlineStructType =
                new TypeDefinitionModel(InlineStructDefModel.fromProto(inlineStructDef, null));
        TypeDefinitionModel mapType = new TypeDefinitionModel(
                new InlineMapDefModel(new TypeDefinitionModel(VariableType.STR), inlineStructType));
        VariableValue structValue = VariableValue.newBuilder()
                .setStruct(Struct.newBuilder()
                        .setStruct(InlineStruct.newBuilder()
                                .putFields(
                                        "street",
                                        StructField.newBuilder()
                                                .setValue(VariableValue.newBuilder()
                                                        .setStr("Main St")
                                                        .build())
                                                .build())
                                .build())
                        .build())
                .build();
        VariableValue mapValue = VariableValue.newBuilder()
                .setMap(io.littlehorse.sdk.common.proto.Map.newBuilder()
                        .addEntries(io.littlehorse.sdk.common.proto.Map.Entry.newBuilder()
                                .setKey(VariableValue.newBuilder().setStr("home"))
                                .setValue(structValue)))
                .build();

        assertThatCode(() -> mapType.validateCompatibility(
                        io.littlehorse.common.model.getable.core.variable.VariableValueModel.fromProto(mapValue, null),
                        null))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldValidateInlineStructDefinitionsNestedInArrays() {
        InlineStructDef invalidStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "invalid_field",
                        StructFieldDef.newBuilder()
                                .setFieldType(new TypeDefinitionModel(VariableType.STR).toProto())
                                .build())
                .build();
        TypeDefinitionModel inlineStructType =
                new TypeDefinitionModel(InlineStructDefModel.fromProto(invalidStructDef, null));
        TypeDefinitionModel arrayType = new TypeDefinitionModel(new InlineArrayDefModel(inlineStructType));

        assertThatThrownBy(() -> arrayType.validateAndPin(null))
                .isInstanceOf(StructDefValidationException.class)
                .hasMessageContaining("invalid_field");
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
