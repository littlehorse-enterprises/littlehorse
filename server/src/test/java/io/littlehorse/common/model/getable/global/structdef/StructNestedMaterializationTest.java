package io.littlehorse.common.model.getable.global.structdef;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.proto.Array;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.InlineMapDef;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import org.junit.jupiter.api.Test;

/**
 * Verifies that struct field defaults are materialized for struct elements nested inside native
 * arrays and maps, not just for direct/top-level struct fields.
 */
public class StructNestedMaterializationTest {

    private static final String INNER_STRUCT_NAME = "inner";

    @Test
    public void shouldMaterializeDefaultsInArrayOfStructElements() throws Exception {
        ReadOnlyMetadataManager metadataManager = mockMetadataManager();

        // Type: ARRAY<struct 'inner'>
        TypeDefinitionModel arrayType = TypeDefinitionModel.fromProto(
                TypeDefinition.newBuilder()
                        .setInlineArrayDef(InlineArrayDef.newBuilder().setArrayType(structTypeDef()))
                        .build(),
                null);

        // Value: [ {}, {} ] — two struct elements each missing the defaulted field
        VariableValueModel arrayValue = VariableValueModel.fromProto(
                VariableValue.newBuilder()
                        .setArray(
                                Array.newBuilder().addItems(emptyStructValue()).addItems(emptyStructValue()))
                        .build(),
                null);

        arrayType.validateCompatibility(arrayValue, metadataManager);

        assertThat(arrayValue.getArray().getItems()).hasSize(2);
        for (VariableValueModel item : arrayValue.getArray().getItems()) {
            var fields = item.getStruct().getInlineStruct().getFields();
            assertThat(fields).containsKey("name");
            assertThat(fields.get("name").getValue().getStrVal()).isEqualTo("default");
        }
    }

    @Test
    public void shouldMaterializeDefaultsInMapOfStructValues() throws Exception {
        ReadOnlyMetadataManager metadataManager = mockMetadataManager();

        // Type: MAP<STR, struct 'inner'>
        TypeDefinitionModel mapType = TypeDefinitionModel.fromProto(
                TypeDefinition.newBuilder()
                        .setInlineMapDef(InlineMapDef.newBuilder()
                                .setKeyType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                .setValueType(structTypeDef()))
                        .build(),
                null);

        // Value: { "a": {} } — struct value missing the defaulted field
        VariableValueModel mapValue = VariableValueModel.fromProto(
                VariableValue.newBuilder()
                        .setMap(io.littlehorse.sdk.common.proto.Map.newBuilder()
                                .addEntries(io.littlehorse.sdk.common.proto.Map.Entry.newBuilder()
                                        .setKey(VariableValue.newBuilder().setStr("a"))
                                        .setValue(emptyStructValue())))
                        .build(),
                null);

        mapType.validateCompatibility(mapValue, metadataManager);

        assertThat(mapValue.getMap().getEntries()).hasSize(1);
        var fields = mapValue.getMap()
                .getEntries()
                .get(0)
                .getValue()
                .getStruct()
                .getInlineStruct()
                .getFields();
        assertThat(fields).containsKey("name");
        assertThat(fields.get("name").getValue().getStrVal()).isEqualTo("default");
    }

    private static TypeDefinition structTypeDef() {
        return TypeDefinition.newBuilder()
                .setStructDefId(
                        StructDefId.newBuilder().setName(INNER_STRUCT_NAME).setVersion(0))
                .build();
    }

    private static VariableValue emptyStructValue() {
        return VariableValue.newBuilder()
                .setStruct(Struct.newBuilder()
                        .setStructDefId(StructDefId.newBuilder()
                                .setName(INNER_STRUCT_NAME)
                                .setVersion(0))
                        .setStruct(InlineStruct.newBuilder()))
                .build();
    }

    private static ReadOnlyMetadataManager mockMetadataManager() {
        StructDef proto = StructDef.newBuilder()
                .setId(StructDefId.newBuilder().setName(INNER_STRUCT_NAME).setVersion(0))
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "name",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .setIsNullable(true)
                                        .setDefaultValue(
                                                VariableValue.newBuilder().setStr("default"))
                                        .build()))
                .build();
        StructDefModel structDef = StructDefModel.fromProto(proto, mock(RequestExecutionContext.class));

        ReadOnlyMetadataManager metadataManager = mock(ReadOnlyMetadataManager.class);
        when(metadataManager.get(any(StructDefIdModel.class))).thenReturn(structDef);
        when(metadataManager.getLastFromPrefix(anyString(), eq(StructDefModel.class)))
                .thenReturn(structDef);
        return metadataManager;
    }
}
