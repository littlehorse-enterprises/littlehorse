package io.littlehorse.common.model.getable.global.wfspec.variable;

import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class VariableAssignmentModelTest {

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
}
