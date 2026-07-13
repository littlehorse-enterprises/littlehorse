package io.littlehorse.common.model.getable.global.wfspec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.common.exceptions.validation.TypeValidationException;
import io.littlehorse.common.model.getable.core.variable.ArrayModel;
import io.littlehorse.common.model.getable.core.variable.MapModel;
import io.littlehorse.common.model.getable.core.variable.MapModel.MapEntryModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.structdef.InlineArrayDefModel;
import io.littlehorse.common.model.getable.global.structdef.InlineMapDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.proto.Array;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
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
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class IngressTypeUtilsTest {

    private final ReadOnlyMetadataManager metadataManager = mock(ReadOnlyMetadataManager.class);

    private static TypeDefinitionModel arrayOf(TypeDefinitionModel elementType) {
        return new TypeDefinitionModel(new InlineArrayDefModel(elementType));
    }

    private static ArrayModel intArrayWithoutElementType(long... values) {
        ArrayList<VariableValueModel> items = new ArrayList<>();
        for (long value : values) {
            items.add(new VariableValueModel(value));
        }
        // Simulate a client-supplied array that has no authoritative element type set.
        return new ArrayModel(items, null);
    }

    @Test
    void shouldPinElementTypeOnTopLevelArray() throws TypeValidationException {
        TypeDefinitionModel expected = arrayOf(new TypeDefinitionModel(VariableType.INT));
        VariableValueModel value = new VariableValueModel(intArrayWithoutElementType(1L, 2L, 3L));

        IngressTypeUtils.applyExpectedTypeAndValidate(Optional.of(expected), value, metadataManager);

        assertThat(value.getArray().getElementType().getPrimitiveType()).isEqualTo(VariableType.INT);
    }

    @Test
    void shouldPinElementTypeOnNestedArrays() throws TypeValidationException {
        // Array<Array<INT>>
        TypeDefinitionModel expected = arrayOf(arrayOf(new TypeDefinitionModel(VariableType.INT)));

        ArrayList<VariableValueModel> outerItems = new ArrayList<>();
        outerItems.add(new VariableValueModel(intArrayWithoutElementType(1L, 2L, 3L)));
        VariableValueModel value = new VariableValueModel(new ArrayModel(outerItems, null));

        IngressTypeUtils.applyExpectedTypeAndValidate(Optional.of(expected), value, metadataManager);

        // Outer array's element type is Array<INT>.
        assertThat(value.getArray()
                        .getElementType()
                        .getInlineArrayDef()
                        .getArrayType()
                        .getPrimitiveType())
                .isEqualTo(VariableType.INT);

        // The inner array must have its authoritative element type pinned to INT.
        VariableValueModel innerArray = value.getArray().getItems().get(0);
        assertThat(innerArray.getArray().getElementType().getPrimitiveType()).isEqualTo(VariableType.INT);
    }

    @Test
    void shouldPinValueElementTypeForArraysNestedInMaps() throws TypeValidationException {
        // Map<STR, Array<INT>>
        InlineMapDefModel mapDef = new InlineMapDefModel(
                new TypeDefinitionModel(VariableType.STR), arrayOf(new TypeDefinitionModel(VariableType.INT)));
        TypeDefinitionModel expected = new TypeDefinitionModel(mapDef);

        MapEntryModel entry = new MapEntryModel(
                new VariableValueModel("nums"), new VariableValueModel(intArrayWithoutElementType(1L, 2L, 3L)));
        VariableValueModel value = new VariableValueModel(new MapModel(List.of(entry), null));

        IngressTypeUtils.applyExpectedTypeAndValidate(Optional.of(expected), value, metadataManager);

        assertThat(value.getMap().getMapType()).isEqualTo(mapDef);

        // The array value nested inside the map must have its element type pinned to INT.
        VariableValueModel nestedArray = value.getMap().getEntries().get(0).getValue();
        assertThat(nestedArray.getArray().getElementType().getPrimitiveType()).isEqualTo(VariableType.INT);
    }

    @Test
    void shouldLeaveValueUntouchedWhenExpectedTypeAbsent() throws TypeValidationException {
        VariableValueModel value = new VariableValueModel(intArrayWithoutElementType(1L, 2L, 3L));

        IngressTypeUtils.applyExpectedTypeAndValidate(Optional.empty(), value, metadataManager);

        // No expected type => no authoritative element type is pinned.
        assertThat(value.getArray().getElementType()).isNull();
    }

    @Test
    void shouldPinElementTypeOnArrayFieldInsideStruct() throws TypeValidationException {
        // StructDef "my-struct" with a single field "nums" of type Array<INT>.
        StructDef structDefProto = StructDef.newBuilder()
                .setId(StructDefId.newBuilder().setName("my-struct").setVersion(0))
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "nums",
                                StructFieldDef.newBuilder()
                                        .setFieldType(TypeDefinition.newBuilder()
                                                .setInlineArrayDef(InlineArrayDef.newBuilder()
                                                        .setArrayType(TypeDefinition.newBuilder()
                                                                .setPrimitiveType(VariableType.INT))))
                                        .build()))
                .build();
        ExecutionContext context = mock(ExecutionContext.class);
        StructDefModel structDefModel = StructDefModel.fromProto(structDefProto, context);
        when(metadataManager.getLastFromPrefix(StructDefIdModel.getPrefix("my-struct"), StructDefModel.class))
                .thenReturn(structDefModel);

        // A struct value { nums: [1, 2, 3] } whose array field has no authoritative element type.
        VariableValue structValue = VariableValue.newBuilder()
                .setStruct(Struct.newBuilder()
                        .setStructDefId(
                                StructDefId.newBuilder().setName("my-struct").setVersion(-1))
                        .setStruct(InlineStruct.newBuilder()
                                .putFields(
                                        "nums",
                                        StructField.newBuilder()
                                                .setValue(VariableValue.newBuilder()
                                                        .setArray(Array.newBuilder()
                                                                .addItems(VariableValue.newBuilder()
                                                                        .setInt(1))
                                                                .addItems(VariableValue.newBuilder()
                                                                        .setInt(2))
                                                                .addItems(VariableValue.newBuilder()
                                                                        .setInt(3))))
                                                .build())))
                .build();
        VariableValueModel value = VariableValueModel.fromProto(structValue, context);

        TypeDefinitionModel expected = new TypeDefinitionModel(new StructDefIdModel("my-struct", -1));

        IngressTypeUtils.applyExpectedTypeAndValidate(Optional.of(expected), value, metadataManager);

        // The array field inside the struct must have its authoritative element type pinned to INT.
        VariableValueModel numsField =
                value.getStruct().getInlineStruct().getFields().get("nums").getValue();
        assertThat(numsField.getArray().getElementType().getPrimitiveType()).isEqualTo(VariableType.INT);
    }
}
