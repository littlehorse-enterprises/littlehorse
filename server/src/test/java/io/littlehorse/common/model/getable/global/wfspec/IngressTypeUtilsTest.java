package io.littlehorse.common.model.getable.global.wfspec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.littlehorse.common.exceptions.validation.TypeValidationException;
import io.littlehorse.common.model.getable.core.variable.ArrayModel;
import io.littlehorse.common.model.getable.core.variable.MapModel;
import io.littlehorse.common.model.getable.core.variable.MapModel.MapEntryModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.structdef.InlineArrayDefModel;
import io.littlehorse.common.model.getable.global.structdef.InlineMapDefModel;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
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
}
