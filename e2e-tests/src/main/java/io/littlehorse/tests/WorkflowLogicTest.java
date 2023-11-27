package io.littlehorse.tests;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WorkflowLogicTest extends Test {

    private static final int WAIT_TIME_BETWEEN_REGISTER = 500;
    private static Logger log = LoggerFactory.getLogger(WorkflowLogicTest.class);

    public abstract List<String> launchAndCheckWorkflows(LHPublicApiBlockingStub client)
            throws TestFailure, IOException, InterruptedException;

    protected abstract Workflow getWorkflowImpl();

    public abstract List<Object> getTaskWorkerObjects();

    private Workflow workflow;
    private List<LHTaskWorker> workers;
    private List<String> wfRunIds;
    private int majorVersion; // usually will be 0
    private int revision; // usually will be 0

    public WorkflowLogicTest(LHPublicApiBlockingStub client, LHConfig workerConfig) {
        super(client, workerConfig);
        wfRunIds = new ArrayList<>();
    }

    public void test() throws Exception {
        deploy(client, workerConfig);
        launchAndCheckWorkflows(client);
    }

    public void cleanup() {
        log.info("Shutting down task workers for testcase {}", getWorkflowName());
        for (LHTaskWorker worker : workers) {
            worker.close();
        }

        for (String wfRunId : wfRunIds) {
            client.deleteWfRun(DeleteWfRunRequest.newBuilder()
                    .setId(WfRunId.newBuilder().setId(wfRunId))
                    .build());
        }

        for (String tdn : workflow.getRequiredTaskDefNames()) {
            client.deleteTaskDef(DeleteTaskDefRequest.newBuilder()
                    .setId(TaskDefId.newBuilder().setName(tdn))
                    .build());
        }

        for (String eedn : workflow.getRequiredExternalEventDefNames()) {
            client.deleteExternalEventDef(DeleteExternalEventDefRequest.newBuilder()
                    .setId(ExternalEventDefId.newBuilder().setName(eedn))
                    .build());
        }

        client.deleteWfSpec(DeleteWfSpecRequest.newBuilder()
                .setId(WfSpecId.newBuilder().setName(getWorkflowName()).setMajorVersion(majorVersion).setRevision(revision))
                .build());
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

    protected void deploy(LHPublicApiBlockingStub client, LHConfig workerConfig) throws TestFailure {
        workers = new ArrayList<>();

        // Now need to create LHTaskWorkers and run them for all worker objects.
        for (Object executable : getTaskWorkerObjects()) {
            try {
                for (LHTaskWorker worker : getWorkersFromExecutable(executable, workerConfig)) {
                    workers.add(worker);
                    worker.registerTaskDef(true);
                    Thread.sleep(WAIT_TIME_BETWEEN_REGISTER);

                    worker.start();
                }
            } catch (IOException | InterruptedException exn) {
                throw new RuntimeException(exn);
            }
        }

        Set<String> requiredExternalEventDefNames = getWorkflow().getRequiredExternalEventDefNames();

        for (String externalEvent : requiredExternalEventDefNames) {
            try {
                client.putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                        .setName(externalEvent)
                        .build());
            } catch (StatusRuntimeException exn) {
                if (exn.getStatus().getCode() != Code.ALREADY_EXISTS) {
                    throw exn;
                }
            }
        }

        try {
            Thread.sleep(WAIT_TIME_BETWEEN_REGISTER);
        } catch (Exception ignored) {
        }

        // Now deploy the WF
        getWorkflow().registerWfSpec(client);

        try {
            Thread.sleep(WAIT_TIME_BETWEEN_REGISTER);
        } catch (Exception ignored) {
        }

        majorVersion = client.getLatestWfSpec(GetLatestWfSpecRequest.newBuilder()
                        .setName(getWorkflowName())
                        .build())
                .getId().getMajorVersion();

        revision = client.getLatestWfSpec(GetLatestWfSpecRequest.newBuilder()
                .setName(getWorkflowName())
                .build())
        .getId().getRevision();

        log.info("Done deploying for testCase " + getWorkflowName());
    }

    private List<LHTaskWorker> getWorkersFromExecutable(Object exe, LHConfig workerConfig)
            throws TestFailure, IOException {
        List<LHTaskWorker> out = new ArrayList<>();

        for (Method method : exe.getClass().getMethods()) {
            if (method.isAnnotationPresent(LHTaskMethod.class)) {
                String taskDefForThisMethod =
                        method.getAnnotation(LHTaskMethod.class).value();

                out.add(new LHTaskWorker(exe, taskDefForThisMethod, workerConfig));
            }
        }

        return out;
    }

    protected String runWf(LHPublicApiBlockingStub client, Arg... params) throws TestFailure, IOException {
        return runWf(generateGuid(), client, params);
    }

    protected String runWf(String id, LHPublicApiBlockingStub client, Arg... params) throws TestFailure, IOException {
        RunWfRequest.Builder b =
                RunWfRequest.newBuilder().setWfSpecName(getWorkflowName()).setMajorVersion(majorVersion).setRevision(revision);

        if (id != null) {
            b.setId(id);
        }

        for (Arg arg : params) {
            try {
                b.putVariables(arg.name, LHLibUtil.objToVarVal(arg.value));
            } catch (LHSerdeError exn) {
                throw new RuntimeException(exn);
            }
        }

        WfRunId resultingId = client.runWf(b.build()).getId();

        log.info("Test {} launched: {}", getWorkflowName(), resultingId);

        // Add it so we can cleanup later
        wfRunIds.add(resultingId.getId());

        return resultingId.getId();
    }

    protected String runWithInputsAndCheckPath(LHPublicApiBlockingStub client, Object input, Object... expectedPath)
            throws TestFailure, InterruptedException, IOException {
        String wfRunId = runWf(client, Arg.of("input", input));
        Thread.sleep(100 * (expectedPath.length + 1));
        assertStatus(client, wfRunId, LHStatus.COMPLETED);
        WfRun wfRun = getWfRun(client, wfRunId);

        List<VariableValue> actualPath = new ArrayList<>();
        for (int i = 1; i < wfRun.getThreadRuns(0).getCurrentNodePosition(); i++) {
            NodeRun nr = getNodeRun(client, wfRunId, 0, i);
            if (nr.getNodeTypeCase() != NodeTypeCase.TASK) {
                continue;
            }
            TaskRun taskRun = getTaskRun(client, nr.getTask().getTaskRunId());
            actualPath.add(taskRun.getAttempts(taskRun.getAttemptsCount() - 1).getOutput());
        }

        if (expectedPath.length != actualPath.size()) {
            fail("Expected " + (expectedPath.length) + " tasks ", wfRunId, input);
        }

        for (int i = 0; i < expectedPath.length; i++) {
            Object expected = expectedPath[i];
            VariableValue actual = actualPath.get(i);
            try {
                if (!LHLibUtil.areVariableValuesEqual(actual, LHLibUtil.objToVarVal(expected))) {
                    fail("Went down the wrong path!", wfRunId, input);
                }
            } catch (LHSerdeError exn) {
                throw new RuntimeException(exn);
            }
        }

        return wfRunId;
    }
}
