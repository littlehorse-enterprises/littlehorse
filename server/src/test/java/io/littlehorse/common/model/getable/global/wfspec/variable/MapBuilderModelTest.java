package io.littlehorse.common.model.getable.global.wfspec.variable;

import static org.mockito.Mockito.mock;

import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.core.variable.MapModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.structdef.InlineMapDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.sdk.common.proto.InlineMapDef;
import io.littlehorse.sdk.common.proto.MapBuilder;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class MapBuilderModelTest {

    private ExecutionContext ctx = mock(ExecutionContext.class);

    private MapBuilderModel emptyBuilder() {
        MapBuilderModel m = new MapBuilderModel();
        return m;
    }

    private MapBuilderModel builderWithMapType(VariableType keyType, VariableType valueType) {
        InlineMapDef mapTypePb = InlineMapDef.newBuilder()
                .setKeyType(TypeDefinition.newBuilder().setPrimitiveType(keyType))
                .setValueType(TypeDefinition.newBuilder().setPrimitiveType(valueType))
                .build();
        MapBuilder pb = MapBuilder.newBuilder().setMapType(mapTypePb).build();
        return MapBuilderModel.fromProto(pb, ctx);
    }

    private MapBuilderModel builderFromProto(MapBuilder pb) {
        return MapBuilderModel.fromProto(pb, ctx);
    }

    private VariableAssignment varAssn(String varName) {
        return VariableAssignment.newBuilder().setVariableName(varName).build();
    }

    @Test
    void roundTripWithoutMapType() {
        MapBuilder pb = MapBuilder.newBuilder()
                .addEntries(MapBuilder.Entry.newBuilder().setKey(varAssn("k")).setValue(varAssn("v")))
                .build();
        MapBuilderModel model = builderFromProto(pb);
        MapBuilder rebuilt = model.toProto().build();
        Assertions.assertThat(rebuilt).isEqualTo(pb);
    }

    @Test
    void roundTripWithMapType() {
        MapBuilder pb = MapBuilder.newBuilder()
                .setMapType(InlineMapDef.newBuilder()
                        .setKeyType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                        .setValueType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT)))
                .build();
        MapBuilderModel model = builderFromProto(pb);
        MapBuilder rebuilt = model.toProto().build();
        Assertions.assertThat(rebuilt).isEqualTo(pb);
    }

    @Test
    void resolveTypeDefinitionUsesExplicitMapTypeFirst() throws InvalidExpressionException {
        MapBuilderModel model = builderWithMapType(VariableType.STR, VariableType.INT);
        Optional<TypeDefinitionModel> result = model.resolveTypeDefinition(null, null, null);
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getDefinedTypeCase()).isEqualTo(DefinedTypeCase.INLINE_MAP_DEF);
        Assertions.assertThat(result.get().getInlineMapDef().getKeyType().getPrimitiveType())
                .isEqualTo(VariableType.STR);
    }

    @Test
    void resolveTypeDefinitionDerivesFromEntryWhenNoMapType() throws InvalidExpressionException {
        // Build a builder with one entry where key is a STR variable
        ThreadVarDefModel keyVar = makeVarDef("myKey", VariableType.STR);
        ThreadVarDefModel valVar = makeVarDef("myVal", VariableType.INT);
        ThreadSpecModel threadSpec = makeThreadSpec("entrypoint", keyVar, valVar);
        WfSpecModel wfSpec = makeWfSpec("entrypoint", threadSpec);

        MapBuilderModel.MapBuilderEntryModel entry = entryModel("myKey", "myVal");
        MapBuilderModel model = new MapBuilderModel();
        model.getEntries().add(entry);

        Optional<TypeDefinitionModel> result = model.resolveTypeDefinition(null, wfSpec, "entrypoint");
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getDefinedTypeCase()).isEqualTo(DefinedTypeCase.INLINE_MAP_DEF);
        Assertions.assertThat(result.get().getInlineMapDef().getKeyType().getPrimitiveType())
                .isEqualTo(VariableType.STR);
        Assertions.assertThat(result.get().getInlineMapDef().getValueType().getPrimitiveType())
                .isEqualTo(VariableType.INT);
    }

    @Test
    void resolveTypeDefinitionThrowsWhenEmptyAndNoMapType() {
        MapBuilderModel model = emptyBuilder();
        Assertions.assertThatThrownBy(() -> model.resolveTypeDefinition(null, null, null))
                .isInstanceOf(InvalidExpressionException.class)
                .hasMessageContaining("untyped empty Map");
    }

    @Test
    void validateRejectsNonPrimitiveKeyType() {
        // Key is a literal Map value — resolves to INLINE_MAP_DEF, not primitive
        VariableAssignmentModel keyAssn = new VariableAssignmentModel();
        keyAssn.setRhsSourceType(SourceCase.LITERAL_VALUE);
        MapModel mapVal = new MapModel();
        keyAssn.setRhsLiteralValue(new VariableValueModel(mapVal));

        VariableAssignmentModel valAssn = new VariableAssignmentModel();
        valAssn.setRhsSourceType(SourceCase.LITERAL_VALUE);
        valAssn.setRhsLiteralValue(new VariableValueModel("hello"));

        MapBuilderModel model = new MapBuilderModel();
        model.getEntries().add(makeEntry(keyAssn, valAssn));

        ThreadSpecModel threadSpec = makeThreadSpec("entrypoint");
        WfSpecModel wfSpec = makeWfSpec("entrypoint", threadSpec);

        Assertions.assertThatThrownBy(() -> model.validate(null, null, threadSpec))
                .isInstanceOf(InvalidExpressionException.class)
                .hasMessageContaining("primitive");
    }

    @Test
    void validateRejectsValueTypeIncompatibleWithDeclaredMapType() {
        // mapType declares INT values; entry resolves to STR value
        ThreadVarDefModel keyVar = makeVarDef("myKey", VariableType.STR);
        ThreadVarDefModel valVar = makeVarDef("myVal", VariableType.STR); // STR, not INT
        ThreadSpecModel threadSpec = makeThreadSpec("entrypoint", keyVar, valVar);
        WfSpecModel wfSpec = makeWfSpec("entrypoint", threadSpec);

        InlineMapDefModel mapType = new InlineMapDefModel(
                new TypeDefinitionModel(VariableType.STR), new TypeDefinitionModel(VariableType.INT));
        MapBuilderModel model = new MapBuilderModel();
        model.setMapType(mapType);
        model.getEntries().add(entryModel("myKey", "myVal"));

        Assertions.assertThatThrownBy(() -> model.validate(null, null, threadSpec))
                .isInstanceOf(InvalidExpressionException.class)
                .hasMessageContaining("value type");
    }

    private ThreadVarDefModel makeVarDef(String name, VariableType type) {
        ThreadVarDefModel v = new ThreadVarDefModel();
        v.setAccessLevel(WfRunVariableAccessLevel.PRIVATE_VAR);
        v.setVarDef(new VariableDefModel());
        v.getVarDef().setName(name);
        v.getVarDef().setTypeDef(new TypeDefinitionModel(type));
        return v;
    }

    private ThreadSpecModel makeThreadSpec(String name, ThreadVarDefModel... vars) {
        ThreadSpecModel ts = new ThreadSpecModel();
        ts.setName(name);
        ts.setVariableDefs(List.of(vars));
        return ts;
    }

    private WfSpecModel makeWfSpec(String tsName, ThreadSpecModel ts) {
        WfSpecModel wfSpec = new WfSpecModel();
        wfSpec.setThreadSpecs(Map.of(tsName, ts));
        ts.setWfSpec(wfSpec);
        return wfSpec;
    }

    private MapBuilderModel.MapBuilderEntryModel entryModel(String keyVar, String valVar) {
        VariableAssignmentModel k = new VariableAssignmentModel();
        k.setRhsSourceType(SourceCase.VARIABLE_NAME);
        k.setVariableName(keyVar);

        VariableAssignmentModel v = new VariableAssignmentModel();
        v.setRhsSourceType(SourceCase.VARIABLE_NAME);
        v.setVariableName(valVar);

        return makeEntry(k, v);
    }

    private MapBuilderModel.MapBuilderEntryModel makeEntry(VariableAssignmentModel key, VariableAssignmentModel val) {
        MapBuilderModel.MapBuilderEntryModel e = new MapBuilderModel.MapBuilderEntryModel();
        e.setKey(key);
        e.setValue(val);
        return e;
    }
}
