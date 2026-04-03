package io.littlehorse.common.model.getable.global.structdef;

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
public class StructSupersetValidationTest {

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
}
