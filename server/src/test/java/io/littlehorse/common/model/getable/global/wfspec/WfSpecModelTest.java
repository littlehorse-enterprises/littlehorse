package io.littlehorse.common.model.getable.global.wfspec;

import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.exceptions.validation.InvalidThreadSpecException;
import io.littlehorse.common.exceptions.validation.InvalidWfSpecException;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WfSpecModelTest {

    private final WfSpecModel wfSpec = TestUtil.wfSpec("my-wf");
    private final WfSpecModel parentWfSpec = TestUtil.wfSpec("my-parent-wf");
    private final MetadataProcessorContext mockContext = mock(Answers.RETURNS_DEEP_STUBS);
    private ThreadSpecModel childEntrypointThread;
    private VariableDefModel variableDef;

    @BeforeEach
    public void setup() {
        childEntrypointThread = spy(new ThreadSpecModel());
        VariableDef variableDefProto = VariableDef.newBuilder()
                .setName("my-var")
                .setTypeDef(TypeDefinition.newBuilder().setType(VariableType.BOOL))
                .build();
        variableDef = LHSerializable.fromProto(variableDefProto, VariableDefModel.class, mockContext);
        ThreadVarDefModel inheritedVariable =
                new ThreadVarDefModel(variableDef, false, false, WfRunVariableAccessLevel.INHERITED_VAR);
        childEntrypointThread.setVariableDefs(List.of(inheritedVariable));
        wfSpec.setThreadSpecs(Map.of(wfSpec.getEntrypointThreadName(), childEntrypointThread));
    }

    @Test
    public void shouldPreventInheritedVariablesFromParentWfSpec() throws InvalidThreadSpecException {
        wfSpec.setParentWfSpec(null);
        verify(childEntrypointThread, never()).validate(Mockito.any());
        Throwable caughtException =
                Assertions.catchThrowable(() -> wfSpec.validateAndMaybeBumpVersion(Optional.empty(), mockContext));
        Assertions.assertThat(caughtException)
                .isNotNull()
                .isInstanceOf(InvalidWfSpecException.class)
                .hasMessageContaining("Only child workflows are allowed to access INHERITED variables");
    }

    @Test
    public void shouldValidateInheritedVariableExists() throws InvalidThreadSpecException {
        when(mockContext.service().getWfSpec("my-parent-wf", 1, 0)).thenReturn(parentWfSpec);
        wfSpec.setParentWfSpec(new ParentWfSpecReferenceModel("my-parent-wf", 1));
        verify(childEntrypointThread, never()).validate(Mockito.any());
        Throwable caughtException =
                Assertions.catchThrowable(() -> wfSpec.validateAndMaybeBumpVersion(Optional.empty(), mockContext));
        Assertions.assertThat(caughtException)
                .isNotNull()
                .isInstanceOf(InvalidWfSpecException.class)
                .hasMessageContaining("INHERITED variable my-var does not exist in parent WfSpec");
    }

    @Test
    public void shouldValidateInheritedIsAccessible() throws InvalidThreadSpecException {
        ThreadVarDefModel parentVariable =
                new ThreadVarDefModel(variableDef, false, false, WfRunVariableAccessLevel.PRIVATE_VAR);
        ThreadSpecModel parentEntrypoint = parentWfSpec.getThreadSpecs().get(parentWfSpec.getEntrypointThreadName());
        parentEntrypoint.setVariableDefs(List.of(parentVariable));
        when(mockContext.service().getWfSpec("my-parent-wf", 2, 0)).thenReturn(parentWfSpec);
        wfSpec.setParentWfSpec(new ParentWfSpecReferenceModel("my-parent-wf", 2));
        verify(childEntrypointThread, never()).validate(Mockito.any());
        Throwable caughtException =
                Assertions.catchThrowable(() -> wfSpec.validateAndMaybeBumpVersion(Optional.empty(), mockContext));
        Assertions.assertThat(caughtException)
                .isNotNull()
                .isInstanceOf(InvalidWfSpecException.class)
                .hasMessageContaining("Inherited variable my-var is defined as PRIVATE in parent WfSpec");
    }

    @Test
    public void shouldIncreaseTheMajorVersionWhenAPublicVariableChangesItsAccessLevelToPrivate()
            throws InvalidThreadSpecException, InvalidWfSpecException {
        WfSpecModel oldVersion = TestUtil.wfSpec("my-wf");
        ThreadVarDefModel publicVariable =
                new ThreadVarDefModel(variableDef, false, false, WfRunVariableAccessLevel.PUBLIC_VAR);
        ThreadVarDefModel privateVariable =
                new ThreadVarDefModel(variableDef, false, false, WfRunVariableAccessLevel.PRIVATE_VAR);
        ThreadSpecModel entrypointThread = spy(new ThreadSpecModel());
        doNothing().when(entrypointThread).validate(Mockito.any());
        oldVersion.getEntrypointThread().setVariableDefs(List.of(publicVariable));
        entrypointThread.setVariableDefs(List.of(privateVariable));
        wfSpec.setThreadSpecs(Map.of(wfSpec.getEntrypointThreadName(), entrypointThread));
        Assertions.assertThat(wfSpec.getId().getMajorVersion()).isEqualTo(0);
        wfSpec.validateAndMaybeBumpVersion(Optional.of(oldVersion), mockContext);
        Assertions.assertThat(wfSpec.getId().getMajorVersion()).isEqualTo(1);
    }

    @Test
    public void shouldNotIncreaseTheMajorVersionWhenAInheritedVariableChangesItsAccessLevelToPrivate()
            throws LHValidationException {
        WfSpecModel oldVersion = TestUtil.wfSpec("my-parent-wf");
        ThreadVarDefModel inheritedVar =
                new ThreadVarDefModel(variableDef, false, false, WfRunVariableAccessLevel.INHERITED_VAR);
        ThreadVarDefModel privateVariable =
                new ThreadVarDefModel(variableDef, false, false, WfRunVariableAccessLevel.PRIVATE_VAR);

        ThreadSpecModel realThreadSpec = new ThreadSpecModel();
        ThreadSpecModel entrypointThread = spy(realThreadSpec);
        doNothing().when(entrypointThread).validate(Mockito.any());
        oldVersion.getEntrypointThread().setVariableDefs(List.of(inheritedVar));
        when(mockContext.service().getWfSpec("my-parent-wf", 2, 0)).thenReturn(oldVersion);
        wfSpec.setParentWfSpec(new ParentWfSpecReferenceModel("my-parent-wf", 2));
        entrypointThread.setVariableDefs(List.of(privateVariable));
        wfSpec.setThreadSpecs(Map.of(wfSpec.getEntrypointThreadName(), entrypointThread));
        Assertions.assertThat(wfSpec.getId().getMajorVersion()).isEqualTo(0);
        wfSpec.validateAndMaybeBumpVersion(Optional.of(oldVersion), mockContext);
        Assertions.assertThat(wfSpec.getId().getMajorVersion()).isEqualTo(0);
        Assertions.assertThat(wfSpec.getId().getRevision()).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("provideBreakingChangeArguments")
    public void shouldNotIncreaseTheMajorVersionWhenVariablesAreStillAccessibleFromChildWorkflows(
            WfRunVariableAccessLevel from, WfRunVariableAccessLevel to) throws LHValidationException {
        WfSpecModel oldVersion = TestUtil.wfSpec("my-parent-wf");
        ThreadVarDefModel fromVar = new ThreadVarDefModel(variableDef, false, false, from);
        ThreadVarDefModel toVar = new ThreadVarDefModel(variableDef, false, false, to);
        ThreadSpecModel entrypointThread = spy(new ThreadSpecModel());
        doNothing().when(entrypointThread).validate(Mockito.any());
        oldVersion.getEntrypointThread().setVariableDefs(List.of(fromVar));
        if (to == WfRunVariableAccessLevel.INHERITED_VAR) {
            when(mockContext.service().getWfSpec("my-parent-wf", 2, 0)).thenReturn(oldVersion);
            wfSpec.setParentWfSpec(new ParentWfSpecReferenceModel("my-parent-wf", 2));
        }
        entrypointThread.setVariableDefs(List.of(toVar));
        wfSpec.setThreadSpecs(Map.of(wfSpec.getEntrypointThreadName(), entrypointThread));
        Assertions.assertThat(wfSpec.getId().getMajorVersion()).isEqualTo(0);
        wfSpec.validateAndMaybeBumpVersion(Optional.of(oldVersion), mockContext);
        Assertions.assertThat(wfSpec.getId().getMajorVersion()).isEqualTo(1);
    }

    /*
    Access level combinations when the major version shouldn't increase
     */
    private static Stream<Arguments> provideBreakingChangeArguments() {
        return Stream.of(
                Arguments.of(WfRunVariableAccessLevel.PUBLIC_VAR, WfRunVariableAccessLevel.INHERITED_VAR),
                Arguments.of(WfRunVariableAccessLevel.PRIVATE_VAR, WfRunVariableAccessLevel.PUBLIC_VAR),
                Arguments.of(WfRunVariableAccessLevel.INHERITED_VAR, WfRunVariableAccessLevel.PUBLIC_VAR));
    }
}
