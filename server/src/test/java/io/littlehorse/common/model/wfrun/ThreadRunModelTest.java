package io.littlehorse.common.model.wfrun;

import static org.mockito.ArgumentMatchers.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableMutationModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableMutation;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.TestCoreProcessorContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class ThreadRunModelTest {
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessor = new MockProcessorContext<>();
    private final Headers metadata = HeadersUtil.metadataHeadersFor("my-tenant", "my-principal");
    private final Command dummyCommand = buildCommand();
    private final TestCoreProcessorContext testProcessorContext =
            TestCoreProcessorContext.create(dummyCommand, metadata, mockProcessor);

    private final WfRunModel wfRun = new WfRunModel(testProcessorContext);

    @Test
    public void shouldResolveLocalVariable() {
        wfRun.setId(new WfRunIdModel("my-wf"));
        VariableModel variableModel =
                new VariableModel("myVar", new VariableValueModel("hello"), wfRun.getId(), 0, new WfSpecModel(), false);
        testProcessorContext.getableManager().put(variableModel);
        ThreadRunModel threadRun = new ThreadRunModel(testProcessorContext);
        threadRun.setWfRun(wfRun);
        VariableModel resolvedVariable = threadRun.getVariable("myVar");
        Assertions.assertThat(resolvedVariable).isNotNull();
        Assertions.assertThat(resolvedVariable.getValue().getStrVal()).isEqualTo("hello");
    }

    @Test
    public void shouldAddSafeContextToJsonPathAssignmentError() {
        ThreadRunModel threadRun = threadRunWithCurrentNode("wf-jsonpath-assignment", "task-node");
        VariableAssignmentModel assignment = VariableAssignmentModel.fromProto(
                VariableAssignment.newBuilder()
                        .setLiteralValue(new VariableValueModel(Map.of("secret", "customer-secret-value"))
                                .toProto())
                        .setJsonPath("$[")
                        .build(),
                testProcessorContext);

        Assertions.assertThatThrownBy(() -> threadRun.assignVariable(assignment))
                .isInstanceOf(LHVarSubError.class)
                .hasMessageContaining("wfRunId=wf-jsonpath-assignment")
                .hasMessageContaining("threadRunNumber=0")
                .hasMessageContaining("threadSpecName=entrypoint")
                .hasMessageContaining("nodeRunPosition=1")
                .hasMessageContaining("nodeName=task-node")
                .hasMessageContaining("role=variable_assignment")
                .hasMessageContaining("jsonPath=$[")
                .hasMessageNotContaining("customer-secret-value");
    }

    @Test
    public void shouldAddSafeContextToJsonPathMutationError() {
        ThreadRunModel threadRun = threadRunWithCurrentNode("wf-jsonpath-mutation", "mutation-node");
        VariableMutationModel mutation = VariableMutationModel.fromProto(
                VariableMutation.newBuilder()
                        .setLhsName("input-json")
                        .setOperation(VariableMutationType.ASSIGN)
                        .setNodeOutput(VariableMutation.NodeOutputSource.newBuilder().setJsonpath("$["))
                        .build(),
                testProcessorContext);

        Assertions.assertThatThrownBy(() -> mutation.getRhsValue(
                        threadRun, Map.of(), new VariableValueModel(Map.of("secret", "customer-secret-value"))))
                .isInstanceOf(LHVarSubError.class)
                .hasMessageContaining("wfRunId=wf-jsonpath-mutation")
                .hasMessageContaining("threadRunNumber=0")
                .hasMessageContaining("threadSpecName=entrypoint")
                .hasMessageContaining("nodeRunPosition=1")
                .hasMessageContaining("nodeName=mutation-node")
                .hasMessageContaining("role=mutation_rhs_node_output")
                .hasMessageContaining("jsonPath=$[")
                .hasMessageContaining("mutationVariable=input-json")
                .hasMessageContaining("mutationOperation=ASSIGN")
                .hasMessageNotContaining("customer-secret-value");
    }

    @Nested
    class SharedVariables {
        private final WfSpecModel childWfSpec = TestUtil.wfSpec("my-child-wf");
        private final WfSpecModel parentWfSpec = TestUtil.wfSpec("my-parent-wf");
        private final WfRunIdModel childWfRunId = new WfRunIdModel("my-wf");
        private final WfRunModel childWfRun = new WfRunModel(testProcessorContext);
        private final ThreadRunModel parentThreadRun = new ThreadRunModel(testProcessorContext);
        private final WfRunModel parentWfRun = new WfRunModel(testProcessorContext);
        private final ThreadRunModel childThreadRun = new ThreadRunModel(testProcessorContext);
        private final ThreadVarDefModel inherentVar =
                TestUtil.threadVarDef("my-inherent-var", VariableType.INT, WfRunVariableAccessLevel.INHERITED_VAR);
        private ThreadVarDefModel parentVar;

        @BeforeEach
        public void setup() {
            // Parent WfRun setup
            parentWfRun.setWfSpecId(parentWfSpec.getId());
            parentThreadRun.setWfRun(parentWfRun);
            parentThreadRun.setWfSpecId(parentWfSpec.getId());
            parentThreadRun.setNumber(0);
            parentWfRun.setId(new WfRunIdModel("parent-wf-id"));
            parentWfRun.setThreadRunsUseMeCarefully(new ArrayList<>(List.of(parentThreadRun)));
            parentWfRun.setGreatestThreadRunNumber(1);
            Assertions.assertThat(parentWfRun.getThreadRunIterator().next()).isSameAs(parentThreadRun);
            testProcessorContext.getableManager().put(parentWfRun);

            // Child WfRun setup
            childWfSpec.getThreadSpecs().values().stream()
                    .findFirst()
                    .get()
                    .getVariableDefs()
                    .add(inherentVar);
            childWfRunId.setParentWfRunId(new WfRunIdModel("parent-wf-id"));
            testProcessorContext.metadataManager().put(childWfSpec);
            childWfRun.setId(childWfRunId);
            childThreadRun.setWfRun(this.childWfRun);
            childThreadRun.setWfSpecId(childWfSpec.getId());
        }

        @ParameterizedTest
        @EnumSource(
                value = WfRunVariableAccessLevel.class,
                mode = EnumSource.Mode.INCLUDE,
                names = {"PUBLIC_VAR", "PRIVATE_VAR"})
        public void shouldResolvePublicVariableFromParent(WfRunVariableAccessLevel parentVariableAccessLevel) {
            parentVar = TestUtil.threadVarDef("my-inherent-var", VariableType.INT, parentVariableAccessLevel);
            parentWfSpec.getThreadSpecs().values().stream()
                    .findFirst()
                    .get()
                    .getVariableDefs()
                    .add(parentVar);
            testProcessorContext.metadataManager().put(parentWfSpec);
            VariableModel parentVariable = new VariableModel(
                    "my-inherent-var", new VariableValueModel(2), parentWfRun.getId(), 0, parentWfSpec, false);
            testProcessorContext.getableManager().put(parentVariable);
            VariableModel result = childThreadRun.getVariable("my-inherent-var");
            if (parentVariableAccessLevel == WfRunVariableAccessLevel.PUBLIC_VAR) {
                Assertions.assertThat(result)
                        .isNotNull()
                        .extracting(VariableModel::getValue)
                        .extracting(VariableValueModel::getIntVal)
                        .isEqualTo(2L);
            }
            if (parentVariableAccessLevel == WfRunVariableAccessLevel.PRIVATE_VAR
                    || parentVariableAccessLevel == WfRunVariableAccessLevel.INHERITED_VAR) {
                Assertions.assertThat(result).isNull();
            }
        }
    }

    private Command buildCommand() {
        StopWfRunRequestModel dummyCommand = new StopWfRunRequestModel();
        dummyCommand.wfRunId = new WfRunIdModel(UUID.randomUUID().toString());
        dummyCommand.threadRunNumber = 0;
        return new CommandModel(dummyCommand).toProto().build();
    }

    private ThreadRunModel threadRunWithCurrentNode(String wfRunId, String nodeName) {
        WfRunModel wfRun = new WfRunModel(testProcessorContext);
        wfRun.setId(new WfRunIdModel(wfRunId));
        ThreadRunModel threadRun = new ThreadRunModel(testProcessorContext);
        threadRun.setWfRun(wfRun);
        threadRun.setNumber(0);
        threadRun.setThreadSpecName("entrypoint");
        threadRun.setCurrentNodePosition(1);

        NodeRunModel nodeRun = new NodeRunModel(testProcessorContext);
        nodeRun.setId(new NodeRunIdModel(wfRun.getId(), 0, 1));
        nodeRun.setThreadRun(threadRun);
        nodeRun.setThreadSpecName("entrypoint");
        nodeRun.setNodeName(nodeName);
        testProcessorContext.getableManager().put(nodeRun);
        return threadRun;
    }
}
