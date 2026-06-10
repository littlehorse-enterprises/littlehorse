package io.littlehorse.common.model.metadatacommand.subcommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import io.grpc.Status.Code;
import io.littlehorse.TestUtil;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.migrations.NodeMigrationPlanModel;
import io.littlehorse.common.model.getable.global.migrations.ThreadMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.StartThreadNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.PutWorkflowMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;

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
        migrations.forEach((k, v) -> proto.putThreadMigrations(k, v.toProto().build()));
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
     * and empty variables and dependencies lists (safe to mutate).
     */
    private ThreadMigrationPlanModel migration(String newThread, String fromNode, String toNode) {
        ThreadMigrationPlanModel m = new ThreadMigrationPlanModel();
        m.setNewThreadName(newThread);
        NodeMigrationPlanModel nodeMigration = new NodeMigrationPlanModel();
        nodeMigration.setNewNodeName(toNode);
        Map<String, NodeMigrationPlanModel> nodeMigrations = new HashMap<>();
        nodeMigrations.put(fromNode, nodeMigration);
        m.setNodeMigrations(nodeMigrations);
        m.setRequiredVariables(new ArrayList<>());
        m.setDependencies(new ArrayList<>());
        return m;
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

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("does not have node bad-node");
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

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("does not have node bad-node");
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
    void shouldRejectRequiredVarNotInScopeOfEither() {
        // Neither old nor new spec defines "my-var"
        WfSpecModel oldSpec = buildWfSpec("my-wf", 0, 0, "old-node");
        WfSpecModel newSpec = buildWfSpec("my-wf", 1, 0, "new-node");
        stubSpecs(newSpec, oldSpec);

        ThreadMigrationPlanModel m = migration("entrypoint", "old-node", "new-node");
        m.setRequiredVariables(List.of("my-var"));

        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan", new WfSpecIdModel("my-wf", 0, 0), 1, 0, Map.of("entrypoint", m));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("my-var")
                .hasMessageContaining("is not in the scope");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.NOT_FOUND);
    }

    @Test
    void shouldRejectRequiredVarInOldButNotNew() {
        // "my-var" exists in old spec's entrypoint thread but not in new spec
        WfSpecModel oldSpec = buildWfSpec("my-wf", 0, 0, "old-node");
        oldSpec.getThreadSpecs()
                .get("entrypoint")
                .setVariableDefs(List.of(
                        TestUtil.threadVarDef("my-var", VariableType.STR, WfRunVariableAccessLevel.PUBLIC_VAR)));
        WfSpecModel newSpec = buildWfSpec("my-wf", 1, 0, "new-node");
        stubSpecs(newSpec, oldSpec);

        ThreadMigrationPlanModel m = migration("entrypoint", "old-node", "new-node");
        m.setRequiredVariables(List.of("my-var"));

        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan", new WfSpecIdModel("my-wf", 0, 0), 1, 0, Map.of("entrypoint", m));

        Throwable thrown = catchThrowable(() -> request.process(mockContext));

        assertThat(thrown)
                .isInstanceOf(LHApiException.class)
                .hasMessageContaining("my-var")
                .hasMessageContaining("is not accessible in destination thread");
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.FAILED_PRECONDITION);
    }

    @Test
    void shouldRejectWhenVarOwnerThreadNotInMigrationPlan() {
        // Scenario: "my-var" is owned by "entrypoint" in newSpec, which spawns "child-thread".
        // "child-thread" can access "my-var" (it's a descendant of the owner).
        // The migration targets "child-thread" but does NOT include the owner ("entrypoint") in the plan.
        WfSpecModel oldSpec = new WfSpecModel();
        oldSpec.setId(new WfSpecIdModel("my-wf", 0, 0));
        oldSpec.setEntrypointThreadName("entrypoint");
        ThreadSpecModel oldEntrypoint = buildThread("entrypoint", oldSpec, Map.of("old-node", TestUtil.node()));
        ThreadSpecModel oldChild = buildThread("old-child", oldSpec, Map.of("old-child-node", TestUtil.node()));
        oldSpec.setThreadSpecs(Map.of("entrypoint", oldEntrypoint, "old-child", oldChild));

        // newSpec "entrypoint" owns "my-var" and has a START_THREAD node spawning "child-thread"
        WfSpecModel newSpec = new WfSpecModel();
        newSpec.setId(new WfSpecIdModel("my-wf", 1, 0));
        newSpec.setEntrypointThreadName("entrypoint");

        NodeModel startNode = new NodeModel();
        startNode.setType(NodeCase.START_THREAD);
        startNode.startThreadNode = new StartThreadNodeModel();
        startNode.startThreadNode.threadSpecName = "child-thread";

        ThreadSpecModel newEntrypoint = buildThread("entrypoint", newSpec, Map.of("start-node", startNode));
        newEntrypoint.setVariableDefs(List.of(
                TestUtil.threadVarDef("my-var", VariableType.STR, WfRunVariableAccessLevel.PUBLIC_VAR)));
        ThreadSpecModel newChildThread =
                buildThread("child-thread", newSpec, Map.of("new-child-node", TestUtil.node()));
        newSpec.setThreadSpecs(Map.of("entrypoint", newEntrypoint, "child-thread", newChildThread));

        stubSpecs(newSpec, oldSpec);

        // Migration only maps "old-child" → "child-thread"; the owner "entrypoint" is NOT in the plan
        ThreadMigrationPlanModel childMigration = migration("child-thread", "old-child-node", "new-child-node");
        childMigration.setRequiredVariables(List.of("my-var"));

        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan", new WfSpecIdModel("my-wf", 0, 0), 1, 0, Map.of("old-child", childMigration));

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
        newEntrypoint.setVariableDefs(List.of(
                TestUtil.threadVarDef("my-var", VariableType.STR, WfRunVariableAccessLevel.PUBLIC_VAR)));
        ThreadSpecModel newChildThread =
                buildThread("child-thread", newSpec, Map.of("new-child-node", TestUtil.node()));
        newSpec.setThreadSpecs(Map.of("entrypoint", newEntrypoint, "child-thread", newChildThread));

        stubSpecs(newSpec, oldSpec);

        ThreadMigrationPlanModel entrypointMigration = migration("entrypoint", "old-node", "start-node");
        ThreadMigrationPlanModel childMigration = migration("child-thread", "old-child-node", "new-child-node");
        childMigration.setRequiredVariables(List.of("my-var"));

        PutWorkflowMigrationPlanRequestModel request = buildRequest(
                "plan",
                new WfSpecIdModel("my-wf", 0, 0),
                1,
                0,
                Map.of("entrypoint", entrypointMigration, "old-child", childMigration));

        // Should succeed — the owner thread IS in the plan
        assertThat(catchThrowable(() -> request.process(mockContext))).isNull();
        // "entrypoint" should have been auto-added to the child migration's dependencies.
        // Note: buildRequest() round-trips through proto, so we must read the internal copy.
        assertThat(request.getThreadMigrations().get("old-child").getDependencies()).contains("entrypoint");
    }
}
