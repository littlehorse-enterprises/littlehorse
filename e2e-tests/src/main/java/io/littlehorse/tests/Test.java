package io.littlehorse.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ExternalEventIdPb;
import io.littlehorse.sdk.common.proto.ExternalEventPb;
import io.littlehorse.sdk.common.proto.GroupPb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.NodeRunPb;
import io.littlehorse.sdk.common.proto.NodeRunPb.NodeTypeCase;
import io.littlehorse.sdk.common.proto.PutExternalEventPb;
import io.littlehorse.sdk.common.proto.SearchTaskRunPb;
import io.littlehorse.sdk.common.proto.SearchTaskRunPb.StatusAndTaskDefPb;
import io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunPb;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.TaskRunIdPb;
import io.littlehorse.sdk.common.proto.TaskRunPb;
import io.littlehorse.sdk.common.proto.TaskStatusPb;
import io.littlehorse.sdk.common.proto.UserPb;
import io.littlehorse.sdk.common.proto.UserTaskRunIdPb;
import io.littlehorse.sdk.common.proto.UserTaskRunPb;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusPb;
import io.littlehorse.sdk.common.proto.VariablePb;
import io.littlehorse.sdk.common.proto.VariableValuePb;
import io.littlehorse.sdk.common.proto.WfRunPb;
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

    public void assertStatus(LHClient client, String wfRunId, LHStatusPb status)
        throws TestFailure, LHApiError {
        WfRunPb wfRun = getWfRun(client, wfRunId);
        if (wfRun.getStatus() != status) {
            throw new TestFailure(
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

    public WfRunPb getWfRun(LHClient client, String id)
        throws TestFailure, LHApiError {
        WfRunPb result;
        try {
            result = client.getWfRun(id);
        } catch (Exception exn) {
            throw new TestFailure(
                this,
                "Couldn't connect to get wfRun: " + exn.getMessage()
            );
        }

        if (result == null) {
            throw new TestFailure(this, "Couldn't find wfRun " + id);
        }

        return result;
    }

    protected VariableValuePb objToVarVal(Object o, String exnMessage)
        throws TestFailure {
        try {
            return LHLibUtil.objToVarVal(o);
        } catch (LHSerdeError exn) {
            throw new TestFailure(this, exnMessage + ": " + exn.getMessage());
        }
    }

    public void assertVarEqual(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        String name,
        Object desiredValue
    ) throws TestFailure {
        VariableValuePb var = getVariable(client, wfRunId, threadRunNumber, name)
            .getValue();

        if (
            !LHLibUtil.areVariableValuesEqual(
                objToVarVal(desiredValue, "Couldn't convert desiredValue to var val"),
                var
            )
        ) {
            throw new TestFailure(
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

    // Soon we will put a similar method to this in the LHClient for convenience.
    // However, we'll have to be very careful about it since we will need to support
    // pagination via BookmarkPb's.
    public SearchTaskRunReplyPb searchTaskRuns(
        String taskDefName,
        TaskStatusPb status
    ) throws LHApiError {
        return client
            .getGrpcClient()
            .searchTaskRun(
                SearchTaskRunPb
                    .newBuilder()
                    .setStatusAndTaskDef(
                        StatusAndTaskDefPb
                            .newBuilder()
                            .setStatus(status)
                            .setTaskDefName(taskDefName)
                            .build()
                    )
                    .build()
            );
    }

    public SearchUserTaskRunReplyPb searchUserTaskRunsUserGroup(
        String userGroup,
        String userTaskDefName,
        UserTaskRunStatusPb status
    ) throws LHApiError {
        SearchUserTaskRunPb req = SearchUserTaskRunPb
            .newBuilder()
            .setGroup(GroupPb.newBuilder().setId(userGroup).build())
            .setUserTaskDefName(userTaskDefName)
            .setStatus(status)
            .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public SearchUserTaskRunReplyPb searchUserTaskRunsUserGroup(
        String userGroup,
        UserTaskRunStatusPb status
    ) throws LHApiError {
        SearchUserTaskRunPb req = SearchUserTaskRunPb
            .newBuilder()
            .setGroup(GroupPb.newBuilder().setId(userGroup).build())
            .setStatus(status)
            .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public SearchUserTaskRunReplyPb searchUserTaskRunsUserGroup(String userGroup)
        throws LHApiError {
        SearchUserTaskRunPb req = SearchUserTaskRunPb
            .newBuilder()
            .setGroup(GroupPb.newBuilder().setId(userGroup).build())
            .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public SearchUserTaskRunReplyPb searchUserTaskRunsUserId(
        String userId,
        String userTaskDefName,
        UserTaskRunStatusPb status
    ) throws LHApiError {
        SearchUserTaskRunPb req = SearchUserTaskRunPb
            .newBuilder()
            .setUser(UserPb.newBuilder().setId(userId).build())
            .setUserTaskDefName(userTaskDefName)
            .setStatus(status)
            .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public SearchUserTaskRunReplyPb searchUserTaskRunsUserId(
        String userId,
        UserTaskRunStatusPb status
    ) throws LHApiError {
        SearchUserTaskRunPb req = SearchUserTaskRunPb
            .newBuilder()
            .setUser(UserPb.newBuilder().setId(userId).build())
            .setStatus(status)
            .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public SearchUserTaskRunReplyPb searchUserTaskRunsUserId(String userId)
        throws LHApiError {
        SearchUserTaskRunPb req = SearchUserTaskRunPb
            .newBuilder()
            .setUser(UserPb.newBuilder().setId(userId).build())
            .build();
        return client.getGrpcClient().searchUserTaskRun(req);
    }

    public void assertTaskOutput(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        int nodeRunPosition,
        Object expectedOutput
    ) throws TestFailure, LHApiError {
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
            throw new TestFailure(
                this,
                "Did not get expected node output on " +
                wfRunId +
                ", " +
                threadRunNumber +
                ", " +
                nodeRunPosition +
                ", expected:\n" +
                expectedOutput
            );
        }
    }

    public TaskRunPb getTaskRun(LHClient client, TaskRunIdPb taskRunId)
        throws TestFailure, LHApiError {
        TaskRunPb result;
        try {
            result = client.getTaskRun(taskRunId);
        } catch (Exception exn) {
            throw new TestFailure(
                this,
                "Couldn't connect to get taskrun: " + exn.getMessage()
            );
        }

        if (result == null) {
            throw new TestFailure(this, "Couldn't find taskRun.");
        }
        return result;
    }

    public NodeRunPb getNodeRun(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        int nodeRunPosition
    ) throws TestFailure, LHApiError {
        NodeRunPb result;
        try {
            result = client.getNodeRun(wfRunId, threadRunNumber, nodeRunPosition);
        } catch (Exception exn) {
            throw new TestFailure(
                this,
                "Couldn't connect to get wfRun: " + exn.getMessage()
            );
        }

        if (result == null) {
            throw new TestFailure(
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

    public UserTaskRunPb getUserTaskRun(LHClient client, UserTaskRunIdPb id)
        throws TestFailure, LHApiError {
        UserTaskRunPb result;
        try {
            result = client.getUserTaskRun(id);
        } catch (Exception exn) {
            throw new TestFailure(
                this,
                "Couldn't connect to get UserTaskRun: " + exn.getMessage()
            );
        }

        if (result == null) {
            throw new TestFailure(
                this,
                "Couldn't find userTaskRun " +
                id.getWfRunId() +
                "/" +
                id.getUserTaskGuid()
            );
        }

        return result;
    }

    public VariablePb getVariable(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        String name
    ) throws TestFailure {
        VariablePb result;

        try {
            result = client.getVariable(wfRunId, threadRunNumber, name);
        } catch (Exception exn) {
            throw new TestFailure(
                this,
                "Couldn't connect to server: " + exn.getMessage()
            );
        }

        if (result == null) {
            throw new TestFailure(
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
    ) throws TestFailure {
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
            throw new TestFailure(
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
    ) throws TestFailure {
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
            throw new TestFailure(
                this,
                "Couldn't deserialize variable " + varName + ":" + exn.getMessage()
            );
        }
    }

    public static String generateGuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    protected ExternalEventIdPb sendEvent(
        LHClient client,
        String wfRunId,
        String eventName,
        Object content,
        String guid
    ) throws TestFailure, LHApiError {
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
            throw new TestFailure(
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
    ) throws TestFailure, LHApiError {
        ExternalEventPb reply;
        try {
            reply =
                client.getExternalEvent(
                    eventId.getWfRunId(),
                    eventId.getExternalEventDefName(),
                    eventId.getGuid()
                );
        } catch (Exception exn) {
            throw new TestFailure(
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
    ) throws TestFailure, LHApiError {
        WfRunPb wfRun = getWfRun(client, wfRunId);
        if (wfRun.getThreadRuns(threadRunId).getStatus() != status) {
            throw new TestFailure(
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

    public List<VariableValuePb> getTaskRunOutputs(
        LHClient client,
        String wfRunId,
        int threadRunNumber
    ) throws TestFailure, LHApiError {
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

    protected void assertThat(boolean assertion, String message) throws TestFailure {
        if (!assertion) {
            throw new TestFailure(this, "Test case failed: " + message);
        }
    }

    public void assertTaskOutputsMatch(
        LHClient client,
        String wfRunId,
        int threadRunNumber,
        Object... desiredOutputs
    ) throws TestFailure, LHApiError {
        List<VariableValuePb> actual = getTaskRunOutputs(
            client,
            wfRunId,
            threadRunNumber
        );

        if (actual.size() != desiredOutputs.length) {
            throw new TestFailure(
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
                throw new TestFailure(
                    this,
                    "Node outputs didn't match on the " + i + " th task execution!"
                );
            }
        }
    }

    protected void fail(String message, String wfRunId, Object input)
        throws TestFailure {
        throw new TestFailure(
            this,
            "WfRun " +
            wfRunId +
            " Evaluated conditions wrong: " +
            message +
            "\n with input " +
            input.toString()
        );
    }
}
