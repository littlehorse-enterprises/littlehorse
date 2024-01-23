package io.littlehorse.common.model.getable.global.wfspec;

import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WfSpecModelTest {

    private final WfSpecModel wfSpec = TestUtil.wfSpec("my-wf");
    private final WfSpecModel parentWfSpec = TestUtil.wfSpec("my-parent-wf");
    private final MetadataCommandExecution mockContext = mock(Answers.RETURNS_DEEP_STUBS);
    private ThreadSpecModel childEntrypointThread;
    private VariableDefModel variableDef;

    @BeforeEach
    public void setup() {
        childEntrypointThread = spy(wfSpec.getThreadSpecs().get(wfSpec.getEntrypointThreadName()));
        VariableDef variableDefProto = VariableDef.newBuilder()
                .setName("my-var")
                .setType(VariableType.BOOL)
                .build();
        variableDef = LHSerializable.fromProto(variableDefProto, VariableDefModel.class, mockContext);
        ThreadVarDefModel inheritedVariable =
                new ThreadVarDefModel(variableDef, false, false, WfRunVariableAccessLevel.INHERITED_VAR);
        childEntrypointThread.setVariableDefs(List.of(inheritedVariable));
        wfSpec.setThreadSpecs(Map.of(wfSpec.getEntrypointThreadName(), childEntrypointThread));
    }

    @Test
    public void shouldPreventInheritedVariablesFromParentWfSpec() {
        wfSpec.setParentWfSpec(null);
        verify(childEntrypointThread, never()).validate();
        Throwable caughtException =
                Assertions.catchThrowable(() -> wfSpec.validateAndMaybeBumpVersion(Optional.empty(), mockContext));
        Assertions.assertThat(caughtException)
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("Only child workflows are allowed to access inherited variables");
    }

    @Test
    public void shouldValidateInheritedVariableExists() {
        when(mockContext.service().getWfSpec("my-parent-wf", 1, 0)).thenReturn(parentWfSpec);
        wfSpec.setParentWfSpec(new ParentWfSpecReferenceModel("my-parent-wf", 1));
        verify(childEntrypointThread, never()).validate();
        Throwable caughtException =
                Assertions.catchThrowable(() -> wfSpec.validateAndMaybeBumpVersion(Optional.empty(), mockContext));
        Assertions.assertThat(caughtException)
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("Inherited variable my-var does not exist in parent WfSpec");
    }

    @Test
    public void shouldValidateInheritedIsAccessible() {
        ThreadVarDefModel parentVariable =
                new ThreadVarDefModel(variableDef, false, false, WfRunVariableAccessLevel.PRIVATE_VAR);
        ThreadSpecModel parentEntrypoint = parentWfSpec.getThreadSpecs().get(parentWfSpec.getEntrypointThreadName());
        parentEntrypoint.setVariableDefs(List.of(parentVariable));
        when(mockContext.service().getWfSpec("my-parent-wf", 2, 0)).thenReturn(parentWfSpec);
        wfSpec.setParentWfSpec(new ParentWfSpecReferenceModel("my-parent-wf", 2));
        verify(childEntrypointThread, never()).validate();
        Throwable caughtException =
                Assertions.catchThrowable(() -> wfSpec.validateAndMaybeBumpVersion(Optional.empty(), mockContext));
        Assertions.assertThat(caughtException)
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("Inherited variable my-var is defined as PRIVATE in parent WfSpec");
    }


    @Test
    public void shouldIncreaseTheMajorVersionWhenAPublicVariableChangesItsAccessLevel() {
        WfSpecModel oldVersion = TestUtil.wfSpec("my-wf");
        ThreadSpecModel entrypointThread = spy(wfSpec.getThreadSpecs().get(wfSpec.getEntrypointThreadName()));
        doNothing().when(entrypointThread).validate();
        entrypointThread.setVariableDefs(List.of());
        wfSpec.setThreadSpecs(Map.of(wfSpec.getEntrypointThreadName(), entrypointThread));
        Assertions.assertThat(wfSpec.getId().getMajorVersion()).isEqualTo(0);
        wfSpec.validateAndMaybeBumpVersion(Optional.of(oldVersion), mockContext);
        Assertions.assertThat(wfSpec.getId().getMajorVersion()).isEqualTo(1);
    }
}
