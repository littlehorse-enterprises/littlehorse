package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeds a deterministic set of WfSpecs and WfRuns that the Dashboard E2E suite asserts
 * against. Everything here is fixed and idempotent: fixed WfSpec names, fixed wfRun ids,
 * fixed input values. Playwright navigates straight to these ids, so tests never depend
 * on search ordering or timing.
 *
 * After seeding it keeps the task worker (and the parked user task) alive and prints
 * `FIXTURES_READY` so the orchestration can wait for that line before running Playwright.
 */
public class DashboardE2EFixtures {

    private static final Logger log = LoggerFactory.getLogger(DashboardE2EFixtures.class);

    private static final String TASK = "dashe2e-greet";
    private static final String USER_TASK_FORM = "dashe2e-approval";

    // ---- WfSpecs -------------------------------------------------------------

    static Workflow basic() {
        return new WorkflowImpl("dashe2e-basic", wf -> {
            WfRunVariable name = wf.declareStr("name").searchable();
            wf.execute(TASK, name);
        });
    }

    /** Branching + a variable mutation (exercises condition labels, the else edge, and the mutation icon). */
    static Workflow conditionals() {
        return new WorkflowImpl("dashe2e-conditionals", wf -> {
            WfRunVariable count = wf.declareInt("count").searchable();
            wf.declareDouble("ratio");
            wf.declareBool("enabled");
            WfRunVariable label = wf.declareStr("label");

            wf.execute(TASK, label);
            wf.doIf(count.isGreaterThan(10), ifBody -> {
                        label.assign("processed"); // variable mutation -> mutation icon on the edge
                        ifBody.execute(TASK, label);
                    })
                    .doElse(elseBody -> elseBody.execute(TASK, label));
        });
    }

    /** Parks in RUNNING at a USER_TASK node. */
    static Workflow userTask() {
        return new WorkflowImpl("dashe2e-usertask", wf -> {
            WfRunVariable assignee = wf.declareStr("assignee").searchable();
            wf.assignUserTask(USER_TASK_FORM, assignee, null);
            wf.execute(TASK, assignee);
        });
    }

    // ---- Seeding -------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        File configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config")
                .toFile();
        if (configPath.exists()) props.load(new FileInputStream(configPath));
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        LHTaskWorker worker = new LHTaskWorker(new FixtureTasks(), TASK, config);
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        worker.registerTaskDef();

        client.putUserTaskDef(new UserTaskSchema(new ApprovalForm(), USER_TASK_FORM).compile());

        basic().registerWfSpec(client);
        conditionals().registerWfSpec(client);
        userTask().registerWfSpec(client);

        // Worker runs in the background so we can submit runs and poll for completion.
        Thread workerThread = new Thread(worker::start, "fixture-worker");
        workerThread.setDaemon(true);
        workerThread.start();

        run(client, "dashe2e-basic", "dashe2e-basic-completed", Map.of("name", "Ada Lovelace"));
        run(
                client,
                "dashe2e-conditionals",
                "dashe2e-cond-hi",
                Map.of("count", 42, "ratio", 3.14, "enabled", true, "label", "hello"));
        run(
                client,
                "dashe2e-conditionals",
                "dashe2e-cond-lo",
                Map.of("count", 3, "ratio", 0.5, "enabled", false, "label", "world"));
        run(client, "dashe2e-usertask", "dashe2e-usertask-running", Map.of("assignee", "ada"));

        // Wait for the runs that should reach a terminal state (the user-task run stays RUNNING).
        awaitStatus(client, "dashe2e-basic-completed", LHStatus.COMPLETED);
        awaitStatus(client, "dashe2e-cond-hi", LHStatus.COMPLETED);
        awaitStatus(client, "dashe2e-cond-lo", LHStatus.COMPLETED);
        awaitStatus(client, "dashe2e-usertask-running", LHStatus.RUNNING);

        System.out.println("FIXTURES_READY");
        log.info("Dashboard E2E fixtures seeded; keeping worker + parked user task alive.");
        workerThread.join(); // keep the process (and workers) alive for interactive tests
    }

    private static void run(LittleHorseBlockingStub client, String wfSpec, String id, Map<String, Object> vars) {
        RunWfRequest.Builder req =
                RunWfRequest.newBuilder().setWfSpecName(wfSpec).setId(id);
        vars.forEach((k, v) -> req.putVariables(k, LHLibUtil.objToVarVal(v)));
        try {
            client.runWf(req.build());
            log.info("Seeded wfRun {} ({})", id, wfSpec);
        } catch (Exception e) {
            // Idempotent: a re-run against an existing server already has this wfRun.
            log.info("wfRun {} already present ({})", id, e.getMessage());
        }
    }

    private static void awaitStatus(LittleHorseBlockingStub client, String id, LHStatus expected)
            throws InterruptedException {
        WfRunId wfRunId = WfRunId.newBuilder().setId(id).build();
        for (int i = 0; i < 60; i++) {
            LHStatus status = client.getWfRun(wfRunId).getStatus();
            if (status == expected) return;
            if (status == LHStatus.ERROR || status == LHStatus.EXCEPTION) {
                throw new IllegalStateException("wfRun " + id + " reached " + status + ", expected " + expected);
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException("Timed out waiting for wfRun " + id + " to reach " + expected);
    }
}
