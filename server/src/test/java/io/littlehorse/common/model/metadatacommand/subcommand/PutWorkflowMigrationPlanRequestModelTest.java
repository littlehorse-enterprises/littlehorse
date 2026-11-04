package io.littlehorse.common.model.metadatacommand.subcommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.grpc.Status.Code;
import io.littlehorse.TestUtil;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.migrations.NodeMigrationPlanModel;
import io.littlehorse.common.model.getable.global.migrations.ThreadMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.StartThreadNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.TaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.PutWorkflowMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.TaskNode.TaskToExecuteCase;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.sdk.common.proto.WorkflowMigrationPlan;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PutWorkflowMigrationPlanRequestModelTest {

    private final MetadataProcessorContext mockContext = mock(Answers.RETURNS_DEEP_STUBS);

    private PutWorkflowMigrationPlanRequestModel buildRequest(
            String name,
            WfSpecIdModel oldWfSpecId,
            int majorVersion,
            int revision,
            Map<String, ThreadMigrationPlanModel> migrations) {
        PutWorkflowMigrationPlanRequest.Builder proto = PutWorkflowMigrationPlanRequest.newBuilder()
                .setName(name)
                .setOldWfSpec(oldWfSpecId.toProto())
                .setMajorVersion(majorVersion)
                .setRevision(revision);
        migrations.forEach((k, v) -> {
            ThreadMigrationPlanRequest.Builder reqBuilder =
                    ThreadMigrationPlanRequest.newBuilder().setNewThreadName(v.getNewThreadName());
            v.getNodeMigrations()
                    .forEach((node, nodeMig) ->
                            reqBuilder.putNodeMigrations(node, nodeMig.toProto().build()));
            proto.putThreadMigrations(k, reqBuilder.build());
        });
        return LHSerializable.fromProto(proto.build(), PutWorkflowMigrationPlanRequestModel.class, mockContext);
    }

    private WfSpecModel buildWfSpec(String name, int major, int revision, String... nodeNames) {
        WfSpecModel spec = TestUtil.wfSpec(name);
        spec.setId(new WfSpecIdModel(name, major, revision));
        Map<String, NodeModel> nodes = new HashMap<>();
        for (String nodeName : nodeNames) {
            nodes.put(nodeName, TestUtil.node());
        }
        spec.getThreadSpecs().get("entrypoint").setNodes(nodes);
        return spec;
    }

    private ThreadSpecModel buildThread(String name, WfSpecModel spec, Map<String, NodeModel> nodes) {
        ThreadSpecModel thread = new ThreadSpecModel();
        thread.setName(name);
        thread.wfSpec = spec;
        thread.setNodes(new HashMap<>(nodes));
        thread.setVariableDefs(new ArrayList<>());
        return thread;
    }

    /**
     * Builds a ThreadMigrationPlanModel with a single node migration (fromNode -> toNode)
     * and an empty dependencies list (safe to mutate).
     */
    private ThreadMigrationPlanModel migration(String newThread, String fromNode, String toNode) {
        ThreadMigrationPlanModel m = new ThreadMigrationPlanModel();
        m.setNewThreadName(newThread);
        NodeMigrationPlanModel nodeMigration = new NodeMigrationPlanModel();
        nodeMigration.setNewNodeName(toNode);
        Map<String, NodeMigrationPlanModel> nodeMigrations = new HashMap<>();
        nodeMigrations.put(fromNode, nodeMigration);
        m.setNodeMigrations(nodeMigrations);
        m.setThreadSpecDependencies(new ArrayList<>());
        return m;
    }

    /**
     * Builds a TASK node whose input references the given wfRun variable, so that the
     * thread spec it belongs to reports {@code varName} via getNamesOfVariablesUsed().
     */
    private NodeModel nodeUsingVar(String varName) {
        NodeModel node = new NodeModel();
        TaskNodeModel taskNode = new TaskNodeModel();
        taskNode.setTaskDefId(new TaskDefIdModel("test-task-def-name"));
        taskNode.setTaskToExecuteType(TaskToExecuteCase.TASK_DEF_ID);
        VariableAssignmentModel assignment = new VariableAssignmentModel();
        assignment.setRhsSourceType(SourceCase.VARIABLE_NAME);
        assignment.setVariableName(varName);
        taskNode.setVariables(List.of(assignment));
        node.setTaskNode(taskNode);
        node.setType(NodeCase.TASK);
        return node;
    }

    /**
     * Stubs getWfSpec() to return newSpec on the first call and oldSpec on the second.
     * Order matches process(): newWfSpec is fetched first, oldWfSpec second.
     */
    private void stubSpecs(WfSpecModel newSpec, WfSpecModel oldSpec) {
        when(mockContext.service().getWfSpec(any(WfSpecIdModel.class)))
                .thenReturn(newSpec)
                .thenReturn(oldSpec);
    }

    // ---- Tests ----

    @Test
    void shouldSuccessfullyProcessValidMigrationPlan() {
        WfSpecModel oldSpec = buildWfSpec("my-wf", 0, 0, "old-node");
        WfSpecModel newSpec = buildWfSpec("my-wf", 1, 0, "new-node");
        stubSpecs(newSpec, oldSpec);

        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("entrypoint", migration("entrypoint", "old-node", "new-node")));

        // No exception means validation passed and metadataManager.put() was called (no-op on mock)
        assertThat(catchThrowable(() -> request.process(mockContext))).isNull();
    }

    @Test
    void shouldRejectWhenNewWfSpecDoesNotExist() {
        // First call to getWfSpec (for newWfSpec) returns null
        when(mockContext.service().getWfSpec(any(WfSpecIdModel.class))).thenReturn(null);

        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("entrypoint", migration("entrypoint", "old-node", "new-node")));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("Destination WfSpec")
                .hasMessageContaining("does not exist");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.NOT_FOUND);
    }

    @Test
    void shouldRejectWhenOldWfSpecDoesNotExist() {
        WfSpecModel newSpec = buildWfSpec("my-wf", 1, 0, "new-node");
        when(mockContext.service().getWfSpec(any(WfSpecIdModel.class)))
                .thenReturn(newSpec) // first call: newWfSpec found
                .thenReturn(null); // second call: oldWfSpec not found

        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("entrypoint", migration("entrypoint", "old-node", "new-node")));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("Source WfSpec")
                .hasMessageContaining("does not exist");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.NOT_FOUND);
    }

    @Test
    void shouldRejectWhenOldThreadNameNotInOldWfSpec() {
        WfSpecModel oldSpec = buildWfSpec("my-wf", 0, 0, "old-node");
        WfSpecModel newSpec = buildWfSpec("my-wf", 1, 0, "new-node");
        stubSpecs(newSpec, oldSpec);

        // "missing-thread" does not exist in oldSpec.threadSpecs
        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("missing-thread", migration("entrypoint", "old-node", "new-node")));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("Source WfSpec has no threadSpec missing-thread");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
    }

    @Test
    void shouldRejectWhenFromNodeNotInOldThread() {
        WfSpecModel oldSpec = buildWfSpec("my-wf", 0, 0, "old-node");
        WfSpecModel newSpec = buildWfSpec("my-wf", 1, 0, "new-node");
        stubSpecs(newSpec, oldSpec);

        // "bad-node" does not exist in the entrypoint thread of oldSpec
        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("entrypoint", migration("entrypoint", "bad-node", "new-node")));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown).isInstanceOf(LHApiException.class).hasMessageContaining("does not have node bad-node");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
    }

    @Test
    void shouldRejectWhenNewThreadNotInNewWfSpec() {
        WfSpecModel oldSpec = buildWfSpec("my-wf", 0, 0, "old-node");
        WfSpecModel newSpec = buildWfSpec("my-wf", 1, 0, "new-node");
        stubSpecs(newSpec, oldSpec);

        // "missing-thread" does not exist in newSpec.threadSpecs
        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("entrypoint", migration("missing-thread", "old-node", "new-node")));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("Destination WfSpec has no threadSpec missing-thread");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
    }

    @Test
    void shouldRejectWhenToNodeNotInNewThread() {
        WfSpecModel oldSpec = buildWfSpec("my-wf", 0, 0, "old-node");
        WfSpecModel newSpec = buildWfSpec("my-wf", 1, 0, "new-node");
        stubSpecs(newSpec, oldSpec);

        // "bad-node" does not exist in the entrypoint thread of newSpec
        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("entrypoint", migration("entrypoint", "old-node", "bad-node")));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown).isInstanceOf(LHApiException.class).hasMessageContaining("does not have node bad-node");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
    }

    @Test
    void shouldRejectEntrypointMigratingToChildThread() {
        WfSpecModel oldSpec = buildWfSpec("my-wf", 0, 0, "old-node"); // entrypoint = "entrypoint"

        // newSpec has both "entrypoint" and "child" threads; entrypoint of new is "entrypoint"
        WfSpecModel newSpec = new WfSpecModel();
        newSpec.setId(new WfSpecIdModel("my-wf", 1, 0));
        newSpec.setEntrypointThreadName("entrypoint");
        ThreadSpecModel newEntrypoint = buildThread("entrypoint", newSpec, Map.of("new-node", TestUtil.node()));
        ThreadSpecModel childThread = buildThread("child", newSpec, Map.of("new-node", TestUtil.node()));
        newSpec.setThreadSpecs(Map.of("entrypoint", newEntrypoint, "child", childThread));
        stubSpecs(newSpec, oldSpec);

        // The old entrypoint tries to migrate to "child", which is NOT the entrypoint of newSpec
        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("entrypoint", migration("child", "old-node", "new-node")));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("Entrypoint thread 'entrypoint' cannot migrate to non-entrypoint thread 'child'");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
    }

    @Test
    void shouldRejectChildMigratingToEntrypointThread() {
        // oldSpec has "entrypoint" and a child "old-child"
        WfSpecModel oldSpec = new WfSpecModel();
        oldSpec.setId(new WfSpecIdModel("my-wf", 0, 0));
        oldSpec.setEntrypointThreadName("entrypoint");
        ThreadSpecModel oldEntrypoint = buildThread("entrypoint", oldSpec, Map.of("old-node", TestUtil.node()));
        ThreadSpecModel oldChild = buildThread("old-child", oldSpec, Map.of("old-node", TestUtil.node()));
        oldSpec.setThreadSpecs(Map.of("entrypoint", oldEntrypoint, "old-child", oldChild));

        WfSpecModel newSpec = buildWfSpec("my-wf", 1, 0, "new-node"); // entrypoint = "entrypoint"
        stubSpecs(newSpec, oldSpec);

        // "old-child" is a child thread trying to migrate to "entrypoint", which IS the entrypoint of newSpec
        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("old-child", migration("entrypoint", "old-node", "new-node")));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("Child thread 'old-child' cannot migrate to entrypoint thread 'entrypoint'");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
    }

    @Test
    void shouldRejectWhenUsedVarNotDefinedInNewWfSpec() {
        // The destination thread uses "my-var", but it is not defined by any thread in the
        // new WfSpec and is not in the source thread's scope either.
        WfSpecModel oldSpec = buildWfSpec("my-wf", 0, 0, "old-node");
        WfSpecModel newSpec = buildWfSpec("my-wf", 1, 0, "new-node");
        newSpec.getThreadSpecs().get("entrypoint").setNodes(Map.of("new-node", nodeUsingVar("my-var")));
        stubSpecs(newSpec, oldSpec);

        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("entrypoint", migration("entrypoint", "old-node", "new-node")));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("my-var")
                .hasMessageContaining("is not defined by any thread");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.NOT_FOUND);
    }

    @Test
    void shouldSucceedWhenUsedVarAlreadyInOldScope() {
        // The destination thread uses "my-var", which already exists in the source thread's
        // scope, so no dependency is required.
        WfSpecModel oldSpec = buildWfSpec("my-wf", 0, 0, "old-node");
        oldSpec.getThreadSpecs()
                .get("entrypoint")
                .setVariableDefs(List.of(
                        TestUtil.threadVarDef("my-var", VariableType.STR, WfRunVariableAccessLevel.PUBLIC_VAR)));

        WfSpecModel newSpec = buildWfSpec("my-wf", 1, 0, "new-node");
        newSpec.getThreadSpecs().get("entrypoint").setNodes(Map.of("new-node", nodeUsingVar("my-var")));
        newSpec.getThreadSpecs()
                .get("entrypoint")
                .setVariableDefs(List.of(
                        TestUtil.threadVarDef("my-var", VariableType.STR, WfRunVariableAccessLevel.PUBLIC_VAR)));
        stubSpecs(newSpec, oldSpec);

        ThreadMigrationPlanModel m = migration("entrypoint", "old-node", "new-node");
        PutWorkflowMigrationPlanRequestModel request =
                buildRequest("plan", new WfSpecIdModel("my-wf", 0, 0), 1, 0, Map.of("entrypoint", m));

        WorkflowMigrationPlan result = (WorkflowMigrationPlan) request.process(mockContext);
        assertThat(result.getThreadMigrationsMap().get("entrypoint").getThreadSpecDependenciesList())
                .isEmpty();
    }

    @Test
    void shouldRejectWhenVarOwnerThreadNotInMigrationPlan() {
        // Scenario: "my-var" is owned by "entrypoint" in newSpec, which spawns "child-thread".
        // The destination "child-thread" uses "my-var", but it is not in the source child's
        // scope and the owner ("entrypoint") is NOT included in the migration plan.
        WfSpecModel oldSpec = new WfSpecModel();
        oldSpec.setId(new WfSpecIdModel("my-wf", 0, 0));
        oldSpec.setEntrypointThreadName("entrypoint");
        ThreadSpecModel oldEntrypoint = buildThread("entrypoint", oldSpec, Map.of("old-node", TestUtil.node()));
        ThreadSpecModel oldChild = buildThread("old-child", oldSpec, Map.of("old-child-node", TestUtil.node()));
        oldSpec.setThreadSpecs(Map.of("entrypoint", oldEntrypoint, "old-child", oldChild));

        // newSpec "entrypoint" owns "my-var" and has a START_THREAD node spawning "child-thread".
        // "child-thread" uses "my-var".
        WfSpecModel newSpec = new WfSpecModel();
        newSpec.setId(new WfSpecIdModel("my-wf", 1, 0));
        newSpec.setEntrypointThreadName("entrypoint");

        NodeModel startNode = new NodeModel();
        startNode.setType(NodeCase.START_THREAD);
        startNode.startThreadNode = new StartThreadNodeModel();
        startNode.startThreadNode.threadSpecName = "child-thread";

        ThreadSpecModel newEntrypoint = buildThread("entrypoint", newSpec, Map.of("start-node", startNode));
        newEntrypoint.setVariableDefs(
                List.of(TestUtil.threadVarDef("my-var", VariableType.STR, WfRunVariableAccessLevel.PUBLIC_VAR)));
        ThreadSpecModel newChildThread =
                buildThread("child-thread", newSpec, Map.of("new-child-node", nodeUsingVar("my-var")));
        newSpec.setThreadSpecs(Map.of("entrypoint", newEntrypoint, "child-thread", newChildThread));

        stubSpecs(newSpec, oldSpec);

        // Migration only maps "old-child" → "child-thread"; the owner "entrypoint" is NOT in the plan
        ThreadMigrationPlanModel childMigration = migration("child-thread", "old-child-node", "new-child-node");

        PutWorkflowMigrationPlanRequestModel request =
                buildRequest("plan", new WfSpecIdModel("my-wf", 0, 0), 1, 0, Map.of("old-child", childMigration));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("my-var")
                .hasMessageContaining("is not included in this migration plan");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.FAILED_PRECONDITION);
    }

    @Test
    void shouldAutomaticallyAddDependencyWhenVarOwnerIsInPlan() {
        // Same scenario as above, but this time the owner ("entrypoint") IS included in the migration plan.
        // The expected outcome: process() succeeds and auto-adds "entrypoint" to childMigration's dependencies.
        WfSpecModel oldSpec = new WfSpecModel();
        oldSpec.setId(new WfSpecIdModel("my-wf", 0, 0));
        oldSpec.setEntrypointThreadName("entrypoint");
        ThreadSpecModel oldEntrypoint = buildThread("entrypoint", oldSpec, Map.of("old-node", TestUtil.node()));
        ThreadSpecModel oldChild = buildThread("old-child", oldSpec, Map.of("old-child-node", TestUtil.node()));
        oldSpec.setThreadSpecs(Map.of("entrypoint", oldEntrypoint, "old-child", oldChild));

        WfSpecModel newSpec = new WfSpecModel();
        newSpec.setId(new WfSpecIdModel("my-wf", 1, 0));
        newSpec.setEntrypointThreadName("entrypoint");

        NodeModel startNode = new NodeModel();
        startNode.setType(NodeCase.START_THREAD);
        startNode.startThreadNode = new StartThreadNodeModel();
        startNode.startThreadNode.threadSpecName = "child-thread";

        ThreadSpecModel newEntrypoint = buildThread("entrypoint", newSpec, Map.of("start-node", startNode));
        newEntrypoint.setVariableDefs(
                List.of(TestUtil.threadVarDef("my-var", VariableType.STR, WfRunVariableAccessLevel.PUBLIC_VAR)));
        ThreadSpecModel newChildThread =
                buildThread("child-thread", newSpec, Map.of("new-child-node", nodeUsingVar("my-var")));
        newSpec.setThreadSpecs(Map.of("entrypoint", newEntrypoint, "child-thread", newChildThread));

        stubSpecs(newSpec, oldSpec);

        ThreadMigrationPlanModel entrypointMigration = migration("entrypoint", "old-node", "start-node");
        ThreadMigrationPlanModel childMigration = migration("child-thread", "old-child-node", "new-child-node");

        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("entrypoint", entrypointMigration, "old-child", childMigration));

        // Should succeed — the owner thread IS in the plan, and "entrypoint" should have been
        // auto-added to the child migration's dependencies in the resulting plan.
        WorkflowMigrationPlan result = (WorkflowMigrationPlan) request.process(mockContext);
        assertThat(result.getThreadMigrationsMap().get("old-child").getThreadSpecDependenciesList())
                .contains("entrypoint");
    }
}
