package io.littlehorse.common.model.wfrun;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.getable.core.nodeoutput.NodeOutputModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskAttemptModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.TaskNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.NodeOutputReferenceModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableAssignment.PathCase;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.TestCoreProcessorContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.assertj.core.api.Assertions;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.*;
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
            parentWfRun.setId(new WfRunIdModel("parent-wf-id"));
            parentWfRun.getThreadRunsUseMeCarefully().add(parentThreadRun);
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

    @Nested
    class NodeOutputTests {
        private WfSpecModel wfSpec;
        private ThreadSpecModel threadSpec;
        private WfRunModel wfRun;
        private ThreadRunModel threadRun;
        private NodeRunModel nodeRun;

        @BeforeEach
        public void setup() {
            wfSpec = TestUtil.wfSpec("test-wf");
            threadSpec = wfSpec.getThreadSpecs().get("entrypoint");
            wfRun = TestUtil.wfRun("test-wf-run");
            wfRun.setWfSpec(wfSpec);
            wfRun.setWfSpecId(wfSpec.getId());
            threadRun = new ThreadRunModel(testProcessorContext);
            threadRun.setWfRun(wfRun);
            threadRun.setWfSpecId(wfSpec.getId());
            threadRun.setNumber(0);
            threadRun.setThreadSpecName("entrypoint");

            nodeRun = TestUtil.nodeRun("test-wf-run");
            nodeRun.setThreadRun(threadRun);
            nodeRun.setNodeName("task-node");
            nodeRun.setId(new NodeRunIdModel(wfRun.getId(), 0, 0)); // Fix: use threadRun number 0
        }

        @Test
        public void shouldStoreNodeOutputWhenRequired() {
            // Setup: Make the node required by adding it to required node names
            threadSpec.getRequiredNodeNames().add("task-node");

            // Create a completed TaskRun with output
            VariableValueModel output = new VariableValueModel("test-output");
            TaskRunModel taskRun = TestUtil.taskRun();
            taskRun.setStatus(TaskStatus.TASK_SUCCESS);
            TaskAttemptModel attempt = taskRun.getAttempts().get(0);
            attempt.setStatus(TaskStatus.TASK_SUCCESS);
            attempt.setOutput(output);
            testProcessorContext.getableManager().put(taskRun);

            // Set up the node run to reference the completed task
            TaskNodeRunModel taskNodeRun = new TaskNodeRunModel(testProcessorContext);
            taskNodeRun.setTaskRunId(taskRun.getId());
            nodeRun.setTaskRun(taskNodeRun);
            nodeRun.setStatus(LHStatus.COMPLETED);

            Mockito.doCallRealMethod().when(testProcessorContext.getCoreStore()).put(any());
            Mockito.doCallRealMethod().when(testProcessorContext.getCoreStore()).get(anyString(), any(Class.class));

            // Execute
            threadRun.storeNodeOutput(nodeRun);

            // Verify: NodeOutput should be stored
            String expectedKey = Storeable.getGroupedFullStoreKey(wfRun.getId(), StoreableType.NODE_OUTPUT, threadRun.getNumber() + "/" + "task-node");
            NodeOutputModel storedOutput = testProcessorContext.getCoreStore().get(expectedKey, NodeOutputModel.class);
            Assertions.assertThat(storedOutput).isNotNull();
            Assertions.assertThat(storedOutput.getValue().getStrVal()).isEqualTo("test-output");
            Assertions.assertThat(storedOutput.getWfRunId().getId()).isEqualTo("test-wf-run");
        }

        @Test
        public void shouldNotStoreNodeOutputWhenNotRequired() {
            // Setup: Node is not in required node names (default state)

            // Create a completed TaskRun with output
            VariableValueModel output = new VariableValueModel("test-output");
            TaskRunModel taskRun = TestUtil.taskRun();
            taskRun.setStatus(TaskStatus.TASK_SUCCESS);
            TaskAttemptModel attempt = taskRun.getAttempts().get(0);
            attempt.setStatus(TaskStatus.TASK_SUCCESS);
            attempt.setOutput(output);
            testProcessorContext.getableManager().put(taskRun);

            // Set up the node run to reference the completed task
            TaskNodeRunModel taskNodeRun = new TaskNodeRunModel(testProcessorContext);
            taskNodeRun.setTaskRunId(taskRun.getId());
            nodeRun.setTaskRun(taskNodeRun);
            nodeRun.setStatus(LHStatus.COMPLETED);

            // Execute
            threadRun.storeNodeOutput(nodeRun);

            // Verify: No NodeOutput should be stored
            String expectedKey = Storeable.getGroupedFullStoreKey(wfRun.getId(), StoreableType.NODE_OUTPUT, threadRun.getNumber() + "/" + "task-node");
            NodeOutputModel storedOutput = testProcessorContext.getCoreStore().get(expectedKey, NodeOutputModel.class);
            Assertions.assertThat(storedOutput).isNull();
        }

        @Test
        public void shouldNotStoreNodeOutputWhenNoOutput() {
            // Setup: Make the node required but task run has no output
            threadSpec.getRequiredNodeNames().add("task-node");

            // Create a completed TaskRun without output
            TaskRunModel taskRun = TestUtil.taskRun();
            taskRun.setStatus(TaskStatus.TASK_SUCCESS);
            TaskAttemptModel attempt = taskRun.getAttempts().get(0);
            attempt.setStatus(TaskStatus.TASK_SUCCESS);
            // No output set
            testProcessorContext.getableManager().put(taskRun);

            // Set up the node run to reference the completed task
            TaskNodeRunModel taskNodeRun = new TaskNodeRunModel(testProcessorContext);
            taskNodeRun.setTaskRunId(taskRun.getId());
            nodeRun.setTaskRun(taskNodeRun);
            nodeRun.setStatus(LHStatus.COMPLETED);

            // Execute
            threadRun.storeNodeOutput(nodeRun);

            // Verify: No NodeOutput should be stored
            String expectedKey = Storeable.getGroupedFullStoreKey(wfRun.getId(), StoreableType.NODE_OUTPUT, threadRun.getNumber() + "/" + "task-node");
            NodeOutputModel storedOutput = testProcessorContext.getCoreStore().get(expectedKey, NodeOutputModel.class);
            Assertions.assertThat(storedOutput).isNull();
        }

        @Test
        public void shouldRetrieveNodeOutputFromVariableAssignment() throws Exception {
            // Setup: Create a VariableAssignment that uses NODE_OUTPUT
            VariableAssignmentModel assignment = new VariableAssignmentModel();
            assignment.setPathCase(PathCase.PATH_NOT_SET);
            assignment.setRhsSourceType(SourceCase.NODE_OUTPUT);
            NodeOutputReferenceModel nodeRef = new NodeOutputReferenceModel();
            nodeRef.initFrom(io.littlehorse.sdk.common.proto.VariableAssignment.NodeOutputReference.newBuilder().setNodeName("task-node").build(), null);
            assignment.setNodeOutputReference(nodeRef);

            // Store a NodeOutput directly
            VariableValueModel expectedOutput = new VariableValueModel("stored-output");
            NodeOutputModel nodeOutput = new NodeOutputModel(
                    wfRun.getId(), 0, "task-node", expectedOutput, 1);
            testProcessorContext.getCoreStore().put(nodeOutput);

            // Execute: Use assignVariable which internally calls getNodeOutput
            VariableValueModel result = threadRun.assignVariable(assignment);

            // Verify
            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getStrVal()).isEqualTo("stored-output");
        }

        @Test
        public void shouldRetrieveNodeOutputFromNodeRunWhenNotInStore() throws Exception {
            // Setup: VariableAssignment that uses NODE_OUTPUT
            VariableAssignmentModel assignment = new VariableAssignmentModel();
            assignment.setPathCase(PathCase.PATH_NOT_SET);
            assignment.setRhsSourceType(SourceCase.NODE_OUTPUT);
            NodeOutputReferenceModel nodeRef = new NodeOutputReferenceModel();
            nodeRef.initFrom(io.littlehorse.sdk.common.proto.VariableAssignment.NodeOutputReference.newBuilder().setNodeName("task-node").build(), null);
            assignment.setNodeOutputReference(nodeRef);

            // Create a completed TaskRun with output
            VariableValueModel expectedOutput = new VariableValueModel("node-run-output");
            TaskRunModel taskRun = TestUtil.taskRun();
            taskRun.setStatus(TaskStatus.TASK_SUCCESS);
            TaskAttemptModel attempt = taskRun.getAttempts().get(0);
            attempt.setStatus(TaskStatus.TASK_SUCCESS);
            attempt.setOutput(expectedOutput);
            testProcessorContext.getableManager().put(taskRun);

            // Set up the node run to reference the completed task
            TaskNodeRunModel taskNodeRun = new TaskNodeRunModel(testProcessorContext);
            taskNodeRun.setTaskRunId(taskRun.getId());
            nodeRun.setTaskRun(taskNodeRun);
            nodeRun.setStatus(LHStatus.COMPLETED);
            threadRun.putNodeRun(nodeRun);

            // Execute
            VariableValueModel result = threadRun.assignVariable(assignment);

            // Verify
            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getStrVal()).isEqualTo("node-run-output");
        }

        @Test
        public void shouldFailWhenNodeOutputNotFound() {
            // Setup: VariableAssignment that uses NODE_OUTPUT for nonexistent node
            VariableAssignmentModel assignment = new VariableAssignmentModel();
            assignment.setPathCase(PathCase.PATH_NOT_SET);
            assignment.setRhsSourceType(SourceCase.NODE_OUTPUT);
            NodeOutputReferenceModel nodeRef = new NodeOutputReferenceModel();
            nodeRef.initFrom(io.littlehorse.sdk.common.proto.VariableAssignment.NodeOutputReference.newBuilder().setNodeName("nonexistent-node").build(), null);
            assignment.setNodeOutputReference(nodeRef);

            // Execute & Verify: Should throw LHVarSubError
            Assertions.assertThatThrownBy(() -> threadRun.assignVariable(assignment))
                    .isInstanceOf(LHVarSubError.class)
                    .hasMessageContaining("Specified node nonexistent-node has no output.");
        }

        @Test
        public void shouldPreferStoredNodeOutputOverNodeRun() throws Exception {
            // Setup: VariableAssignment that uses NODE_OUTPUT
            VariableAssignmentModel assignment = new VariableAssignmentModel();
            assignment.setPathCase(PathCase.PATH_NOT_SET);
            assignment.setRhsSourceType(SourceCase.NODE_OUTPUT);
            NodeOutputReferenceModel nodeRef = new NodeOutputReferenceModel();
            nodeRef.initFrom(io.littlehorse.sdk.common.proto.VariableAssignment.NodeOutputReference.newBuilder().setNodeName("task-node").build(), null);
            assignment.setNodeOutputReference(nodeRef);

            // Store NodeOutput with one value
            VariableValueModel storedOutput = new VariableValueModel("stored-output");
            NodeOutputModel nodeOutput = new NodeOutputModel(
                    wfRun.getId(), 0, "task-node", storedOutput, 1);
            testProcessorContext.getCoreStore().put(nodeOutput);

            // Create NodeRun with different output
            VariableValueModel nodeRunOutput = new VariableValueModel("node-run-output");
            TaskRunModel taskRun = TestUtil.taskRun();
            taskRun.setStatus(TaskStatus.TASK_SUCCESS);
            TaskAttemptModel attempt = taskRun.getAttempts().get(0);
            attempt.setStatus(TaskStatus.TASK_SUCCESS);
            attempt.setOutput(nodeRunOutput);
            testProcessorContext.getableManager().put(taskRun);

            TaskNodeRunModel taskNodeRun = new TaskNodeRunModel(testProcessorContext);
            taskNodeRun.setTaskRunId(taskRun.getId());
            nodeRun.setTaskRun(taskNodeRun);
            nodeRun.setStatus(LHStatus.COMPLETED);
            threadRun.putNodeRun(nodeRun);

            // Execute
            VariableValueModel result = threadRun.assignVariable(assignment);

            // Verify: Should return stored output, not node run output
            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getStrVal()).isEqualTo("stored-output");
        }
    }

    private Command buildCommand() {
        StopWfRunRequestModel dummyCommand = new StopWfRunRequestModel();
        dummyCommand.wfRunId = new WfRunIdModel(UUID.randomUUID().toString());
        dummyCommand.threadRunNumber = 0;
        return new CommandModel(dummyCommand).toProto().build();
    }
}
