package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.ApplyWorkflowMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.MigrationVars;
import io.littlehorse.sdk.common.proto.NodeMigrationPlan;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.PutWorkflowMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.ThreadRun;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.proto.WorkflowMigrationPlan;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHUserTaskForm;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

/**
 * Test suite covers the following cases:
 *
 *
 * Migrating while wfRun is on the following nodes:
 * -- ee, sleep, usertask, waitForCondition
 *
 * Migrating only child thread to new child thread.
 *
 * Reject putWorkflowMigrationPlanRequest when variable in new threadSpec will not exist at runtime.
 *
 * Thread dependency is added to a threadMigrationPlan when new threadSpec uses variable defined/created from a parent thread.
 *
 * Migration variable provided with applyMigrationPlanRequest both pass/fail conditions.
 *
 * Multiple nodeMigrationPlans provided for one threadMigrationPlan
 *
 */
@LHTest(externalEventNames = {"migration-test-event"})
public class WorkflowMigrationTest {

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    private void awaitMigrationPlanVisible(WorkflowMigrationPlan plan) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .ignoreExceptionsInstanceOf(StatusRuntimeException.class)
                .until(() -> client.getWorkflowMigrationPlan(plan.getWorkflowMigrationPlanId()) != null);
    }

    @LHWorkflow("migration-test-wf")
    private Workflow migrationWf;

    @LHWorkflow("migration-test-wf")
    public Workflow getMigrationWf() {
        return Workflow.newWorkflow("migration-test-wf", wf -> {
            wf.waitForEvent("migration-test-event");
        });
    }

    @LHWorkflow("migration-from-sleep")
    private Workflow migrateFromSleepWf;

    @LHWorkflow("migration-from-sleep")
    public Workflow getSleepWf() {
        return Workflow.newWorkflow("migration-from-sleep", wf -> {
            wf.sleepSeconds(300);
        });
    }

    public static final String MIGRATION_USER_TASK_DEF_NAME = "migration-user-task";

    @LHUserTaskForm(MIGRATION_USER_TASK_DEF_NAME)
    private MigrationForm migrationForm = new MigrationForm();

    @LHWorkflow("migration-from-user-task")
    private Workflow migrateFromUserTaskWf;

    @LHWorkflow("migration-from-user-task")
    public Workflow getUserTaskWf() {
        return Workflow.newWorkflow("migration-from-user-task", wf -> {
            wf.assignUserTask(MIGRATION_USER_TASK_DEF_NAME, "test-user-id", null);
        });
    }

    @LHWorkflow("migration-from-wait-for-condition")
    private Workflow migrateFromWaitForConditionWf;

    @LHWorkflow("migration-from-wait-for-condition")
    public Workflow getWaitForConditionWf() {
        return Workflow.newWorkflow("migration-from-wait-for-condition", wf -> {
            // counter defaults to 1 and is never mutated, so the condition (counter == 0)
            // is never satisfied and the node parks in RUNNING until we migrate it.
            WfRunVariable counter = wf.declareInt("counter").withDefault(1);
            wf.waitForCondition(wf.condition(counter, Comparator.EQUALS, 0));
        });
    }

    @LHWorkflow("migrate-child-spec")
    private Workflow migrateChildSpec;

    @LHWorkflow("migrate-child-spec")
    public Workflow getChildSpecWf() {
        return Workflow.newWorkflow("migrate-child-spec", wf -> {
            wf.execute("migration-task");
            wf.spawnThread(
                    thread -> {
                        thread.waitForEvent("migration-test-event");
                    },
                    "child-thread",
                    null);
        });
    }

    @LHWorkflow("child-depends-on-parent")
    private Workflow migrateChildWParent;

    @LHWorkflow("child-depends-on-parent")
    public Workflow getChildDependsOnParentWf() {
        return Workflow.newWorkflow("child-depends-on-parent", wf -> {
            wf.spawnThread(
                    thread -> {
                        thread.waitForEvent("migration-test-event");
                    },
                    "child-thread",
                    null);
            wf.waitForEvent("migration-test-event");
        });
    }

    @LHWorkflow("migrate-from-second-migration-node")
    private Workflow migrateFromSecondMigrationNode;

    @LHWorkflow("migrate-from-second-migration-node")
    public Workflow getMigrateFromSecondMigrationNode() {
        return Workflow.newWorkflow("migrate-from-second-migration-node", wf -> {
            wf.execute("migration-task");
            wf.waitForEvent("migration-test-event");
        });
    }

    @LHWorkflow("migrate-with-variable")
    private Workflow migrateWithVariableWf;

    @LHWorkflow("migrate-with-variable")
    public Workflow getMigrateWithVariableWf() {
        return Workflow.newWorkflow("migrate-with-variable", wf -> {
            wf.waitForEvent("migration-test-event");
        });
    }

    // Task worker used by v1 WfSpec (registered manually inside the test)
    @LHTaskMethod("migration-task")
    public String migrationTask() {
        return "migrated";
    }

    @Test
    void shouldMigrateWfRunFromExternalEventToTask() {
        WfRunId runId = verifier.prepareRun(migrationWf)
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .start();

        // Read the actual old WfSpecId from the running WfRun so we never hardcode a version.
        WfSpecId oldSpecId = client.getWfRun(runId).getWfSpecId();

        WfSpec v1Spec = client.putWfSpec(Workflow.newWorkflow("migration-test-wf", wf -> {
                    wf.execute("migration-task");
                })
                .compileWorkflow());

        // Register the migration plan: entrypoint thread, ext-event node → task node.
        WorkflowMigrationPlan plan = client.putWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest.newBuilder()
                .setName(LHUtil.generateGuid())
                .setOldWfSpec(oldSpecId)
                .setMajorVersion(v1Spec.getId().getMajorVersion())
                .setRevision(v1Spec.getId().getRevision())
                .putThreadMigrations(
                        "entrypoint",
                        ThreadMigrationPlanRequest.newBuilder()
                                .setNewThreadName("entrypoint")
                                .putNodeMigrations(
                                        "1-migration-test-event-EXTERNAL_EVENT",
                                        NodeMigrationPlan.newBuilder()
                                                .setNewNodeName("1-migration-task-TASK")
                                                .build())
                                .build())
                .build());

        awaitMigrationPlanVisible(plan);

        int taskNodePosition = client.getWfRun(runId).getThreadRuns(0).getCurrentNodePosition() + 1;

        // Apply the plan to the running WfRun.
        WfRun migratedRun = client.applyWorkflowMigrationPlan(ApplyWorkflowMigrationPlanRequest.newBuilder()
                .setId(plan.getWorkflowMigrationPlanId())
                .setWfRunId(runId)
                .build());

        // The WfRun's top-level wfSpecId should reflect the new version.
        assertThat(migratedRun.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(migratedRun.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        // ThreadRun 0's own wfSpecId should also be updated to the new version.
        ThreadRun entrypointThread = migratedRun.getThreadRuns(0);
        assertThat(entrypointThread.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(entrypointThread.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        NodeRun taskNodeRun = client.getNodeRun(NodeRunId.newBuilder()
                .setWfRunId(runId)
                .setThreadRunNumber(0)
                .setPosition(taskNodePosition)
                .build());
        assertThat(taskNodeRun.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(taskNodeRun.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        // Ensure external_event nodeRun halted and remains on previous spec
        NodeRun eeNodeRun = client.getNodeRun(NodeRunId.newBuilder()
                .setThreadRunNumber(0)
                .setWfRunId(runId)
                .setPosition(taskNodePosition - 1)
                .build());
        assertThat(eeNodeRun.getStatus()).isEqualTo(LHStatus.HALTED);
        assertThat(eeNodeRun.getWfSpecId()).isEqualTo(oldSpecId);

        // The task worker picks up the task and the WfRun completes under v1.
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .until(() -> client.getWfRun(runId).getStatus() == LHStatus.COMPLETED);
    }

    @Test
    void shouldMigrateWfRunFromSleepToTask() {
        // Start a WfRun on v0 first so it is registered as the first (major=0, revision=0)
        // version. We must do this before registering v1, otherwise v1 would claim major=0
        // and the sleep spec would become a new major version instead. The 300s sleep keeps
        // the node parked long enough to migrate before it matures.
        WfRunId runId = verifier.prepareRun(migrateFromSleepWf)
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .start();

        // Read the actual old WfSpecId from the running WfRun so we never hardcode a version.
        WfSpecId oldSpecId = client.getWfRun(runId).getWfSpecId();

        WfSpec v1Spec = client.putWfSpec(Workflow.newWorkflow("migration-from-sleep", wf -> {
                    wf.execute("migration-task");
                })
                .compileWorkflow());

        // Register the migration plan: entrypoint thread, sleep node → task node.
        WorkflowMigrationPlan plan = client.putWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest.newBuilder()
                .setName(LHUtil.generateGuid())
                .setOldWfSpec(oldSpecId)
                .setMajorVersion(v1Spec.getId().getMajorVersion())
                .setRevision(v1Spec.getId().getRevision())
                .putThreadMigrations(
                        "entrypoint",
                        ThreadMigrationPlanRequest.newBuilder()
                                .setNewThreadName("entrypoint")
                                .putNodeMigrations(
                                        "1-sleep-SLEEP",
                                        NodeMigrationPlan.newBuilder()
                                                .setNewNodeName("1-migration-task-TASK")
                                                .build())
                                .build())
                .build());

        awaitMigrationPlanVisible(plan);

        int taskNodePosition = client.getWfRun(runId).getThreadRuns(0).getCurrentNodePosition() + 1;

        // Apply the plan to the running WfRun.
        WfRun migratedRun = client.applyWorkflowMigrationPlan(ApplyWorkflowMigrationPlanRequest.newBuilder()
                .setId(plan.getWorkflowMigrationPlanId())
                .setWfRunId(runId)
                .build());

        // The WfRun's top-level wfSpecId should reflect the new version.
        assertThat(migratedRun.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(migratedRun.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        // ThreadRun 0's own wfSpecId should also be updated to the new version.
        ThreadRun entrypointThread = migratedRun.getThreadRuns(0);
        assertThat(entrypointThread.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(entrypointThread.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        NodeRun taskNodeRun = client.getNodeRun(NodeRunId.newBuilder()
                .setWfRunId(runId)
                .setThreadRunNumber(0)
                .setPosition(taskNodePosition)
                .build());
        assertThat(taskNodeRun.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(taskNodeRun.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        // Ensure sleep nodeRun halted and remains on previous spec
        NodeRun sleepNodeRun = client.getNodeRun(NodeRunId.newBuilder()
                .setThreadRunNumber(0)
                .setWfRunId(runId)
                .setPosition(taskNodePosition - 1)
                .build());
        assertThat(sleepNodeRun.getStatus()).isEqualTo(LHStatus.HALTED);
        assertThat(sleepNodeRun.getWfSpecId()).isEqualTo(oldSpecId);

        // The task worker picks up the task and the WfRun completes under v1.
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .until(() -> client.getWfRun(runId).getStatus() == LHStatus.COMPLETED);
    }

    @Test
    void shouldMigrateWfRunFromUserTaskToTask() {
        WfRunId runId = verifier.prepareRun(migrateFromUserTaskWf)
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .start();

        // Read the actual old WfSpecId from the running WfRun so we never hardcode a version.
        WfSpecId oldSpecId = client.getWfRun(runId).getWfSpecId();

        WfSpec v1Spec = client.putWfSpec(Workflow.newWorkflow("migration-from-user-task", wf -> {
                    wf.execute("migration-task");
                })
                .compileWorkflow());

        // Register the migration plan: entrypoint thread, user-task node → task node.
        WorkflowMigrationPlan plan = client.putWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest.newBuilder()
                .setName(LHUtil.generateGuid())
                .setOldWfSpec(oldSpecId)
                .setMajorVersion(v1Spec.getId().getMajorVersion())
                .setRevision(v1Spec.getId().getRevision())
                .putThreadMigrations(
                        "entrypoint",
                        ThreadMigrationPlanRequest.newBuilder()
                                .setNewThreadName("entrypoint")
                                .putNodeMigrations(
                                        "1-migration-user-task-USER_TASK",
                                        NodeMigrationPlan.newBuilder()
                                                .setNewNodeName("1-migration-task-TASK")
                                                .build())
                                .build())
                .build());

        awaitMigrationPlanVisible(plan);

        int taskNodePosition = client.getWfRun(runId).getThreadRuns(0).getCurrentNodePosition() + 1;

        WfRun migratedRun = client.applyWorkflowMigrationPlan(ApplyWorkflowMigrationPlanRequest.newBuilder()
                .setId(plan.getWorkflowMigrationPlanId())
                .setWfRunId(runId)
                .build());

        assertThat(migratedRun.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(migratedRun.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        ThreadRun entrypointThread = migratedRun.getThreadRuns(0);
        assertThat(entrypointThread.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(entrypointThread.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        NodeRun taskNodeRun = client.getNodeRun(NodeRunId.newBuilder()
                .setWfRunId(runId)
                .setThreadRunNumber(0)
                .setPosition(taskNodePosition)
                .build());
        assertThat(taskNodeRun.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(taskNodeRun.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        // Ensure user_task nodeRun halted and remains on previous spec
        NodeRun userTaskNodeRun = client.getNodeRun(NodeRunId.newBuilder()
                .setThreadRunNumber(0)
                .setWfRunId(runId)
                .setPosition(taskNodePosition - 1)
                .build());
        assertThat(userTaskNodeRun.getStatus()).isEqualTo(LHStatus.HALTED);
        assertThat(userTaskNodeRun.getWfSpecId()).isEqualTo(oldSpecId);

        // The task worker picks up the task and the WfRun completes under v1.
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .until(() -> client.getWfRun(runId).getStatus() == LHStatus.COMPLETED);
    }

    @Test
    void shouldMigrateWfRunFromWaitForConditionToTask() {
        WfRunId runId = verifier.prepareRun(migrateFromWaitForConditionWf)
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .start();

        // Read the actual old WfSpecId from the running WfRun so we never hardcode a version.
        WfSpecId oldSpecId = client.getWfRun(runId).getWfSpecId();

        WfSpec v1Spec = client.putWfSpec(Workflow.newWorkflow("migration-from-wait-for-condition", wf -> {
                    wf.execute("migration-task");
                })
                .compileWorkflow());

        WorkflowMigrationPlan plan = client.putWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest.newBuilder()
                .setName(LHUtil.generateGuid())
                .setOldWfSpec(oldSpecId)
                .setMajorVersion(v1Spec.getId().getMajorVersion())
                .setRevision(v1Spec.getId().getRevision())
                .putThreadMigrations(
                        "entrypoint",
                        ThreadMigrationPlanRequest.newBuilder()
                                .setNewThreadName("entrypoint")
                                .putNodeMigrations(
                                        "1-wait-for-condition-WAIT_FOR_CONDITION",
                                        NodeMigrationPlan.newBuilder()
                                                .setNewNodeName("1-migration-task-TASK")
                                                .build())
                                .build())
                .build());

        awaitMigrationPlanVisible(plan);

        int taskNodePosition = client.getWfRun(runId).getThreadRuns(0).getCurrentNodePosition() + 1;

        // Apply the plan to the running WfRun.
        WfRun migratedRun = client.applyWorkflowMigrationPlan(ApplyWorkflowMigrationPlanRequest.newBuilder()
                .setId(plan.getWorkflowMigrationPlanId())
                .setWfRunId(runId)
                .build());

        // The WfRun's top-level wfSpecId should reflect the new version.
        assertThat(migratedRun.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(migratedRun.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        // ThreadRun 0's own wfSpecId should also be updated to the new version.
        ThreadRun entrypointThread = migratedRun.getThreadRuns(0);
        assertThat(entrypointThread.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(entrypointThread.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        NodeRun taskNodeRun = client.getNodeRun(NodeRunId.newBuilder()
                .setWfRunId(runId)
                .setThreadRunNumber(0)
                .setPosition(taskNodePosition)
                .build());
        assertThat(taskNodeRun.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(taskNodeRun.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        // Ensure wait-for-condition nodeRun halted and remains on previous spec
        NodeRun waitForConditionNodeRun = client.getNodeRun(NodeRunId.newBuilder()
                .setThreadRunNumber(0)
                .setWfRunId(runId)
                .setPosition(taskNodePosition - 1)
                .build());
        assertThat(waitForConditionNodeRun.getStatus()).isEqualTo(LHStatus.HALTED);
        assertThat(waitForConditionNodeRun.getWfSpecId()).isEqualTo(oldSpecId);

        // The task worker picks up the task and the WfRun completes under v1.
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .until(() -> client.getWfRun(runId).getStatus() == LHStatus.COMPLETED);
    }

    @Test
    void shouldMigrateChildThreadRunToNewSpec() {

        WfRunId runId = verifier.prepareRun(migrateChildSpec)
                .waitForNodeRunStatus(0, 1, LHStatus.COMPLETED, Duration.ofSeconds(30))
                .waitForNodeRunStatus(1, 1, LHStatus.RUNNING, Duration.ofSeconds(30))
                .start();

        // Read the actual old WfSpecId from the running WfRun so we never hardcode a version.
        WfSpecId oldSpecId = client.getWfRun(runId).getWfSpecId();

        WfSpec v1Spec = client.putWfSpec(Workflow.newWorkflow("migrate-child-spec", wf -> {
                    wf.execute("migration-task");
                    wf.spawnThread(
                            thread -> {
                                thread.execute("migration-task");
                            },
                            "child-thread",
                            null);
                })
                .compileWorkflow());

        // Register the migration plan: entrypoint thread, wait-for-condition node → task node.
        WorkflowMigrationPlan plan = client.putWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest.newBuilder()
                .setName(LHUtil.generateGuid())
                .setOldWfSpec(oldSpecId)
                .setMajorVersion(v1Spec.getId().getMajorVersion())
                .setRevision(v1Spec.getId().getRevision())
                .putThreadMigrations(
                        "child-thread",
                        ThreadMigrationPlanRequest.newBuilder()
                                .setNewThreadName("child-thread")
                                .putNodeMigrations(
                                        "1-migration-test-event-EXTERNAL_EVENT",
                                        NodeMigrationPlan.newBuilder()
                                                .setNewNodeName("1-migration-task-TASK")
                                                .build())
                                .build())
                .build());

        awaitMigrationPlanVisible(plan);

        int taskNodePosition = client.getWfRun(runId).getThreadRuns(1).getCurrentNodePosition() + 1;

        // Apply the plan to the running WfRun.
        WfRun migratedRun = client.applyWorkflowMigrationPlan(ApplyWorkflowMigrationPlanRequest.newBuilder()
                .setId(plan.getWorkflowMigrationPlanId())
                .setWfRunId(runId)
                .build());

        // The WfRun's top-level wfSpecId should remain the same
        assertThat(migratedRun.getWfSpecId().getMajorVersion()).isEqualTo(oldSpecId.getMajorVersion());
        assertThat(migratedRun.getWfSpecId().getRevision()).isEqualTo(oldSpecId.getRevision());

        // ThreadRun 1 own wfSpecId should be updated to the new version.
        ThreadRun entrypointThread = migratedRun.getThreadRuns(1);
        assertThat(entrypointThread.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(entrypointThread.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        NodeRun taskNodeRun = client.getNodeRun(NodeRunId.newBuilder()
                .setWfRunId(runId)
                .setThreadRunNumber(1)
                .setPosition(taskNodePosition)
                .build());
        assertThat(taskNodeRun.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(taskNodeRun.getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        // The task worker picks up the task and the WfRun completes under v1.
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .until(() -> client.getWfRun(runId).getStatus() == LHStatus.COMPLETED);
    }

    @Test
    void shouldRejectMigrationPlanSinceChildThreadVariableNotAvailableAtRunTime() {

        WfRunId runId = verifier.prepareRun(migrateChildSpec)
                .waitForNodeRunStatus(0, 1, LHStatus.COMPLETED, Duration.ofSeconds(30))
                .waitForNodeRunStatus(1, 1, LHStatus.RUNNING, Duration.ofSeconds(30))
                .start();

        // Read the actual old WfSpecId from the running WfRun so we never hardcode a version.
        WfSpecId oldSpecId = client.getWfRun(runId).getWfSpecId();

        WfSpec v1Spec = client.putWfSpec(Workflow.newWorkflow("migrate-child-spec", wf -> {
                    WfRunVariable num = wf.declareInt("num").withDefault(1);
                    wf.execute("migration-task");
                    wf.spawnThread(
                            thread -> {
                                thread.execute("migration-task");
                                num.assign(32);
                            },
                            "child-thread",
                            null);
                })
                .compileWorkflow());

        assertThatThrownBy(() -> client.putWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest.newBuilder()
                        .setName(LHUtil.generateGuid())
                        .setOldWfSpec(oldSpecId)
                        .setMajorVersion(v1Spec.getId().getMajorVersion())
                        .setRevision(v1Spec.getId().getRevision())
                        .putThreadMigrations(
                                "child-thread",
                                ThreadMigrationPlanRequest.newBuilder()
                                        .setNewThreadName("child-thread")
                                        .putNodeMigrations(
                                                "1-migration-test-event-EXTERNAL_EVENT",
                                                NodeMigrationPlan.newBuilder()
                                                        .setNewNodeName("1-migration-task-TASK")
                                                        .build())
                                        .build())
                        .build()))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(ex -> assertThat(
                                ((StatusRuntimeException) ex).getStatus().getCode())
                        .isEqualTo(Status.Code.FAILED_PRECONDITION))
                .hasMessageContaining("The variable 'num' is required by destination thread 'child-thread' "
                        + "but the thread that defines it ('entrypoint') is not included in this migration plan");
    }

    @Test
    void shouldAddDependencyToThreadMigration() {

        WfRunId runId = verifier.prepareRun(migrateChildWParent)
                .waitForNodeRunStatus(1, 1, LHStatus.RUNNING, Duration.ofSeconds(30))
                .waitForNodeRunStatus(0, 2, LHStatus.RUNNING, Duration.ofSeconds(30))
                .start();

        // Read the actual old WfSpecId from the running WfRun so we never hardcode a version.
        WfSpecId oldSpecId = client.getWfRun(runId).getWfSpecId();

        WfSpec v1Spec = client.putWfSpec(Workflow.newWorkflow("child-depends-on-parent", wf -> {
                    WfRunVariable num = wf.declareInt("num").withDefault(1);
                    wf.spawnThread(
                            thread -> {
                                thread.execute("migration-task");
                                num.assign(32);
                            },
                            "child-thread",
                            null);
                    wf.execute("migration-task");
                })
                .compileWorkflow());

        WorkflowMigrationPlan plan = client.putWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest.newBuilder()
                .setName(LHUtil.generateGuid())
                .setOldWfSpec(oldSpecId)
                .setMajorVersion(v1Spec.getId().getMajorVersion())
                .setRevision(v1Spec.getId().getRevision())
                .putThreadMigrations(
                        "entrypoint",
                        ThreadMigrationPlanRequest.newBuilder()
                                .setNewThreadName("entrypoint")
                                .putNodeMigrations(
                                        "2-migration-test-event-EXTERNAL_EVENT",
                                        NodeMigrationPlan.newBuilder()
                                                .setNewNodeName("2-migration-task-TASK")
                                                .build())
                                .build())
                .putThreadMigrations(
                        "child-thread",
                        ThreadMigrationPlanRequest.newBuilder()
                                .setNewThreadName("child-thread")
                                .putNodeMigrations(
                                        "1-migration-test-event-EXTERNAL_EVENT",
                                        NodeMigrationPlan.newBuilder()
                                                .setNewNodeName("1-migration-task-TASK")
                                                .build())
                                .build())
                .build());

        awaitMigrationPlanVisible(plan);

        assertThat(plan.getThreadMigrationsMap().get("child-thread").getThreadSpecDependenciesList())
                .containsExactly("entrypoint");

        // The entrypoint thread has no dependencies (it owns its own variables).
        assertThat(plan.getThreadMigrationsMap().get("entrypoint").getThreadSpecDependenciesList())
                .isEmpty();

        // Capture both task node positions before migration so the fetches are race-free.
        int entrypointTaskPosition = client.getWfRun(runId).getThreadRuns(0).getCurrentNodePosition() + 1;
        int childTaskPosition = client.getWfRun(runId).getThreadRuns(1).getCurrentNodePosition() + 1;

        // Apply the plan to the running WfRun.
        client.applyWorkflowMigrationPlan(ApplyWorkflowMigrationPlanRequest.newBuilder()
                .setId(plan.getWorkflowMigrationPlanId())
                .setWfRunId(runId)
                .build());

        // The WfRun should run to completion under the new spec.
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .until(() -> client.getWfRun(runId).getStatus() == LHStatus.COMPLETED);

        // Both threads should now be on the new version.
        WfRun completedRun = client.getWfRun(runId);
        assertThat(completedRun.getThreadRuns(0).getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(completedRun.getThreadRuns(1).getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());

        NodeRun entrypointTaskNode = client.getNodeRun(NodeRunId.newBuilder()
                .setWfRunId(runId)
                .setThreadRunNumber(0)
                .setPosition(entrypointTaskPosition)
                .build());
        NodeRun childTaskNode = client.getNodeRun(NodeRunId.newBuilder()
                .setWfRunId(runId)
                .setThreadRunNumber(1)
                .setPosition(childTaskPosition)
                .build());
        long entrypointArrival =
                LHLibUtil.fromProtoTs(entrypointTaskNode.getArrivalTime()).getTime();
        long childArrival =
                LHLibUtil.fromProtoTs(childTaskNode.getArrivalTime()).getTime();
        assertThat(entrypointArrival).isLessThanOrEqualTo(childArrival);

        assertThat(client.getVariable(VariableId.newBuilder()
                                .setWfRunId(runId)
                                .setThreadRunNumber(0)
                                .setName("num")
                                .build())
                        .getValue()
                        .getInt())
                .isEqualTo(32);
    }

    @Test
    void shouldSelectCorrectNodeMigrationWhenMultipleNodesInThreadPlan() {
        WfRunId runId = verifier.prepareRun(migrateFromSecondMigrationNode)
                .waitForNodeRunStatus(0, 2, LHStatus.RUNNING, Duration.ofSeconds(30))
                .start();

        WfSpecId oldSpecId = client.getWfRun(runId).getWfSpecId();

        WfSpec v1Spec = client.putWfSpec(Workflow.newWorkflow("migrate-from-second-migration-node", wf -> {
                    wf.waitForEvent("migration-test-event");
                    wf.waitForEvent("migration-test-event");
                })
                .compileWorkflow());

        WorkflowMigrationPlan plan = client.putWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest.newBuilder()
                .setName(LHUtil.generateGuid())
                .setOldWfSpec(oldSpecId)
                .setMajorVersion(v1Spec.getId().getMajorVersion())
                .setRevision(v1Spec.getId().getRevision())
                .putThreadMigrations(
                        "entrypoint",
                        ThreadMigrationPlanRequest.newBuilder()
                                .setNewThreadName("entrypoint")
                                .putNodeMigrations(
                                        "1-migration-task-TASK",
                                        NodeMigrationPlan.newBuilder()
                                                .setNewNodeName("1-migration-test-event-EXTERNAL_EVENT")
                                                .build())
                                .putNodeMigrations(
                                        "2-migration-test-event-EXTERNAL_EVENT",
                                        NodeMigrationPlan.newBuilder()
                                                .setNewNodeName("2-migration-test-event-EXTERNAL_EVENT")
                                                .build())
                                .build())
                .build());

        awaitMigrationPlanVisible(plan);

        // Migration creates a new NodeRun at (currentNodePosition + 1). Capture it pre-apply.
        int migratedNodePosition = client.getWfRun(runId).getThreadRuns(0).getCurrentNodePosition() + 1;

        WfRun migratedRun = client.applyWorkflowMigrationPlan(ApplyWorkflowMigrationPlanRequest.newBuilder()
                .setId(plan.getWorkflowMigrationPlanId())
                .setWfRunId(runId)
                .build());

        // The entrypoint thread moved to the new version.
        assertThat(migratedRun.getThreadRuns(0).getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());
        assertThat(migratedRun.getThreadRuns(0).getWfSpecId().getRevision())
                .isEqualTo(v1Spec.getId().getRevision());

        // KEY ASSERTION: the migrated node is the SECOND v1 node, proving the server looked up
        // the node-migration entry by the parked node's name (not the first entry in the map).
        NodeRun migratedNodeRun = client.getNodeRun(NodeRunId.newBuilder()
                .setWfRunId(runId)
                .setThreadRunNumber(0)
                .setPosition(migratedNodePosition)
                .build());
        assertThat(migratedNodeRun.getNodeName()).isEqualTo("2-migration-test-event-EXTERNAL_EVENT");
        assertThat(migratedNodeRun.getWfSpecId().getMajorVersion())
                .isEqualTo(v1Spec.getId().getMajorVersion());

        // The parked old node halted and stayed on the old spec.
        NodeRun oldNodeRun = client.getNodeRun(NodeRunId.newBuilder()
                .setWfRunId(runId)
                .setThreadRunNumber(0)
                .setPosition(migratedNodePosition - 1)
                .build());
        assertThat(oldNodeRun.getStatus()).isEqualTo(LHStatus.HALTED);
        assertThat(oldNodeRun.getWfSpecId()).isEqualTo(oldSpecId);

        client.putExternalEvent(PutExternalEventRequest.newBuilder()
                .setWfRunId(runId)
                .setContent(LHLibUtil.objToVarVal("ignored"))
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName("migration-test-event"))
                .build());

        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .until(() -> client.getWfRun(runId).getStatus() == LHStatus.COMPLETED);
    }

    @Test
    void shouldApplyMigrationVariableWhenMigratingFromExternalEventToTask() {

        WfRunId runId = verifier.prepareRun(migrateWithVariableWf)
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .start();

        // Read the actual old WfSpecId from the running WfRun so we never hardcode a version.
        WfSpecId oldSpecId = client.getWfRun(runId).getWfSpecId();

        WfSpec v1Spec = client.putWfSpec(Workflow.newWorkflow("migrate-with-variable", wf -> {
                    wf.declareInt("num").withDefault(1);
                    wf.execute("migration-task");
                })
                .compileWorkflow());

        WorkflowMigrationPlan plan = client.putWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest.newBuilder()
                .setName(LHUtil.generateGuid())
                .setOldWfSpec(oldSpecId)
                .setMajorVersion(v1Spec.getId().getMajorVersion())
                .setRevision(v1Spec.getId().getRevision())
                .putThreadMigrations(
                        "entrypoint",
                        ThreadMigrationPlanRequest.newBuilder()
                                .setNewThreadName("entrypoint")
                                .putNodeMigrations(
                                        "1-migration-test-event-EXTERNAL_EVENT",
                                        NodeMigrationPlan.newBuilder()
                                                .setNewNodeName("1-migration-task-TASK")
                                                .build())
                                .build())
                .build());

        awaitMigrationPlanVisible(plan);

        client.applyWorkflowMigrationPlan(ApplyWorkflowMigrationPlanRequest.newBuilder()
                .setId(plan.getWorkflowMigrationPlanId())
                .setWfRunId(runId)
                .putMigrationVarsByThread(
                        "entrypoint",
                        MigrationVars.newBuilder()
                                .putVarAssignmentByVarName(
                                        "num",
                                        VariableAssignment.newBuilder()
                                                .setLiteralValue(VariableValue.newBuilder()
                                                        .setInt(42)
                                                        .build())
                                                .build())
                                .build())
                .build());

        // The task worker picks up the task and the WfRun completes under v1.
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .until(() -> client.getWfRun(runId).getStatus() == LHStatus.COMPLETED);

        // The migration variable overrode the declared default (1) with the new value (42),
        // proving migration variables are applied to the migrated thread.
        assertThat(client.getVariable(VariableId.newBuilder()
                                .setWfRunId(runId)
                                .setThreadRunNumber(0)
                                .setName("num")
                                .build())
                        .getValue()
                        .getInt())
                .isEqualTo(42);
    }

    @Test
    void shouldRejectMigrationVariableWithTypeMismatch() {
        // Start v0 first so it claims (major=0, revision=0). The entrypoint parks on an
        // external event, keeping the thread halted long enough to migrate.
        WfRunId runId = verifier.prepareRun(migrateWithVariableWf)
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .start();

        // Read the actual old WfSpecId from the running WfRun so we never hardcode a version.
        WfSpecId oldSpecId = client.getWfRun(runId).getWfSpecId();

        // Register v1 WfSpec: declares an int variable 'num' (default 1) and runs a task.
        WfSpec v1Spec = client.putWfSpec(Workflow.newWorkflow("migrate-with-variable", wf -> {
                    wf.declareInt("num").withDefault(1);
                    wf.execute("migration-task");
                })
                .compileWorkflow());

        // Register the migration plan: entrypoint thread, external-event node → task node.
        WorkflowMigrationPlan plan = client.putWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest.newBuilder()
                .setName(LHUtil.generateGuid())
                .setOldWfSpec(oldSpecId)
                .setMajorVersion(v1Spec.getId().getMajorVersion())
                .setRevision(v1Spec.getId().getRevision())
                .putThreadMigrations(
                        "entrypoint",
                        ThreadMigrationPlanRequest.newBuilder()
                                .setNewThreadName("entrypoint")
                                .putNodeMigrations(
                                        "1-migration-test-event-EXTERNAL_EVENT",
                                        NodeMigrationPlan.newBuilder()
                                                .setNewNodeName("1-migration-task-TASK")
                                                .build())
                                .build())
                .build());

        awaitMigrationPlanVisible(plan);

        assertThatThrownBy(() -> client.applyWorkflowMigrationPlan(ApplyWorkflowMigrationPlanRequest.newBuilder()
                        .setId(plan.getWorkflowMigrationPlanId())
                        .setWfRunId(runId)
                        .putMigrationVarsByThread(
                                "entrypoint",
                                MigrationVars.newBuilder()
                                        .putVarAssignmentByVarName(
                                                "num",
                                                VariableAssignment.newBuilder()
                                                        .setLiteralValue(VariableValue.newBuilder()
                                                                .setStr("not-an-int")
                                                                .build())
                                                        .build())
                                        .build())
                        .build()))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(ex -> assertThat(
                                ((StatusRuntimeException) ex).getStatus().getCode())
                        .isEqualTo(Status.Code.INVALID_ARGUMENT));
    }
}

class MigrationForm {

    @UserTaskField(displayName = "Notes", description = "free-form notes")
    public String notes;

    public MigrationForm() {}
}
