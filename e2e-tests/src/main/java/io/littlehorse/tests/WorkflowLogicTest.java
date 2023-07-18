package io.littlehorse.tests;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.NodeRunPb;
import io.littlehorse.sdk.common.proto.NodeRunPb.NodeTypeCase;
import io.littlehorse.sdk.common.proto.PutExternalEventDefPb;
import io.littlehorse.sdk.common.proto.TaskRunPb;
import io.littlehorse.sdk.common.proto.VariableValuePb;
import io.littlehorse.sdk.common.proto.WfRunPb;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WorkflowLogicTest extends Test {

    private static final int WAIT_TIME_BETWEEN_REGISTER = 500;
    private static Logger log = LoggerFactory.getLogger(WorkflowLogicTest.class);

    public abstract List<String> launchAndCheckWorkflows(LHClient client)
        throws TestFailure, LHApiError, InterruptedException;

    protected abstract Workflow getWorkflowImpl();

    public abstract List<Object> getTaskWorkerObjects();

    private Workflow workflow;
    private List<LHTaskWorker> workers;
    private List<String> wfRunIds;
    private int wfSpecVersion; // usually will be 0

    public WorkflowLogicTest(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
        wfRunIds = new ArrayList<>();
    }

    public void test() throws Exception {
        deploy(client, workerConfig);
        launchAndCheckWorkflows(client);
    }

    public void cleanup() throws LHApiError {
        log.info("Shutting down task workers for testcase {}", getWorkflowName());
        for (LHTaskWorker worker : workers) {
            worker.close();
        }

        for (String wfRunId : wfRunIds) {
            client.deleteWfRun(wfRunId);
        }

        for (String tdn : workflow.getRequiredTaskDefNames()) {
            client.deleteTaskDef(tdn);
        }

        for (String eedn : workflow.getRequiredExternalEventDefNames()) {
            client.deleteExternalEventDef(eedn);
        }

        client.deleteWfSpec(getWorkflowName(), wfSpecVersion);
    }

    protected Workflow getWorkflow() {
        if (workflow == null) {
            workflow = getWorkflowImpl();
        }
        return workflow;
    }

    protected String getWorkflowName() {
        String raw = this.getClass().getSimpleName();

        StringBuilder result = new StringBuilder();
        result.append(raw.substring(0, 2).toLowerCase());
        for (int i = 2; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append("-").append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    protected void deploy(LHClient client, LHWorkerConfig workerConfig)
        throws TestFailure {
        workers = new ArrayList<>();

        // Now need to create LHTaskWorkers and run them for all worker objects.
        for (Object executable : getTaskWorkerObjects()) {
            for (LHTaskWorker worker : getWorkersFromExecutable(
                executable,
                workerConfig
            )) {
                workers.add(worker);
                try {
                    worker.registerTaskDef(true);
                    worker.start();
                } catch (LHApiError exn) {
                    exn.printStackTrace();
                    throw new TestFailure(
                        this,
                        "Failed to start worker, failing test."
                    );
                }
            }
        }

        Set<String> requiredExternalEventDefNames = getWorkflow()
            .getRequiredExternalEventDefNames();

        for (String externalEvent : requiredExternalEventDefNames) {
            try {
                client.putExternalEventDef(
                    PutExternalEventDefPb.newBuilder().setName(externalEvent).build(),
                    true
                );
            } catch (LHApiError exn) {
                throw new TestFailure(
                    this,
                    "Failed deploying external event def: " + exn.getMessage()
                );
            }
        }

        try {
            Thread.sleep(WAIT_TIME_BETWEEN_REGISTER);
        } catch (Exception ignored) {}

        // Now deploy the WF
        try {
            getWorkflow().registerWfSpec(client);

            try {
                Thread.sleep(WAIT_TIME_BETWEEN_REGISTER);
            } catch (Exception ignored) {}

            wfSpecVersion = client.getWfSpec(getWorkflowName(), null).getVersion();
        } catch (LHApiError exn) {
            throw new TestFailure(
                this,
                "Failed deploying test case: " + exn.getMessage()
            );
        }

        log.info("Done deploying for testCase " + getWorkflowName());
    }

    private List<LHTaskWorker> getWorkersFromExecutable(
        Object exe,
        LHWorkerConfig workerConfig
    ) throws TestFailure {
        List<LHTaskWorker> out = new ArrayList<>();

        for (Method method : exe.getClass().getMethods()) {
            if (method.isAnnotationPresent(LHTaskMethod.class)) {
                String taskDefForThisMethod = method
                    .getAnnotation(LHTaskMethod.class)
                    .value();

                out.add(new LHTaskWorker(exe, taskDefForThisMethod, workerConfig));
            }
        }

        return out;
    }

    protected String runWf(LHClient client, Arg... params)
        throws TestFailure, LHApiError {
        return runWf(generateGuid(), client, params);
    }

    protected String runWf(String id, LHClient client, Arg... params)
        throws TestFailure, LHApiError {
        String resultingId = client.runWf(
            getWorkflowName(),
            wfSpecVersion,
            id,
            params
        );

        log.info("Test {} launched: {}", getWorkflowName(), resultingId);

        // Add it so we can cleanup later
        wfRunIds.add(resultingId);

        return resultingId;
    }

    protected String runWithInputsAndCheckPath(
        LHClient client,
        Object input,
        Object... expectedPath
    ) throws TestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("input", input));
        Thread.sleep(400);
        assertStatus(client, wfRunId, LHStatusPb.COMPLETED);
        WfRunPb wfRun = getWfRun(client, wfRunId);

        List<VariableValuePb> actualPath = new ArrayList<>();
        for (int i = 1; i < wfRun.getThreadRuns(0).getCurrentNodePosition(); i++) {
            NodeRunPb nr = getNodeRun(client, wfRunId, 0, i);
            if (nr.getNodeTypeCase() != NodeTypeCase.TASK) {
                continue;
            }
            TaskRunPb taskRun = getTaskRun(client, nr.getTask().getTaskRunId());
            actualPath.add(
                taskRun.getAttempts(taskRun.getAttemptsCount() - 1).getOutput()
            );
        }

        if (expectedPath.length != actualPath.size()) {
            fail("Expected " + (expectedPath.length) + " tasks ", wfRunId, input);
        }

        for (int i = 0; i < expectedPath.length; i++) {
            Object expected = expectedPath[i];
            VariableValuePb actual = actualPath.get(i);
            try {
                if (
                    !LHLibUtil.areVariableValuesEqual(
                        actual,
                        LHLibUtil.objToVarVal(expected)
                    )
                ) {
                    fail("Went down the wrong path!", wfRunId, input);
                }
            } catch (LHSerdeError exn) {
                throw new RuntimeException(exn);
            }
        }

        return wfRunId;
    }
}
