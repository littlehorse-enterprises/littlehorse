package io.littlehorse.common.model.getable.core.variable;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

import io.littlehorse.common.model.getable.core.variable.MapModel.MapEntryModel;
import io.littlehorse.common.model.getable.global.structdef.InlineMapDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.Map;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MapModelTest {

    private final ExecutionContext context = mock(ExecutionContext.class);

    private static InlineMapDefModel strToIntMapType() {
        return new InlineMapDefModel(
                new TypeDefinitionModel(VariableType.STR), new TypeDefinitionModel(VariableType.INT));
    }

    private static MapModel buildStrToIntMap() {
        List<MapEntryModel> entries = new ArrayList<>();
        entries.add(new MapEntryModel(new VariableValueModel("one"), new VariableValueModel(1L)));
        entries.add(new MapEntryModel(new VariableValueModel("two"), new VariableValueModel(2L)));
        return new MapModel(entries, strToIntMapType());
    }

    @Test
    void shouldRoundTripThroughProto() {
        MapModel original = buildStrToIntMap();

        Map proto = original.toProto().build();
        MapModel reloaded = new MapModel();
        reloaded.initFrom(proto, context);

        assertThat(reloaded.toProto().build()).isEqualTo(proto);
        assertThat(reloaded.getEntries()).hasSize(2);
        assertThat(reloaded.getEntries().get(0).getKey()).isEqualTo(new VariableValueModel("one"));
        assertThat(reloaded.getEntries().get(0).getValue()).isEqualTo(new VariableValueModel(1L));
        assertThat(reloaded.getMapType()).isEqualTo(strToIntMapType());
    }

    @Test
    void shouldPreserveMapTypeInProto() {
        MapModel original = buildStrToIntMap();

        Map proto = original.toProto().build();

        assertThat(proto.hasMapType()).isTrue();
        assertThat(proto.getMapType().getKeyType().getPrimitiveType()).isEqualTo(VariableType.STR);
        assertThat(proto.getMapType().getValueType().getPrimitiveType()).isEqualTo(VariableType.INT);
    }

    @Test
    void shouldOmitMapTypeInProtoWhenNull() {
        List<MapEntryModel> entries = new ArrayList<>();
        entries.add(new MapEntryModel(new VariableValueModel("one"), new VariableValueModel(1L)));
        MapModel original = new MapModel(entries, null);

        Map proto = original.toProto().build();

        assertThat(proto.hasMapType()).isFalse();
        assertThat(proto.getEntriesCount()).isEqualTo(1);
    }

    @Test
    void listConstructorShouldDeepCopyEntries() {
        List<MapEntryModel> entries = new ArrayList<>();
        entries.add(new MapEntryModel(new VariableValueModel("one"), new VariableValueModel(1L)));
        MapModel original = new MapModel(entries, strToIntMapType());

        // Mutating the source list must not affect the constructed MapModel.
        entries.add(new MapEntryModel(new VariableValueModel("two"), new VariableValueModel(2L)));

        assertThat(original.getEntries()).hasSize(1);
    }

    @Test
    void copyConstructorShouldProduceEqualProto() {
        MapModel original = buildStrToIntMap();

        MapModel copy = new MapModel(original);

        assertThat(copy.toProto().build()).isEqualTo(original.toProto().build());
    }

    @Test
    void copyConstructorShouldDeepCopyEntries() {
        MapModel original = buildStrToIntMap();

        MapModel copy = new MapModel(original);

        // The entries list of the copy is independent from the original.
        copy.getEntries().clear();

        assertThat(original.getEntries()).hasSize(2);
    }

    @Test
    void copyConstructorShouldDeepCopyMapType() {
        MapModel original = buildStrToIntMap();

        MapModel copy = new MapModel(original);

        assertThat(copy.getMapType()).isNotSameAs(original.getMapType());
        assertThat(copy.getMapType()).isEqualTo(original.getMapType());
    }

    @Test
    void copyConstructorShouldHandleNullMapType() {
        List<MapEntryModel> entries = new ArrayList<>();
        entries.add(new MapEntryModel(new VariableValueModel("one"), new VariableValueModel(1L)));
        MapModel original = new MapModel(entries, null);

        MapModel copy = new MapModel(original);

        assertThat(copy.getMapType()).isNull();
        assertThat(copy.toProto().build()).isEqualTo(original.toProto().build());
    }

    @Test
    void copyConstructorShouldThrowOnNull() {
        assertThatThrownBy(() -> new MapModel((MapModel) null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getInlineMapDefShouldReturnKeyAndValueTypes() {
        MapModel original = buildStrToIntMap();

        InlineMapDefModel inlineMapDef = original.getInlineMapDef();

        assertThat(inlineMapDef.getKeyType()).isEqualTo(new TypeDefinitionModel(VariableType.STR));
        assertThat(inlineMapDef.getValueType()).isEqualTo(new TypeDefinitionModel(VariableType.INT));
    }

    @Test
    void getInlineMapDefShouldReturnNullTypesWhenMapTypeAbsent() {
        MapModel original = new MapModel();

        InlineMapDefModel inlineMapDef = original.getInlineMapDef();

        assertThat(inlineMapDef.getKeyType()).isNull();
        assertThat(inlineMapDef.getValueType()).isNull();
    }
}
