package io.littlehorse.common.model.getable.core.variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructValidationException;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import org.junit.jupiter.api.Test;

public class StructModelMaskingTest {

    @Test
    void shouldValidateRawMaskedFieldAndMaskOnlyWhenSerializingForRequestContext() {
        StructDefModel structDef = createMaskedIntStructDef();
        RequestExecutionContext requestContext = createRequestContext(structDef);

        Struct structProto = Struct.newBuilder()
                .setStructDefId(
                        StructDefId.newBuilder().setName("secret-struct").setVersion(0))
                .setStruct(InlineStruct.newBuilder()
                        .putFields(
                                "secretCode",
                                StructField.newBuilder()
                                        .setValue(VariableValue.newBuilder().setInt(1234))
                                        .build()))
                .build();

        StructModel structModel = StructModel.fromProto(structProto, StructModel.class, requestContext);

        assertThat(structModel
                        .getInlineStruct()
                        .getFields()
                        .get("secretCode")
                        .getValue()
                        .getIntVal())
                .isEqualTo(1234L);

        ReadOnlyMetadataManager metadataManager = mock(ReadOnlyMetadataManager.class);
        when(metadataManager.getLastFromPrefix(anyString(), eq(StructDefModel.class)))
                .thenReturn(structDef);

        assertDoesNotThrow(() -> structModel.validateAgainstStructDefId(metadataManager));

        Struct outputProto = structModel.toProto().build();
        assertThat(outputProto
                        .getStruct()
                        .getFieldsMap()
                        .get("secretCode")
                        .getValue()
                        .getStr())
                .isEqualTo(LHConstants.STRING_MASK);
    }

    @Test
    void shouldStillFailValidationForInvalidRawMaskedFieldValues() {
        StructDefModel structDef = createMaskedIntStructDef();
        RequestExecutionContext requestContext = createRequestContext(structDef);

        Struct structProto = Struct.newBuilder()
                .setStructDefId(
                        StructDefId.newBuilder().setName("secret-struct").setVersion(0))
                .setStruct(InlineStruct.newBuilder()
                        .putFields(
                                "secretCode",
                                StructField.newBuilder()
                                        .setValue(VariableValue.newBuilder().setStr("not-an-int"))
                                        .build()))
                .build();

        StructModel structModel = StructModel.fromProto(structProto, StructModel.class, requestContext);

        ReadOnlyMetadataManager metadataManager = mock(ReadOnlyMetadataManager.class);
        when(metadataManager.getLastFromPrefix(anyString(), eq(StructDefModel.class)))
                .thenReturn(structDef);

        assertThatThrownBy(() -> structModel.validateAgainstStructDefId(metadataManager))
                .isInstanceOf(StructValidationException.class);

        Struct outputProto = structModel.toProto().build();
        assertThat(outputProto
                        .getStruct()
                        .getFieldsMap()
                        .get("secretCode")
                        .getValue()
                        .getStr())
                .isEqualTo(LHConstants.STRING_MASK);
    }

    private static StructDefModel createMaskedIntStructDef() {
        StructDef proto = StructDef.newBuilder()
                .setId(StructDefId.newBuilder().setName("secret-struct").setVersion(0))
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "secretCode",
                                StructFieldDef.newBuilder()
                                        .setFieldType(TypeDefinition.newBuilder()
                                                .setPrimitiveType(VariableType.INT)
                                                .setMasked(true))
                                        .build()))
                .build();

        return StructDefModel.fromProto(proto, mock(RequestExecutionContext.class));
    }

    private static RequestExecutionContext createRequestContext(StructDefModel structDef) {
        RequestExecutionContext requestContext = mock(RequestExecutionContext.class);
        when(requestContext.support(RequestExecutionContext.class)).thenReturn(true);

        WfService service = mock(WfService.class);
        when(service.getStructDef(any())).thenReturn(structDef);
        when(requestContext.service()).thenReturn(service);

        return requestContext;
    }
}
