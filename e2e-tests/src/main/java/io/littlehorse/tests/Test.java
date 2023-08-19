package io.littlehorse.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.SearchTaskRunRequest;
import io.littlehorse.sdk.common.proto.SearchTaskRunRequest.StatusAndTaskDefRequest;
import io.littlehorse.sdk.common.proto.SearchTaskRunResponse;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.User;
import io.littlehorse.sdk.common.proto.UserGroup;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRun;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * A `Test` is like a Unit Test but it is actually useful: it checks end-to-end
 * behavior of the LittleHorse System.
 *
 * A single `Test` class may be used to check multiple things, with the following
 * caveats:
 * 1. All of the sub-tests should be logically related, for example, testing
 *    conditionals by running the same WfSpec with various different inputs.
 * 2. All of the sub-tests should make use of the same LH Infrastructure, for example
 *    using the same WfSpec and TaskDef.
 *
 * Tests might be really quick (eg. a few seconds), if all it does is deploy a WfSpec
 * and run it with one input.
 *
 * Other tests might take longer, for example if we need to run Workflows with a
 * SLEEP node, and verify the behavior of SLEEP, or if we want to test time filters
 * on search.
 *
 * Multiple Test objects should be able to safely run in parallel and not stomp
 * over each other.
 *
 * Lastly, the `cleanup()` method should be idempotent and should remove all
 * test-specific resources (WfSpec, TaskDef, WfRun, etc) even if the `test()`
 * method threw a `TestFailure`.
 */
public abstract class Test {

    protected LHClient client;
    protected LHWorkerConfig workerConfig;

    public Test(LHClient client, LHWorkerConfig workerConfig) {
        this.client = client;
        this.workerConfig = workerConfig;
    }

    public abstract void cleanup() throws Exception;

    public abstract String getDescription();

    public abstract void test() throws Exception;

    public void assertStatus(LHClient client, String wfRunId, LHStatus status) throws TestFailure, LHApiError {
        WfRun wfRun = getWfRun(client, wfRunId);
        if (wfRun.getStatus() != status) {
            throw new TestFailure(
                    this,
                    "Expected status " + status + " for wfRun " + wfRunId + " but got status " + wfRun.getStatus());
        }
    }

    public WfRun getWfRun(LHClient client, String id) throws TestFailure, LHApiError {
        WfRun result;
        try {
            result = client.getWfRun(id);
        } catch (Exception exn) {
            throw new TestFailure(this, "Couldn't connect to get wfRun: " + exn.getMessage());
        }

        if (result == null) {
            throw new TestFailure(this, "Couldn't find wfRun " + id);
        }

        return result;
    }

    protected VariableValue objToVarVal(Object o, String exnMessage) throws TestFailure {
        try {
            return LHLibUtil.objToVarVal(o);
        } catch (LHSerdeError exn) {
            throw new TestFailure(this, exnMessage + ": " + exn.getMessage());
        }
    }

    public void assertVarEqual(LHClient client, String wfRunId, int threadRunNumber, String name, Object desiredValue)
            throws TestFailure {
        VariableValue var = getVariable(client, wfRunId, threadRunNumber, name).getValue();

        if (!LHLibUtil.areVariableValuesEqual(
                objToVarVal(desiredValue, "Couldn't convert desiredValue to var val"), var)) {
            throw new TestFailure(
                    this, "WfRun " + wfRunId + " Variable " + name + " thread " + threadRunNumber + " wrong value");
        }
    }

    // Soon we will put a similar method to this in the LHClient for convenience.
    // However, we'll have to be very careful about it since we will need to support
    // pagination via Bookmark's.
    public SearchTaskRunResponse searchTaskRuns(String taskDefName, TaskStatus status) throws LHApiError {
        return client.getGrpcClient()
                .searchTaskRun(SearchTaskRunRequest.newBuilder()
                        .setStatusAndTaskDef(StatusAndTaskDefRequest.newBuilder()
                                .setStatus(status)
                                .setTaskDefName(taskDefName)
                                .build())
                        .build());
    }

    public SearchUserTaskRunResponse searchUserTaskRunsUserGroup(
            String userGroup, String userTaskDefName, UserTaskRunStatus status) throws LHApiError {
        SearchUserTaskRunRequest req = SearchUserTaskRunRequest.newBuilder()
                .setUserGroup(UserGroup.newBuilder().setId(userGroup).build())
                .setUserTaskDefName(userTaskDefName)
                .setStatus(status)
                .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public SearchUserTaskRunResponse searchUserTaskRunsUserGroup(String userGroup, UserTaskRunStatus status)
            throws LHApiError {
        SearchUserTaskRunRequest req = SearchUserTaskRunRequest.newBuilder()
                .setUserGroup(UserGroup.newBuilder().setId(userGroup).build())
                .setStatus(status)
                .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public SearchUserTaskRunResponse searchUserTaskRunsUserGroup(String userGroup) throws LHApiError {
        SearchUserTaskRunRequest req = SearchUserTaskRunRequest.newBuilder()
                .setUserGroup(UserGroup.newBuilder().setId(userGroup).build())
                .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public SearchUserTaskRunResponse searchUserTaskRunsUserId(
            String userId, String userTaskDefName, UserTaskRunStatus status) throws LHApiError {
        SearchUserTaskRunRequest req = SearchUserTaskRunRequest.newBuilder()
                .setUser(User.newBuilder().setId(userId).build())
                .setUserTaskDefName(userTaskDefName)
                .setStatus(status)
                .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public SearchUserTaskRunResponse searchUserTaskRunsUserId(String userId, UserTaskRunStatus status)
            throws LHApiError {
        SearchUserTaskRunRequest req = SearchUserTaskRunRequest.newBuilder()
                .setUser(User.newBuilder().setId(userId).build())
                .setStatus(status)
                .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public SearchUserTaskRunResponse searchUserTaskRunsUserId(String userId) throws LHApiError {
        SearchUserTaskRunRequest req = SearchUserTaskRunRequest.newBuilder()
                .setUser(User.newBuilder().setId(userId).build())
                .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public void assertTaskOutput(
            LHClient client, String wfRunId, int threadRunNumber, int nodeRunPosition, Object expectedOutput)
            throws TestFailure, LHApiError {
        NodeRun nodeRun = getNodeRun(client, wfRunId, threadRunNumber, nodeRunPosition);
        VariableValue expectedVarVal = objToVarVal(expectedOutput, "Couldn't convert expected output to varval");

        TaskRun taskRun = getTaskRun(client, nodeRun.getTask().getTaskRunId());
        if (!LHLibUtil.areVariableValuesEqual(
                expectedVarVal,
                taskRun.getAttempts(taskRun.getAttemptsCount() - 1).getOutput())) {
            throw new TestFailure(
                    this,
                    "Did not get expected node output on "
                            + wfRunId
                            + ", "
                            + threadRunNumber
                            + ", "
                            + nodeRunPosition
                            + ", expected:\n"
                            + expectedOutput);
        }
    }

    public TaskRun getTaskRun(LHClient client, TaskRunId taskRunId) throws TestFailure, LHApiError {
        TaskRun result;
        try {
            result = client.getTaskRun(taskRunId);
        } catch (Exception exn) {
            throw new TestFailure(this, "Couldn't connect to get taskrun: " + exn.getMessage());
        }

        if (result == null) {
            throw new TestFailure(this, "Couldn't find taskRun.");
        }
        return result;
    }

    public NodeRun getNodeRun(LHClient client, String wfRunId, int threadRunNumber, int nodeRunPosition)
            throws TestFailure, LHApiError {
        NodeRun result;
        try {
            result = client.getNodeRun(wfRunId, threadRunNumber, nodeRunPosition);
        } catch (Exception exn) {
            throw new TestFailure(this, "Couldn't connect to get wfRun: " + exn.getMessage());
        }

        if (result == null) {
            throw new TestFailure(
                    this, "Couldn't find nodeRun " + wfRunId + " " + threadRunNumber + " " + nodeRunPosition);
        }

        return result;
    }

    public UserTaskRun getUserTaskRun(LHClient client, UserTaskRunId id) throws TestFailure, LHApiError {
        UserTaskRun result;
        try {
            result = client.getUserTaskRun(id);
        } catch (Exception exn) {
            throw new TestFailure(this, "Couldn't connect to get UserTaskRun: " + exn.getMessage());
        }

        if (result == null) {
            throw new TestFailure(this, "Couldn't find userTaskRun " + id.getWfRunId() + "/" + id.getUserTaskGuid());
        }

        return result;
    }

    public Variable getVariable(LHClient client, String wfRunId, int threadRunNumber, String name) throws TestFailure {
        Variable result;

        try {
            result = client.getVariable(wfRunId, threadRunNumber, name);
        } catch (Exception exn) {
            throw new TestFailure(this, "Couldn't connect to server: " + exn.getMessage());
        }

        if (result == null) {
            throw new TestFailure(this, "Couldn't find variable " + wfRunId + " " + threadRunNumber + " " + name);
        }

        return result;
    }

    public List<?> getVarAsList(LHClient client, String wfRunId, int threadRunNumber, String varName)
            throws TestFailure {
        VariableValue varVal =
                getVariable(client, wfRunId, threadRunNumber, varName).getValue();
        try {
            return LHLibUtil.deserializeFromjson(varVal.getJsonArr(), List.class);
        } catch (JsonProcessingException exn) {
            throw new TestFailure(this, "Couldn't deserialize variable " + varName + ":" + exn.getMessage());
        }
    }

    public <T> T getVarAsObj(LHClient client, String wfRunId, int threadRunNumber, String varName, Class<T> cls)
            throws TestFailure {
        VariableValue varVal =
                getVariable(client, wfRunId, threadRunNumber, varName).getValue();
        try {
            return LHLibUtil.deserializeFromjson(varVal.getJsonObj(), cls);
        } catch (JsonProcessingException exn) {
            throw new TestFailure(this, "Couldn't deserialize variable " + varName + ":" + exn.getMessage());
        }
    }

    public static String generateGuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    protected ExternalEventId sendEvent(LHClient client, String wfRunId, String eventName, Object content, String guid)
            throws TestFailure, LHApiError {
        VariableValue varVal = objToVarVal(content, "Failed converting event input");
        if (guid == null) {
            guid = generateGuid();
        }
        PutExternalEventRequest.Builder req = PutExternalEventRequest.newBuilder()
                .setContent(varVal)
                .setWfRunId(wfRunId)
                .setExternalEventDefName(eventName)
                .setGuid(guid);
        try {
            client.putExternalEvent(req.build());
        } catch (Exception exn) {
            throw new TestFailure(this, "Failed posting external event: " + exn.getMessage());
        }

        return ExternalEventId.newBuilder()
                .setExternalEventDefName(eventName)
                .setGuid(guid)
                .setWfRunId(wfRunId)
                .build();
    }

    protected ExternalEvent getExternalEvent(LHClient client, ExternalEventId eventId) throws TestFailure, LHApiError {
        ExternalEvent reply;
        try {
            reply = client.getExternalEvent(eventId.getWfRunId(), eventId.getExternalEventDefName(), eventId.getGuid());
        } catch (Exception exn) {
            throw new TestFailure(this, "Failed getting ExternalEvent: " + exn.getMessage());
        }
        return reply;
    }

    public void assertThreadStatus(LHClient client, String wfRunId, int threadRunId, LHStatus status)
            throws TestFailure, LHApiError {
        WfRun wfRun = getWfRun(client, wfRunId);
        if (wfRun.getThreadRuns(threadRunId).getStatus() != status) {
            throw new TestFailure(
                    this,
                    "Expected status "
                            + status
                            + " for wfRun "
                            + wfRunId
                            + " but got status "
                            + wfRun.getThreadRuns(threadRunId).getStatus());
        }
    }

    public List<VariableValue> getTaskRunOutputs(LHClient client, String wfRunId, int threadRunNumber)
            throws TestFailure, LHApiError {
        List<VariableValue> out = new ArrayList<>();

        int numNodes = getWfRun(client, wfRunId).getThreadRuns(threadRunNumber).getCurrentNodePosition();
        // skip entrypoint node
        for (int i = 1; i <= numNodes; i++) {
            NodeRun nr = getNodeRun(client, wfRunId, threadRunNumber, i);
            if (nr.getNodeTypeCase() == NodeTypeCase.TASK) {
                TaskRun taskRun = getTaskRun(client, nr.getTask().getTaskRunId());
                out.add(taskRun.getAttempts(taskRun.getAttemptsCount() - 1).getOutput());
            }
        }
        return out;
    }

    protected void assertThat(boolean assertion, String message) throws TestFailure {
        if (!assertion) {
            throw new TestFailure(this, "Test case failed: " + message);
        }
    }

    public void assertTaskOutputsMatch(LHClient client, String wfRunId, int threadRunNumber, Object... desiredOutputs)
            throws TestFailure, LHApiError {
        List<VariableValue> actual = getTaskRunOutputs(client, wfRunId, threadRunNumber);

        if (actual.size() != desiredOutputs.length) {
            throw new TestFailure(
                    this,
                    "Expected "
                            + desiredOutputs.length
                            + " task runs but got "
                            + actual.size()
                            + " on wfRun "
                            + wfRunId
                            + " thread "
                            + threadRunNumber);
        }

        for (int i = 0; i < desiredOutputs.length; i++) {
            VariableValue desired = objToVarVal(desiredOutputs[i], "Yikes couldn't convert");

            if (!LHLibUtil.areVariableValuesEqual(desired, actual.get(i))) {
                throw new TestFailure(this, "Node outputs didn't match on the " + i + " th task execution!");
            }
        }
    }

    protected void fail(String message, String wfRunId, Object input) throws TestFailure {
        throw new TestFailure(
                this,
                "WfRun " + wfRunId + " Evaluated conditions wrong: " + message + "\n with input " + input.toString());
    }
}
