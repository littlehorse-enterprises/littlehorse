package io.littlehorse.common.model.getable.global.structdef;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.StructModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for superset-compatible struct validation (allow unknown payload fields).
 */
public class StructModelTest {

    @Test
    public void payloadWithExtraFieldsShouldPass() throws Exception {
        ReadOnlyMetadataManager metadataManager = null;

        // Create a StructDef model: required field 'a' (int)
        InlineStructDefModel def = new InlineStructDefModel();
        StructFieldDef sfda = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                .build();
        def.getFields().put("a", LHSerializable.fromProto(sfda, StructFieldDefModel.class, null));

        StructDefModel model = new StructDefModel();
        model.setStructDef(def);
        model.setId(new StructDefIdModel("test-base", 1));

        // payload inline struct with required 'a' and extra 'c'
        InlineStruct payloadInline = InlineStruct.newBuilder()
                .putFields(
                        "a",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder().setInt(10))
                                .build())
                .putFields(
                        "c",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder().setStr("extra"))
                                .build())
                .build();

        // should not throw
        StructModel sm = new StructModel();
        // reflection-setters used because fields are private; use proto init for model
        sm.initFrom(
                Struct.newBuilder()
                        .setStruct(payloadInline)
                        .setStructDefId(
                                StructDefId.newBuilder().setName("test-base").build())
                        .build(),
                null);

        model.validateAgainstSuperset(sm, metadataManager);
    }

    @Test
    public void missingRequiredFieldShouldFail() throws Exception {
        ReadOnlyMetadataManager metadataManager = null;

        InlineStructDefModel def = new InlineStructDefModel();
        StructFieldDef sfd = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                .build();
        def.getFields().put("a", LHSerializable.fromProto(sfd, StructFieldDefModel.class, null));

        StructDefModel model = new StructDefModel();
        model.setStructDef(def);
        model.setId(new StructDefIdModel("test-base", 1));

        InlineStruct payloadInline = InlineStruct.newBuilder()
                .putFields(
                        "c",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder().setStr("extra"))
                                .build())
                .build();

        StructModel sm = new StructModel();
        sm.initFrom(
                Struct.newBuilder()
                        .setStruct(payloadInline)
                        .setStructDefId(
                                StructDefId.newBuilder().setName("test-base").build())
                        .build(),
                null);

        assertThatThrownBy(() -> model.validateAgainstSuperset(sm, metadataManager))
                .isInstanceOf(StructValidationException.class);
    }

    @Test
    public void nullableFieldWithNullValueShouldPass() throws Exception {
        ReadOnlyMetadataManager metadataManager = null;

        InlineStructDefModel def = new InlineStructDefModel();
        StructFieldDef sfd = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setIsNullable(true)
                .build();
        def.getFields().put("name", LHSerializable.fromProto(sfd, StructFieldDefModel.class, null));

        StructDefModel model = new StructDefModel();
        model.setStructDef(def);
        model.setId(new StructDefIdModel("test-nullable", 1));

        // Field present but value is null (VALUE_NOT_SET)
        InlineStruct payloadInline = InlineStruct.newBuilder()
                .putFields(
                        "name",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder())
                                .build())
                .build();

        StructModel sm = new StructModel();
        sm.initFrom(
                Struct.newBuilder()
                        .setStruct(payloadInline)
                        .setStructDefId(StructDefId.newBuilder()
                                .setName("test-nullable")
                                .build())
                        .build(),
                null);

        // should not throw
        model.validateAgainstSuperset(sm, metadataManager);
    }

    @Test
    public void nonNullableFieldWithNullValueShouldFail() throws Exception {
        ReadOnlyMetadataManager metadataManager = null;

        InlineStructDefModel def = new InlineStructDefModel();
        // is_nullable defaults to false
        StructFieldDef sfd = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setIsNullable(false)
                .build();
        def.getFields().put("name", LHSerializable.fromProto(sfd, StructFieldDefModel.class, null));

        StructDefModel model = new StructDefModel();
        model.setStructDef(def);
        model.setId(new StructDefIdModel("test-non-nullable", 1));

        // Field present but value is null (VALUE_NOT_SET)
        InlineStruct payloadInline = InlineStruct.newBuilder()
                .putFields(
                        "name",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder())
                                .build())
                .build();

        StructModel sm = new StructModel();
        sm.initFrom(
                Struct.newBuilder()
                        .setStruct(payloadInline)
                        .setStructDefId(StructDefId.newBuilder()
                                .setName("test-non-nullable")
                                .build())
                        .build(),
                null);

        assertThatThrownBy(() -> model.validateAgainstSuperset(sm, metadataManager))
                .isInstanceOf(StructValidationException.class)
                .hasMessageContaining("not nullable");
    }

    @Test
    public void nullableFieldAbsentShouldPass() throws Exception {
        ReadOnlyMetadataManager metadataManager = null;

        // nullable=true with no explicit default => implicit null default; absence is fine
        InlineStructDefModel def = new InlineStructDefModel();
        StructFieldDef sfd = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setIsNullable(true)
                .build();
        def.getFields().put("name", LHSerializable.fromProto(sfd, StructFieldDefModel.class, null));

        StructDefModel model = new StructDefModel();
        model.setStructDef(def);
        model.setId(new StructDefIdModel("test-nullable-required", 1));

        // Field is completely absent
        InlineStruct payloadInline = InlineStruct.newBuilder().build();

        StructModel sm = new StructModel();
        sm.initFrom(
                Struct.newBuilder()
                        .setStruct(payloadInline)
                        .setStructDefId(StructDefId.newBuilder()
                                .setName("test-nullable-required")
                                .build())
                        .build(),
                null);

        // should not throw
        model.validateAgainstSuperset(sm, metadataManager);
    }

    @Test
    public void nullableOptionalFieldAbsentShouldMaterializeDefault() throws Exception {
        ReadOnlyMetadataManager metadataManager = null;

        // nullable=true with a non-null default => optional; absence should materialize the default
        InlineStructDefModel def = new InlineStructDefModel();
        StructFieldDef sfd = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setIsNullable(true)
                .setDefaultValue(VariableValue.newBuilder().setStr("default").build())
                .build();
        def.getFields().put("name", LHSerializable.fromProto(sfd, StructFieldDefModel.class, null));

        StructDefModel model = new StructDefModel();
        model.setStructDef(def);
        model.setId(new StructDefIdModel("test-nullable-optional", 1));

        // Field completely absent — default should be materialized
        InlineStruct payloadInline = InlineStruct.newBuilder().build();

        StructModel sm = new StructModel();
        sm.initFrom(
                Struct.newBuilder()
                        .setStruct(payloadInline)
                        .setStructDefId(StructDefId.newBuilder()
                                .setName("test-nullable-optional")
                                .build())
                        .build(),
                null);

        model.validateAgainstSuperset(sm, metadataManager);

        // The default must be written into the struct, not merely tolerated.
        assertThat(sm.getInlineStruct().getFields()).containsKey("name");
        assertThat(sm.getInlineStruct().getFields().get("name").getValue().getStrVal())
                .isEqualTo("default");
    }

    @Test
    public void nonNullableOptionalFieldAbsentShouldMaterializeDefault() throws Exception {
        ReadOnlyMetadataManager metadataManager = null;

        // nullable=false with a non-null default => optional; absence should materialize the default
        InlineStructDefModel def = new InlineStructDefModel();
        StructFieldDef sfd = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setIsNullable(false)
                .setDefaultValue(VariableValue.newBuilder().setStr("default").build())
                .build();
        def.getFields().put("name", LHSerializable.fromProto(sfd, StructFieldDefModel.class, null));

        StructDefModel model = new StructDefModel();
        model.setStructDef(def);
        model.setId(new StructDefIdModel("test-non-nullable-optional", 1));

        // Field completely absent — default should be materialized
        InlineStruct payloadInline = InlineStruct.newBuilder().build();

        StructModel sm = new StructModel();
        sm.initFrom(
                Struct.newBuilder()
                        .setStruct(payloadInline)
                        .setStructDefId(StructDefId.newBuilder()
                                .setName("test-non-nullable-optional")
                                .build())
                        .build(),
                null);

        model.validateAgainstSuperset(sm, metadataManager);

        assertThat(sm.getInlineStruct().getFields()).containsKey("name");
        assertThat(sm.getInlineStruct().getFields().get("name").getValue().getStrVal())
                .isEqualTo("default");
    }

    @Test
    public void explicitNullValueShouldNotBeOverriddenByDefault() throws Exception {
        ReadOnlyMetadataManager metadataManager = null;

        // nullable=true with a non-null default, but the payload explicitly provides null.
        // The explicit null must be preserved; the default must NOT override it.
        InlineStructDefModel def = new InlineStructDefModel();
        StructFieldDef sfd = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setIsNullable(true)
                .setDefaultValue(VariableValue.newBuilder().setStr("default").build())
                .build();
        def.getFields().put("name", LHSerializable.fromProto(sfd, StructFieldDefModel.class, null));

        StructDefModel model = new StructDefModel();
        model.setStructDef(def);
        model.setId(new StructDefIdModel("test-explicit-null", 1));

        // Field present with an explicit null value
        InlineStruct payloadInline = InlineStruct.newBuilder()
                .putFields(
                        "name",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder())
                                .build())
                .build();

        StructModel sm = new StructModel();
        sm.initFrom(
                Struct.newBuilder()
                        .setStruct(payloadInline)
                        .setStructDefId(StructDefId.newBuilder()
                                .setName("test-explicit-null")
                                .build())
                        .build(),
                null);

        model.validateAgainstSuperset(sm, metadataManager);

        assertThat(sm.getInlineStruct().getFields()).containsKey("name");
        assertThat(sm.getInlineStruct().getFields().get("name").getValue().isNull())
                .isTrue();
    }

    @Test
    public void materializedDefaultShouldNotMutateSharedStructDef() throws Exception {
        ReadOnlyMetadataManager metadataManager = null;

        InlineStructDefModel def = new InlineStructDefModel();
        StructFieldDef sfd = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setIsNullable(true)
                .setDefaultValue(VariableValue.newBuilder().setStr("default").build())
                .build();
        def.getFields().put("name", LHSerializable.fromProto(sfd, StructFieldDefModel.class, null));

        StructDefModel model = new StructDefModel();
        model.setStructDef(def);
        model.setId(new StructDefIdModel("test-shared-def", 1));

        InlineStruct payloadInline = InlineStruct.newBuilder().build();

        StructModel sm = new StructModel();
        sm.initFrom(
                Struct.newBuilder()
                        .setStruct(payloadInline)
                        .setStructDefId(StructDefId.newBuilder()
                                .setName("test-shared-def")
                                .build())
                        .build(),
                null);

        model.validateAgainstSuperset(sm, metadataManager);

        // Materialized value must be a distinct copy so the cached StructDef is never mutated.
        assertThat(sm.getInlineStruct().getFields().get("name").getValue())
                .isNotSameAs(def.getFields().get("name").getDefaultValue());
    }

    @Test
    public void nullableOptionalFieldPresentWithNullValueShouldPass() throws Exception {
        ReadOnlyMetadataManager metadataManager = null;

        InlineStructDefModel def = new InlineStructDefModel();
        StructFieldDef sfd = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setIsNullable(true)
                .setDefaultValue(VariableValue.newBuilder().setStr("default").build())
                .build();
        def.getFields().put("name", LHSerializable.fromProto(sfd, StructFieldDefModel.class, null));

        StructDefModel model = new StructDefModel();
        model.setStructDef(def);
        model.setId(new StructDefIdModel("test-nullable-optional-null", 1));

        // Field present with null value
        InlineStruct payloadInline = InlineStruct.newBuilder()
                .putFields(
                        "name",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder())
                                .build())
                .build();

        StructModel sm = new StructModel();
        sm.initFrom(
                Struct.newBuilder()
                        .setStruct(payloadInline)
                        .setStructDefId(StructDefId.newBuilder()
                                .setName("test-nullable-optional-null")
                                .build())
                        .build(),
                null);

        // should not throw
        model.validateAgainstSuperset(sm, metadataManager);
    }

    @Test
    public void nonNullableFieldWithNullDefaultValueShouldFailValidation() throws Exception {
        ReadOnlyMetadataManager metadataManager = null;

        // is_nullable=false with a null default_value is incoherent; validate() should reject it
        StructFieldDef sfd = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setIsNullable(false)
                .setDefaultValue(VariableValue.newBuilder().build()) // null VariableValue
                .build();

        StructFieldDefModel fieldDefModel = LHSerializable.fromProto(sfd, StructFieldDefModel.class, null);

        assertThatThrownBy(() -> fieldDefModel.validate(metadataManager))
                .isInstanceOf(StructDefValidationException.class)
                .hasMessageContaining("null default value");
    }
}
