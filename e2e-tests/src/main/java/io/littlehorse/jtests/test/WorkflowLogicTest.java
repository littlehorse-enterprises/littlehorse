package io.littlehorse.jtests.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.littlehorse.jlib.client.LHClient;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.config.LHWorkerConfig;
import io.littlehorse.jlib.common.exception.LHApiError;
import io.littlehorse.jlib.common.exception.LHSerdeError;
import io.littlehorse.jlib.common.proto.ExternalEventIdPb;
import io.littlehorse.jlib.common.proto.ExternalEventPb;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.NodeRunPb;
import io.littlehorse.jlib.common.proto.NodeRunPb.NodeTypeCase;
import io.littlehorse.jlib.common.proto.PutExternalEventDefPb;
import io.littlehorse.jlib.common.proto.PutExternalEventPb;
import io.littlehorse.jlib.common.proto.TaskRunIdPb;
import io.littlehorse.jlib.common.proto.TaskRunPb;
import io.littlehorse.jlib.common.proto.VariablePb;
import io.littlehorse.jlib.common.proto.VariableValuePb;
import io.littlehorse.jlib.common.proto.WfRunPb;
import io.littlehorse.jlib.common.util.Arg;
import io.littlehorse.jlib.wfsdk.Workflow;
import io.littlehorse.jlib.worker.LHTaskMethod;
import io.littlehorse.jlib.worker.LHTaskWorker;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WorkflowLogicTest extends Test {

    private static final int WAIT_TIME_BETWEEN_REGISTER = 500;
    private static Logger log = LoggerFactory.getLogger(WorkflowLogicTest.class);

    public abstract List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, LHApiError, InterruptedException;

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

    private void deploy(LHClient client, LHWorkerConfig workerConfig)
        throws LogicTestFailure {
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
                    throw new LogicTestFailure(
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
                throw new LogicTestFailure(
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
            throw new LogicTestFailure(
                this,
                "Failed deploying test case: " + exn.getMessage()
            );
        }

        log.info("Done deploying for testCase " + getWorkflowName());
    }

    private List<LHTaskWorker> getWorkersFromExecutable(
        Object exe,
        LHWorkerConfig workerConfig
    ) throws LogicTestFailure {
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
        throws LogicTestFailure, LHApiError {
        return runWf(generateGuid(), client, params);
    }

    public static String generateGuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    protected String runWf(String id, LHClient client, Arg... params)
        throws LogicTestFailure, LHApiError {
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

    protected ExternalEventIdPb sendEvent(
        LHClient client,
        String wfRunId,
        String eventName,
        Object content,
        String guid
    ) throws LogicTestFailure, LHApiError {
        VariableValuePb varVal = objToVarVal(
            content,
            "Failed converting event input"
        );
        if (guid == null) {
            guid = generateGuid();
        }
        PutExternalEventPb.Builder req = PutExternalEventPb
            .newBuilder()
            .setContent(varVal)
            .setWfRunId(wfRunId)
            .setExternalEventDefName(eventName)
            .setGuid(guid);
        try {
            client.putExternalEvent(req.build());
        } catch (Exception exn) {
            throw new LogicTestFailure(
                this,
                "Failed posting external event: " + exn.getMessage()
            );
        }

        return ExternalEventIdPb
            .newBuilder()
            .setExternalEventDefName(eventName)
            .setGuid(guid)
            .setWfRunId(wfRunId)
            .build();
    }

    protected ExternalEventPb getExternalEvent(
        LHClient client,
        ExternalEventIdPb eventId
    ) throws LogicTestFailure, LHApiError {
        ExternalEventPb reply;
        try {
            reply =
                client.getExternalEvent(
                    eventId.getWfRunId(),
                    eventId.getExternalEventDefName(),
                    eventId.getGuid()
                );
        } catch (Exception exn) {
            throw new LogicTestFailure(
                this,
                "Failed getting ExternalEvent: " + exn.getMessage()
            );
        }
        return reply;
    }

    public void assertThreadStatus(
        LHClient client,
        String wfRunId,
        int threadRunId,
        LHStatusPb status
    ) throws LogicTestFailure, LHApiError {
        WfRunPb wfRun = getWfRun(client, wfRunId);
        if (wfRun.getThreadRuns(threadRunId).getStatus() != status) {
            throw new LogicTestFailure(
                this,
                "Expected status " +
                status +
                " for wfRun " +
                wfRunId +
                " but got status " +
                wfRun.getThreadRuns(threadRunId).getStatus()
            );
        }
    }

    public void assertStatus(LHClient client, String wfRunId, LHStatusPb status)
        throws LogicTestFailure, LHApiError {
        WfRunPb wfRun = getWfRun(client, wfRunId);
        if (wfRun.getStatus() != status) {
            throw new LogicTestFailure(
                this,
                "Expected status " +
                status +
                " for wfRun " +
                wfRunId +
                " but got status " +
                wfRun.getStatus()
            );
        }
    }

    public List<VariableValuePb> getTaskRunOutputs(
        LHClient client,
        String wfRunId,
        int threadRunNumber
    ) throws LogicTestFailure, LHApiError {
        List<VariableValuePb> out = new ArrayList<>();

        int numNodes = getWfRun(client, wfRunId)
            .getThreadRuns(threadRunNumber)
            .getCurrentNodePosition();
        // skip entrypoint node
        for (int i = 1; i <= numNodes; i++) {
            NodeRunPb nr = getNodeRun(client, wfRunId, threadRunNumber, i);
            if (nr.getNodeTypeCase() == NodeTypeCase.TASK) {
                TaskRunPb taskRun = getTaskRun(client, nr.getTask().getTaskRunId());
                out.add(
                    taskRun.getAttempts(taskRun.getAttemptsCount() - 1).getOutput()
                );
            }
        }
        return out;
    }

    public void assertTaskOutputsMatch(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        Object... desiredOutputs
    ) throws LogicTestFailure, LHApiError {
        List<VariableValuePb> actual = getTaskRunOutputs(
            client,
            wfRunId,
            threadRunNumber
        );

        if (actual.size() != desiredOutputs.length) {
            throw new LogicTestFailure(
                this,
                "Expected " +
                desiredOutputs.length +
                " task runs but got " +
                actual.size() +
                " on wfRun " +
                wfRunId +
                " thread " +
                threadRunNumber
            );
        }

        for (int i = 0; i < desiredOutputs.length; i++) {
            VariableValuePb desired = objToVarVal(
                desiredOutputs[i],
                "Yikes couldn't convert"
            );

            if (!LHLibUtil.areVariableValuesEqual(desired, actual.get(i))) {
                throw new LogicTestFailure(
                    this,
                    "Node outputs didn't match on the " + i + " th task execution!"
                );
            }
        }
    }

    protected void fail(String message, String wfRunId, Object input)
        throws LogicTestFailure {
        throw new LogicTestFailure(
            this,
            "WfRun " +
            wfRunId +
            " Evaluated conditions wrong: " +
            message +
            "\n with input " +
            input.toString()
        );
    }

    protected String runWithInputsAndCheckPath(
        LHClient client,
        Object input,
        Object... expectedPath
    ) throws LogicTestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("input", input));
        Thread.sleep(200);
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

    public void assertTaskOutput(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        int nodeRunPosition,
        Object expectedOutput
    ) throws LogicTestFailure, LHApiError {
        NodeRunPb nodeRun = getNodeRun(
            client,
            wfRunId,
            threadRunNumber,
            nodeRunPosition
        );
        VariableValuePb expectedVarVal = objToVarVal(
            expectedOutput,
            "Couldn't convert expected output to varval"
        );

        TaskRunPb taskRun = getTaskRun(client, nodeRun.getTask().getTaskRunId());
        if (
            !LHLibUtil.areVariableValuesEqual(
                expectedVarVal,
                taskRun.getAttempts(taskRun.getAttemptsCount() - 1).getOutput()
            )
        ) {
            throw new LogicTestFailure(
                this,
                "Did not get expected node output on " +
                wfRunId +
                ", " +
                threadRunNumber +
                ", " +
                nodeRunPosition
            );
        }
    }

    public TaskRunPb getTaskRun(LHClient client, TaskRunIdPb taskRunId)
        throws LogicTestFailure, LHApiError {
        TaskRunPb result;
        try {
            result = client.getTaskRun(taskRunId);
        } catch (Exception exn) {
            throw new LogicTestFailure(
                this,
                "Couldn't connect to get taskrun: " + exn.getMessage()
            );
        }

        if (result == null) {
            throw new LogicTestFailure(this, "Couldn't find taskRun.");
        }
        return result;
    }

    public NodeRunPb getNodeRun(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        int nodeRunPosition
    ) throws LogicTestFailure, LHApiError {
        NodeRunPb result;
        try {
            result = client.getNodeRun(wfRunId, threadRunNumber, nodeRunPosition);
        } catch (Exception exn) {
            throw new LogicTestFailure(
                this,
                "Couldn't connect to get wfRun: " + exn.getMessage()
            );
        }

        if (result == null) {
            throw new LogicTestFailure(
                this,
                "Couldn't find nodeRun " +
                wfRunId +
                " " +
                threadRunNumber +
                " " +
                nodeRunPosition
            );
        }

        return result;
    }

    public VariablePb getVariable(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        String name
    ) throws LogicTestFailure {
        VariablePb result;

        try {
            result = client.getVariable(wfRunId, threadRunNumber, name);
        } catch (Exception exn) {
            throw new LogicTestFailure(
                this,
                "Couldn't connect to server: " + exn.getMessage()
            );
        }

        if (result == null) {
            throw new LogicTestFailure(
                this,
                "Couldn't find variable " +
                wfRunId +
                " " +
                threadRunNumber +
                " " +
                name
            );
        }

        return result;
    }

    public List<?> getVarAsList(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        String varName
    ) throws LogicTestFailure {
        VariableValuePb varVal = getVariable(
            client,
            wfRunId,
            threadRunNumber,
            varName
        )
            .getValue();
        try {
            return LHLibUtil.deserializeFromjson(varVal.getJsonArr(), List.class);
        } catch (JsonProcessingException exn) {
            throw new LogicTestFailure(
                this,
                "Couldn't deserialize variable " + varName + ":" + exn.getMessage()
            );
        }
    }

    public <T> T getVarAsObj(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        String varName,
        Class<T> cls
    ) throws LogicTestFailure {
        VariableValuePb varVal = getVariable(
            client,
            wfRunId,
            threadRunNumber,
            varName
        )
            .getValue();
        try {
            return LHLibUtil.deserializeFromjson(varVal.getJsonObj(), cls);
        } catch (JsonProcessingException exn) {
            throw new LogicTestFailure(
                this,
                "Couldn't deserialize variable " + varName + ":" + exn.getMessage()
            );
        }
    }

    public void assertVarEqual(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        String name,
        Object desiredValue
    ) throws LogicTestFailure {
        VariableValuePb var = getVariable(client, wfRunId, threadRunNumber, name)
            .getValue();

        if (
            !LHLibUtil.areVariableValuesEqual(
                objToVarVal(desiredValue, "Couldn't convert desiredValue to var val"),
                var
            )
        ) {
            throw new LogicTestFailure(
                this,
                "WfRun " +
                wfRunId +
                " Variable " +
                name +
                " thread " +
                threadRunNumber +
                " wrong value"
            );
        }
    }

    public WfRunPb getWfRun(LHClient client, String id)
        throws LogicTestFailure, LHApiError {
        WfRunPb result;
        try {
            result = client.getWfRun(id);
        } catch (Exception exn) {
            throw new LogicTestFailure(
                this,
                "Couldn't connect to get wfRun: " + exn.getMessage()
            );
        }

        if (result == null) {
            throw new LogicTestFailure(this, "Couldn't find wfRun " + id);
        }

        return result;
    }

    private VariableValuePb objToVarVal(Object o, String exnMessage)
        throws LogicTestFailure {
        try {
            return LHLibUtil.objToVarVal(o);
        } catch (LHSerdeError exn) {
            throw new LogicTestFailure(this, exnMessage + ": " + exn.getMessage());
        }
    }
}
