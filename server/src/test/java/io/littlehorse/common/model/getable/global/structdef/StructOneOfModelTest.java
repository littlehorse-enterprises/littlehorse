package io.littlehorse.common.model.getable.global.structdef;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.InlineStructModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.util.InlineStructDefUtil;
import io.littlehorse.sdk.common.proto.Array;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.InlineMapDef;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.Map;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.StructOneOf;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import org.junit.jupiter.api.Test;

class StructOneOfModelTest {
    private static final StructFieldDef STRING = StructFieldDef.newBuilder()
            .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
            .build();

    private InlineStructDef choice(boolean required) {
        return InlineStructDef.newBuilder()
                .setOneofConfig(StructOneOf.newBuilder().setSelectionRequired(required))
                .putFields("card", STRING)
                .putFields("bank", STRING)
                .build();
    }

    private InlineStructDefModel model(InlineStructDef schema) {
        return InlineStructDefModel.fromProto(schema, null);
    }

    private InlineStructModel value(String... names) {
        InlineStruct.Builder out = InlineStruct.newBuilder();
        for (String name : names)
            out.putFields(
                    name,
                    StructField.newBuilder()
                            .setValue(VariableValue.newBuilder().setStr(""))
                            .build());
        return LHSerializable.fromProto(out.build(), InlineStructModel.class, null);
    }

    @Test
    void preservesPresenceAndSelectionRuleThroughCopies() {
        for (boolean required : new boolean[] {false, true}) {
            InlineStructDef schema = choice(required);
            assertThat(model(schema).toProto().build()).isEqualTo(schema);
            assertThat(new InlineStructDefModel(model(schema)).toProto().build())
                    .isEqualTo(schema);
        }
        assertThat(model(choice(false).toBuilder().clearOneofConfig().build()).getOneofConfig())
                .isNull();
        assertThat(model(choice(false))).isNotEqualTo(model(choice(true)));
    }

    @Test
    void validatesOnlyTheSelectedAlternative() throws Exception {
        model(choice(true)).validateAgainstSuperset(value("card"), null);
        model(choice(false)).validateAgainstSuperset(value(), null);
        assertThatThrownBy(() -> model(choice(true)).validateAgainstSuperset(value(), null))
                .hasMessageContaining("selection is required");
        assertThatThrownBy(() -> model(choice(false)).validateAgainstSuperset(value("card", "bank"), null))
                .hasMessageContaining("Only one");
        assertThatThrownBy(() -> model(choice(false)).validateAgainstSuperset(value("unknown"), null))
                .hasMessageContaining("Unknown oneof alternative");
    }

    @Test
    void rejectsNullAndWrongTypeButCountsFalseAndZero() throws Exception {
        InlineStructModel input = value("card");
        input.getFields().get("card").setValue(new VariableValueModel());
        assertThatThrownBy(() -> model(choice(false)).validateAgainstSuperset(input, null))
                .hasMessageContaining("cannot be null");
        input.getFields()
                .get("card")
                .setValue(VariableValueModel.fromProto(
                        VariableValue.newBuilder()
                                .setArray(Array.getDefaultInstance())
                                .build(),
                        null));
        assertThatThrownBy(() -> model(choice(true)).validateAgainstSuperset(input, null))
                .hasMessageContaining("invalid");
        for (VariableValue selected : new VariableValue[] {
            VariableValue.newBuilder().setInt(0).build(),
            VariableValue.newBuilder().setBool(false).build()
        }) {
            InlineStruct payload = InlineStruct.newBuilder()
                    .putFields(
                            "card", StructField.newBuilder().setValue(selected).build())
                    .build();
            InlineStructModel v = LHSerializable.fromProto(payload, InlineStructModel.class, null);

            VariableType type = selected.hasInt() ? VariableType.INT : VariableType.BOOL;
            InlineStructDef schema = choice(true).toBuilder()
                    .putFields(
                            "card",
                            STRING.toBuilder()
                                    .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(type))
                                    .build())
                    .build();
            model(schema).validateAgainstSuperset(v, null);
        }
    }

    @Test
    void rejectsEmptyNullableAndDefaultedDefinitionsButAllowsSingleAlternative() throws Exception {
        assertThatThrownBy(() ->
                        model(choice(false).toBuilder().clearFields().build()).validate(null))
                .hasMessageContaining("at least one");
        for (StructFieldDef bad : new StructFieldDef[] {
            STRING.toBuilder().setIsNullable(true).build(),
            STRING.toBuilder()
                    .setDefaultValue(VariableValue.newBuilder().setStr("default"))
                    .build()
        }) {
            assertThatThrownBy(() -> model(choice(false).toBuilder()
                                    .putFields("card", bad)
                                    .build())
                            .validate(null))
                    .hasMessageContaining("cannot be nullable or have a default");
        }
        model(choice(false).toBuilder().removeFields("bank").build()).validate(null);
        assertThatThrownBy(() -> model(choice(false)).validateNamedStructDef(null))
                .hasMessageContaining("named StructDef");
    }

    @Test
    void ordinaryStructsKeepRequiredFieldsAndAllowExtraFields() throws Exception {
        InlineStructDefModel ordinary =
                model(choice(false).toBuilder().clearOneofConfig().build());
        assertThatThrownBy(() -> ordinary.validateAgainstSuperset(value("card"), null))
                .hasMessageContaining("Missing required field bank");
        ordinary.validateAgainstSuperset(value("card", "bank", "extra"), null);
    }

    @Test
    void nestedCollectionsValidateChoicesAndApplyMasking() throws Exception {
        InlineStructDef maskedChoice = choice(true).toBuilder()
                .putFields(
                        "card",
                        STRING.toBuilder()
                                .setFieldType(STRING.getFieldType().toBuilder().setMasked(true))
                                .build())
                .build();
        TypeDefinition element =
                TypeDefinition.newBuilder().setInlineStructDef(maskedChoice).build();
        VariableValue selected = VariableValue.newBuilder()
                .setStruct(Struct.newBuilder().setStruct(value("card").toProto()))
                .build();
        TypeDefinition arrayType = TypeDefinition.newBuilder()
                .setInlineArrayDef(InlineArrayDef.newBuilder().setArrayType(element))
                .build();
        VariableValueModel array = VariableValueModel.fromProto(
                VariableValue.newBuilder()
                        .setArray(Array.newBuilder().addItems(selected))
                        .build(),
                null);
        TypeDefinitionModel.fromProto(arrayType, TypeDefinitionModel.class, null)
                .validateCompatibility(array, null);
        assertThat(array.getArray()
                        .getItems()
                        .getFirst()
                        .getStruct()
                        .getInlineStruct()
                        .getFields()
                        .get("card")
                        .isMasked())
                .isTrue();
        VariableValue bad = selected.toBuilder()
                .setStruct(Struct.newBuilder().setStruct(value("card", "bank").toProto()))
                .build();
        for (TypeDefinition type : new TypeDefinition[] {
            arrayType,
            TypeDefinition.newBuilder()
                    .setInlineMapDef(InlineMapDef.newBuilder()
                            .setKeyType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                            .setValueType(element))
                    .build()
        }) {
            VariableValue payload = type.hasInlineArrayDef()
                    ? VariableValue.newBuilder()
                            .setArray(Array.newBuilder().addItems(bad))
                            .build()
                    : VariableValue.newBuilder()
                            .setMap(Map.newBuilder()
                                    .addEntries(Map.Entry.newBuilder()
                                            .setKey(VariableValue.newBuilder().setStr("key"))
                                            .setValue(bad)))
                            .build();
            assertThatThrownBy(() -> TypeDefinitionModel.fromProto(type, TypeDefinitionModel.class, null)
                            .validateCompatibility(VariableValueModel.fromProto(payload, null), null))
                    .hasMessageContaining("Only one");
        }
    }

    @Test
    void nullableParentDoesNotRequireASelectionUntilPresent() throws Exception {
        StructFieldDef method = StructFieldDef.newBuilder()
                .setIsNullable(true)
                .setFieldType(TypeDefinition.newBuilder().setInlineStructDef(choice(true)))
                .build();
        InlineStructDef parent =
                InlineStructDef.newBuilder().putFields("method", method).build();
        model(parent).validateAgainstSuperset(value(), null);
        InlineStructModel present = LHSerializable.fromProto(
                InlineStruct.newBuilder()
                        .putFields(
                                "method",
                                StructField.newBuilder()
                                        .setValue(VariableValue.newBuilder()
                                                .setStruct(Struct.newBuilder()
                                                        .setStruct(InlineStruct.getDefaultInstance())))
                                        .build())
                        .build(),
                InlineStructModel.class,
                null);
        assertThatThrownBy(() -> model(parent).validateAgainstSuperset(present, null))
                .hasMessageContaining("selection is required");
        assertThatThrownBy(() -> model(parent.toBuilder()
                                .putFields(
                                        "method",
                                        method.toBuilder().setIsNullable(false).build())
                                .build())
                        .validateAgainstSuperset(value(), null))
                .hasMessageContaining("Missing required field method");
    }

    private InlineStructDef parent(InlineStructDef choice) {
        return InlineStructDef.newBuilder()
                .putFields(
                        "method",
                        StructFieldDef.newBuilder()
                                .setIsNullable(true)
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setInlineArrayDef(InlineArrayDef.newBuilder()
                                                .setArrayType(TypeDefinition.newBuilder()
                                                        .setInlineStructDef(choice))))
                                .build())
                .build();
    }

    @Test
    void optionalParentsAndCollectionsDoNotHideIncompatibleChoiceChanges() {
        InlineStructDef original = choice(false);
        for (InlineStructDef changed : new InlineStructDef[] {
            choice(true), original.toBuilder().clearOneofConfig().build(),
            original.toBuilder().putFields("wallet", STRING).build(),
                    original.toBuilder().removeFields("card").build()
        }) {
            assertThat(InlineStructDefUtil.getIncompatibleFields(
                            StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES,
                            model(parent(changed)),
                            model(parent(original))))
                    .contains("method[]");
        }
    }

    @Test
    void descriptionsAndFieldOrderDoNotChangeChoiceSchema() {
        InlineStructDef first = choice(false);
        InlineStructDef reordered = first.toBuilder()
                .clearFields()
                .putFields("bank", STRING)
                .putFields(
                        "card", STRING.toBuilder().setDescription("Card token").build())
                .build();
        assertThat(InlineStructDefUtil.schemasEqual(model(parent(first)), model(parent(reordered))))
                .isTrue();
        assertThat(InlineStructDefUtil.getIncompatibleFields(
                        StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES,
                        model(parent(first)),
                        model(parent(reordered))))
                .isEmpty();
    }
}
