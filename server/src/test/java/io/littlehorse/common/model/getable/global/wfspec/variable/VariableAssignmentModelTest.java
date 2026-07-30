package io.littlehorse.common.model.getable.global.wfspec.variable;

import static org.mockito.Mockito.mock;

import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.structdef.InlineArrayDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.sdk.common.proto.InlineMapDef;
import io.littlehorse.sdk.common.proto.MapBuilder;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class VariableAssignmentModelTest {

    private ExecutionContext ctx = mock(ExecutionContext.class);

    @Test
    void ifJsonPathNullDontReturnEmptyType() throws LHValidationException {
        VariableAssignmentModel varAssn = new VariableAssignmentModel();
        varAssn.setVariableName("my-json-var");
        varAssn.setRhsSourceType(SourceCase.VARIABLE_NAME);

        ThreadVarDefModel jsonVar = new ThreadVarDefModel();
        jsonVar.setAccessLevel(WfRunVariableAccessLevel.PRIVATE_VAR);
        jsonVar.setVarDef(new VariableDefModel());
        jsonVar.getVarDef().setTypeDef(new TypeDefinitionModel(VariableType.JSON_OBJ));
        jsonVar.getVarDef().setName("my-json-var");
        ;

        ThreadSpecModel threadSpec = new ThreadSpecModel();
        threadSpec.setVariableDefs(List.of(jsonVar));

        WfSpecModel wfSpec = new WfSpecModel();
        wfSpec.setThreadSpecs(Map.of("entrypoint", threadSpec));
        threadSpec.setName("entrypoint");
        threadSpec.setWfSpec(wfSpec);

        Optional<TypeDefinitionModel> resolvedType = varAssn.resolveType(null, wfSpec, "entrypoint");
        Assertions.assertThat(resolvedType).isPresent();
    }

    @Test
    void ifJsonPathNotNullReturnEmptyType() throws LHValidationException {
        VariableAssignmentModel varAssn = new VariableAssignmentModel();
        varAssn.setVariableName("my-json-var");
        varAssn.setJsonPath("$.somePath");
        varAssn.setRhsSourceType(SourceCase.VARIABLE_NAME);

        ThreadVarDefModel jsonVar = new ThreadVarDefModel();
        jsonVar.setAccessLevel(WfRunVariableAccessLevel.PRIVATE_VAR);
        jsonVar.setVarDef(new VariableDefModel());
        jsonVar.getVarDef().setTypeDef(new TypeDefinitionModel(VariableType.JSON_OBJ));

        ThreadSpecModel threadSpec = new ThreadSpecModel();
        threadSpec.setVariableDefs(List.of(jsonVar));

        WfSpecModel wfSpec = new WfSpecModel();
        wfSpec.setThreadSpecs(Map.of("entrypoint", threadSpec));
        threadSpec.setName("entrypoint");
        threadSpec.setWfSpec(wfSpec);

        Optional<TypeDefinitionModel> resolvedType = varAssn.resolveType(null, wfSpec, "entrypoint");
        Assertions.assertThat(resolvedType).isEmpty();
    }

    @Test
    void shouldResolveSizeOfToIntForArrayOperands() throws LHValidationException, InvalidExpressionException {
        ThreadVarDefModel arrayVar = new ThreadVarDefModel();
        arrayVar.setAccessLevel(WfRunVariableAccessLevel.PRIVATE_VAR);
        arrayVar.setVarDef(new VariableDefModel());
        arrayVar.getVarDef().setName("my-array-var");
        arrayVar.getVarDef()
                .setTypeDef(
                        new TypeDefinitionModel(new InlineArrayDefModel(new TypeDefinitionModel(VariableType.INT))));

        ThreadSpecModel threadSpec = new ThreadSpecModel();
        threadSpec.setVariableDefs(List.of(arrayVar));

        WfSpecModel wfSpec = new WfSpecModel();
        wfSpec.setThreadSpecs(Map.of("entrypoint", threadSpec));
        threadSpec.setName("entrypoint");
        threadSpec.setWfSpec(wfSpec);

        VariableAssignmentModel varAssn = getSizeOfAssignment("my-array-var");
        Optional<TypeDefinitionModel> resolvedType = varAssn.resolveType(null, wfSpec, "entrypoint");
        Assertions.assertThat(resolvedType).isPresent();
        Assertions.assertThat(resolvedType.get().getPrimitiveType()).isEqualTo(VariableType.INT);
    }

    @Test
    void shouldResolveSizeOfToIntForStringOperands() throws LHValidationException, InvalidExpressionException {
        ThreadVarDefModel stringVar = new ThreadVarDefModel();
        stringVar.setAccessLevel(WfRunVariableAccessLevel.PRIVATE_VAR);
        stringVar.setVarDef(new VariableDefModel());
        stringVar.getVarDef().setName("my-string-var");
        stringVar.getVarDef().setTypeDef(new TypeDefinitionModel(VariableType.STR));

        ThreadSpecModel threadSpec = new ThreadSpecModel();
        threadSpec.setVariableDefs(List.of(stringVar));

        WfSpecModel wfSpec = new WfSpecModel();
        wfSpec.setThreadSpecs(Map.of("entrypoint", threadSpec));
        threadSpec.setName("entrypoint");
        threadSpec.setWfSpec(wfSpec);

        VariableAssignmentModel varAssn = getSizeOfAssignment("my-string-var");
        Optional<TypeDefinitionModel> resolvedType = varAssn.resolveType(null, wfSpec, "entrypoint");
        Assertions.assertThat(resolvedType).isPresent();
        Assertions.assertThat(resolvedType.get().getPrimitiveType()).isEqualTo(VariableType.INT);
    }

    @Test
    void shouldRejectSizeOfForNonCollectionOperandTypes() throws LHValidationException {
        ThreadVarDefModel intVar = new ThreadVarDefModel();
        intVar.setAccessLevel(WfRunVariableAccessLevel.PRIVATE_VAR);
        intVar.setVarDef(new VariableDefModel());
        intVar.getVarDef().setName("my-int-var");
        intVar.getVarDef().setTypeDef(new TypeDefinitionModel(VariableType.INT));

        ThreadSpecModel threadSpec = new ThreadSpecModel();
        threadSpec.setVariableDefs(List.of(intVar));

        WfSpecModel wfSpec = new WfSpecModel();
        wfSpec.setThreadSpecs(Map.of("entrypoint", threadSpec));
        threadSpec.setName("entrypoint");
        threadSpec.setWfSpec(wfSpec);

        VariableAssignmentModel varAssn = getSizeOfAssignment("my-int-var");
        Assertions.assertThatThrownBy(() -> varAssn.resolveType(null, wfSpec, "entrypoint"))
                .isInstanceOf(InvalidExpressionException.class)
                .hasMessageContaining("size()");
    }

    private VariableAssignmentModel getSizeOfAssignment(String variableName) {
        VariableAssignmentModel operand = new VariableAssignmentModel();
        operand.setVariableName(variableName);
        operand.setRhsSourceType(SourceCase.VARIABLE_NAME);

        SizeOfModel sizeOf = new SizeOfModel();
        sizeOf.setOperand(operand);

        VariableAssignmentModel varAssn = new VariableAssignmentModel();
        varAssn.setSizeOf(sizeOf);
        varAssn.setRhsSourceType(SourceCase.SIZE_OF);
        return varAssn;
    }

    @Test
    void mapBuilderProtoRoundTrip() {
        InlineMapDef mapTypePb = InlineMapDef.newBuilder()
                .setKeyType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setValueType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                .build();
        MapBuilder pbMapBuilder = MapBuilder.newBuilder()
                .addEntries(MapBuilder.Entry.newBuilder()
                        .setKey(VariableAssignment.newBuilder().setVariableName("k"))
                        .setValue(VariableAssignment.newBuilder().setVariableName("v")))
                .setMapType(mapTypePb)
                .build();
        VariableAssignment proto =
                VariableAssignment.newBuilder().setMapBuilder(pbMapBuilder).build();

        VariableAssignmentModel model = VariableAssignmentModel.fromProto(proto, ctx);
        VariableAssignment rebuilt = model.toProto().build();
        Assertions.assertThat(rebuilt).isEqualTo(proto);
    }

    @Test
    void mapBuilderGetSourceTypeReturnsInlineMapDefWhenMapTypeSet() throws Exception {
        InlineMapDef mapTypePb = InlineMapDef.newBuilder()
                .setKeyType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setValueType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                .build();
        VariableAssignment proto = VariableAssignment.newBuilder()
                .setMapBuilder(MapBuilder.newBuilder().setMapType(mapTypePb))
                .build();

        WfSpecModel wfSpec = new WfSpecModel();
        ThreadSpecModel threadSpec = new ThreadSpecModel();
        threadSpec.setName("entrypoint");
        wfSpec.setThreadSpecs(Map.of("entrypoint", threadSpec));
        threadSpec.setWfSpec(wfSpec);

        VariableAssignmentModel model = VariableAssignmentModel.fromProto(proto, ctx);
        Optional<TypeDefinitionModel> result = model.getSourceType(null, wfSpec, "entrypoint");
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getDefinedTypeCase())
                .isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_MAP_DEF);
    }

    @Test
    void mapBuilderGetSourceTypeDerivesFromEntries() throws Exception {
        ThreadVarDefModel keyVar = new ThreadVarDefModel();
        keyVar.setAccessLevel(WfRunVariableAccessLevel.PRIVATE_VAR);
        keyVar.setVarDef(new VariableDefModel());
        keyVar.getVarDef().setName("kVar");
        keyVar.getVarDef().setTypeDef(new TypeDefinitionModel(VariableType.STR));

        ThreadVarDefModel valVar = new ThreadVarDefModel();
        valVar.setAccessLevel(WfRunVariableAccessLevel.PRIVATE_VAR);
        valVar.setVarDef(new VariableDefModel());
        valVar.getVarDef().setName("vVar");
        valVar.getVarDef().setTypeDef(new TypeDefinitionModel(VariableType.INT));

        ThreadSpecModel threadSpec = new ThreadSpecModel();
        threadSpec.setVariableDefs(List.of(keyVar, valVar));
        WfSpecModel wfSpec = new WfSpecModel();
        wfSpec.setThreadSpecs(Map.of("entrypoint", threadSpec));
        threadSpec.setName("entrypoint");
        threadSpec.setWfSpec(wfSpec);

        VariableAssignment proto = VariableAssignment.newBuilder()
                .setMapBuilder(MapBuilder.newBuilder()
                        .addEntries(MapBuilder.Entry.newBuilder()
                                .setKey(VariableAssignment.newBuilder().setVariableName("kVar"))
                                .setValue(VariableAssignment.newBuilder().setVariableName("vVar"))))
                .build();

        VariableAssignmentModel model = VariableAssignmentModel.fromProto(proto, ctx);
        Optional<TypeDefinitionModel> result = model.getSourceType(null, wfSpec, "entrypoint");
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getDefinedTypeCase())
                .isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_MAP_DEF);
        Assertions.assertThat(result.get().getInlineMapDef().getKeyType().getPrimitiveType())
                .isEqualTo(VariableType.STR);
    }

    @Test
    void mapBuilderGetSourceTypeEmptyBuilderReturnsWildcardMap() throws Exception {
        VariableAssignment proto = VariableAssignment.newBuilder()
                .setMapBuilder(MapBuilder.newBuilder())
                .build();

        WfSpecModel wfSpec = new WfSpecModel();
        ThreadSpecModel threadSpec = new ThreadSpecModel();
        threadSpec.setName("entrypoint");
        wfSpec.setThreadSpecs(Map.of("entrypoint", threadSpec));
        threadSpec.setWfSpec(wfSpec);

        VariableAssignmentModel model = VariableAssignmentModel.fromProto(proto, ctx);
        Optional<TypeDefinitionModel> result = model.getSourceType(null, wfSpec, "entrypoint");
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getDefinedTypeCase())
                .isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_MAP_DEF);
    }

    @Test
    void mapBuilderGetRequiredVariableNamesReturnsBothKeyAndValueVars() {
        VariableAssignment proto = VariableAssignment.newBuilder()
                .setMapBuilder(MapBuilder.newBuilder()
                        .addEntries(MapBuilder.Entry.newBuilder()
                                .setKey(VariableAssignment.newBuilder().setVariableName("myKey"))
                                .setValue(VariableAssignment.newBuilder().setVariableName("myValue"))))
                .build();

        VariableAssignmentModel model = VariableAssignmentModel.fromProto(proto, ctx);
        Collection<String> names = model.getRequiredVariableNames();
        Assertions.assertThat(names).contains("myKey", "myValue");
    }
}
