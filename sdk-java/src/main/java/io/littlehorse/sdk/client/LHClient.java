package io.littlehorse.sdk.client;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHClientConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.GetExternalEventDefResponse;
import io.littlehorse.sdk.common.proto.GetExternalEventResponse;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest;
import io.littlehorse.sdk.common.proto.GetNodeRunResponse;
import io.littlehorse.sdk.common.proto.GetTaskDefResponse;
import io.littlehorse.sdk.common.proto.GetTaskRunResponse;
import io.littlehorse.sdk.common.proto.GetUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.GetVariableResponse;
import io.littlehorse.sdk.common.proto.GetWfRunResponse;
import io.littlehorse.sdk.common.proto.GetWfSpecResponse;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.PutExternalEventDefResponse;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.PutExternalEventResponse;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutTaskDefResponse;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutUserTaskDefResponse;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecResponse;
import io.littlehorse.sdk.common.proto.ResumeWfRunRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.RunWfResponse;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest.StatusAndSpecRequest;
import io.littlehorse.sdk.common.proto.SearchWfRunResponse;
import io.littlehorse.sdk.common.proto.StopWfRunRequest;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.util.Arg;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class LHClient {

    private LHClientConfig config;
    private LHPublicApiBlockingStub client;

    /**
     * Creates a client given a properties object
     *
     * @param props settings
     */
    public LHClient(Properties props) {
        this.config = new LHClientConfig(props);
    }

    /**
     * Creates a client given a client config
     *
     * @param config existing client config
     */
    public LHClient(LHClientConfig config) {
        this.config = config;
    }

    /**
     * Returns the underline RPC client
     *
     * @return A blocking stub
     * @throws LHApiError If it could not connect to the API
     */
    public LHPublicApiBlockingStub getGrpcClient() throws LHApiError {
        if (client == null) {
            try {
                client = config.getBlockingStub();
            } catch (IOException exn) {
                throw new LHApiError(exn, "Could not connect to the API");
            }
        }
        return client;
    }

    /**
     * Gets a external event
     *
     * @param name External event name
     * @return A external event definition, or null if it does not exist
     * @throws LHApiError If it could not connect to the API
     */
    public ExternalEventDef getExternalEventDef(String name) throws LHApiError {
        GetExternalEventDefResponse reply = (GetExternalEventDefResponse) doRequest(() -> {
            return getGrpcClient()
                    .getExternalEventDef(
                            ExternalEventDefId.newBuilder().setName(name).build());
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    /**
     * Gets a task definition given the name
     *
     * @param name Name of the task
     * @return A task definition, or null if it does not exist
     * @throws LHApiError if it failed contacting to the API
     */
    public TaskDef getTaskDef(String name) throws LHApiError {
        GetTaskDefResponse reply = (GetTaskDefResponse) doRequest(() -> {
            return getGrpcClient()
                    .getTaskDef(TaskDefId.newBuilder().setName(name).build());
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    /**
     * Gets the workflow specification for a given workflow name
     *
     * @param name Workflow name
     * @return A workflow specification with the workflow's data and status, or null if the spec
     *     does not exist
     * @throws LHApiError if it failed contacting to the API
     */
    public WfSpec getWfSpec(String name) throws LHApiError {
        return getWfSpec(name, null);
    }

    /**
     * Gets the workflow specification for a given workflow name and version
     *
     * @param name Workflow name
     * @param version Version of the registered workflow
     * @return A workflow specification with the workflow's data and status, or null if the spec
     *     does not exist
     * @throws LHApiError if it failed contacting to the API
     */
    public WfSpec getWfSpec(String name, Integer version) throws LHApiError {
        GetWfSpecResponse reply = (GetWfSpecResponse) doRequest(() -> {
            if (version != null) {
                return getGrpcClient()
                        .getWfSpec(WfSpecId.newBuilder()
                                .setName(name)
                                .setVersion(version)
                                .build());
            } else {
                return getGrpcClient()
                        .getLatestWfSpec(GetLatestWfSpecRequest.newBuilder()
                                .setName(name)
                                .build());
            }
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    /**
     * Returns a TaskRun given an id
     *
     * @param id of the UserTaskRun
     * @return The TaskRun if it exists, or null if it does not exist.
     * @throws LHApiError if it failed contacting to the API
     */
    public TaskRun getTaskRun(TaskRunId id) throws LHApiError {
        GetTaskRunResponse reply = (GetTaskRunResponse) doRequest(() -> {
            return getGrpcClient().getTaskRun(id);
        });
        if (reply.hasResult()) {
            return reply.getResult();
        }
        return null;
    }

    /**
     * Returns a UserTaskRun given an id
     *
     * @param id of the UserTaskRun
     * @return The UserTaskRun if exists, else null.
     * @throws LHApiError if it failed contacting to the API
     */
    public UserTaskRun getUserTaskRun(UserTaskRunId id) throws LHApiError {
        GetUserTaskRunResponse reply = (GetUserTaskRunResponse) doRequest(() -> {
            return getGrpcClient().getUserTaskRun(id);
        });
        if (reply.hasResult()) {
            return reply.getResult();
        }
        return null;
    }

    /**
     * Returns a NodeRun given a workflow, thread run number and position in the ThreadRun
     *
     * @param wfRunId workflow run identification
     * @param threadRunNumber number of the relevant thread
     * @param position in the thread
     * @return null if the NodeRun does not exist, or the NodeRun's data otherwise
     * @throws LHApiError if it failed contacting to the API
     */
    public NodeRun getNodeRun(String wfRunId, int threadRunNumber, int position) throws LHApiError {
        GetNodeRunResponse reply = (GetNodeRunResponse) doRequest(() -> {
            return getGrpcClient()
                    .getNodeRun(NodeRunId.newBuilder()
                            .setWfRunId(wfRunId)
                            .setThreadRunNumber(threadRunNumber)
                            .setPosition(position)
                            .build());
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    /**
     * Returns a passed variable to a thread
     *
     * @param wfRunId workflow run identification
     * @param threadRunNumber number of the subsequent thread
     * @param name of the variable
     * @return The variable's data if it does exist, or null otherwise
     * @throws LHApiError if it failed contacting to the API
     */
    public Variable getVariable(String wfRunId, int threadRunNumber, String name) throws LHApiError {
        GetVariableResponse reply = (GetVariableResponse) doRequest(() -> {
            return getGrpcClient()
                    .getVariable(VariableId.newBuilder()
                            .setWfRunId(wfRunId)
                            .setThreadRunNumber(threadRunNumber)
                            .setName(name)
                            .build());
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    /**
     * Returns an emitted external event for a specific workflow run
     *
     * @param wfRunId workflow run identification
     * @param externalEventName name of the external event
     * @param guid external event's global unique identifier
     * @return The external event's data if it does exist, or null otherwise
     * @throws LHApiError if it failed contacting to the API
     */
    public ExternalEvent getExternalEvent(String wfRunId, String externalEventName, String guid) throws LHApiError {
        GetExternalEventResponse reply = (GetExternalEventResponse) doRequest(() -> {
            return getGrpcClient()
                    .getExternalEvent(ExternalEventId.newBuilder()
                            .setWfRunId(wfRunId)
                            .setGuid(guid)
                            .setExternalEventDefName(externalEventName)
                            .build());
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    /**
     * Gets a workflow run given an id
     *
     * @param id of the workflow run
     * @return A workflow run if it does exist, null otherwise
     * @throws LHApiError
     */
    public WfRun getWfRun(String id) throws LHApiError {
        GetWfRunResponse reply = (GetWfRunResponse) doRequest(() -> {
            return getGrpcClient().getWfRun(WfRunId.newBuilder().setId(id).build());
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    /**
     * Returns a list of ids which match the input parameters
     *
     * @param workflowName Name to search for
     * @param version Version to search for
     * @param status Status of WfRuns to search for (STARTING, RUNNING, COMPLETED, HALTING, HALTED,
     *     ERROR)
     * @param earliestStart Only shows WfRuns more recent than this configuration
     * @param latestStart Only shows WfRuns less recent than this configuration
     * @return A list of workflow ids
     * @throws LHApiError If there is an error when connecting to the server
     */
    public List<WfRunId> searchWfRun(
            String workflowName, int version, LHStatus status, Date earliestStart, Date latestStart) throws LHApiError {
        StatusAndSpecRequest.Builder statusBuilder = StatusAndSpecRequest.newBuilder()
                .setWfSpecName(workflowName)
                .setWfSpecVersion(version)
                .setStatus(status);

        if (earliestStart != null) {
            statusBuilder.setEarliestStart(LHLibUtil.fromDate(earliestStart));
        }

        if (latestStart != null) {
            statusBuilder.setLatestStart(LHLibUtil.fromDate(latestStart));
        }

        StatusAndSpecRequest statusAndSpecPb = statusBuilder.build();

        SearchWfRunResponse reply = (SearchWfRunResponse) doRequest(() -> {
            return getGrpcClient()
                    .searchWfRun(SearchWfRunRequest.newBuilder()
                            .setStatusAndSpec(statusAndSpecPb)
                            .build());
        });

        return reply.getResultsList();
    }

    /**
     * Makes a request to the server to execute a workflow specification. The execution of the WfRun
     * is asynchronous; the RunWf request only blocks until the first step of the WfRun is
     * *scheduled*.
     *
     * @param wfSpecName workflow name
     * @param wfSpecVersion workflow version. This is optional, pass null for the server to decide
     * @param wfRunId workflow run id. This is optional, pass null for the server to decide
     * @param args list of variables. This is optional
     * @return The workflow run identification
     * @throws LHApiError If there is an error when connecting to the server
     */
    public String runWf(String wfSpecName, Integer wfSpecVersion, String wfRunId, Arg... args) throws LHApiError {
        RunWfRequest.Builder req = RunWfRequest.newBuilder().setWfSpecName(wfSpecName);
        if (wfRunId != null) {
            req.setId(wfRunId);
        }
        if (wfSpecVersion != null) {
            req.setWfSpecVersion(wfSpecVersion);
        }
        for (Arg arg : args) {
            try {
                req.putVariables(arg.name, LHLibUtil.objToVarVal(arg.value));
            } catch (LHSerdeError exn) {
                throw new LHApiError(exn, "Couldn't serialize input variable " + arg.name);
            }
        }

        RunWfResponse response = (RunWfResponse) doRequest(() -> {
            return getGrpcClient().runWf(req.build());
        });

        if (response.hasWfRunId()) {
            return response.getWfRunId();
        } else {
            // to prevent any missed validation
            throw new LHApiError(response.getMessage(), response.getCode());
        }
    }

    /**
     * Emits an external event to the server
     *
     * @param req put external event request
     * @return An external event when successful
     * @throws LHApiError If there is an error when connecting to the server
     */
    public ExternalEvent putExternalEvent(PutExternalEventRequest req) throws LHApiError {
        PutExternalEventResponse response = (PutExternalEventResponse) doRequest(() -> {
            return getGrpcClient().putExternalEvent(req);
        });
        if (response.hasResult()) {
            return response.getResult();
        } else {
            // to prevent any missed validation
            throw new LHApiError(response.getMessage(), response.getCode());
        }
    }

    /**
     * Creates a new external event definition. It throws and error if already exists
     *
     * @param req put external event definition request
     * @return The definition's data when successful
     * @throws LHApiError If there is an error when connecting to the server
     */
    public ExternalEventDef putExternalEventDef(PutExternalEventDefRequest req) throws LHApiError {
        return putExternalEventDef(req, false);
    }

    /**
     * Creates a new external event definition
     *
     * @param req put external event definition request
     * @param swallowAlreadyExists this flag defines if whether or not to throw an error if it
     *     already exist
     * @return The definition's data when successful
     * @throws LHApiError If there is an error when connecting to the server
     */
    public ExternalEventDef putExternalEventDef(PutExternalEventDefRequest req, boolean swallowAlreadyExists)
            throws LHApiError {
        PutExternalEventDefResponse response = (PutExternalEventDefResponse) doRequest(
                () -> {
                    return getGrpcClient().putExternalEventDef(req);
                },
                swallowAlreadyExists);
        if (response.hasResult()) {
            return response.getResult();
        } else {
            // to prevent any missed validation
            throw new LHApiError(response.getMessage(), response.getCode());
        }
    }

    /**
     * Creates a task definition. It throws and error if already exists
     *
     * @param req request
     * @return Task definition's data
     * @throws LHApiError If there is an error when connecting to the server
     */
    public TaskDef putTaskDef(PutTaskDefRequest req) throws LHApiError {
        return putTaskDef(req, false);
    }

    /**
     * Creates a task definition
     *
     * @param req request
     * @param swallowAlreadyExists this flag defines if whether or not to throw an error if it
     *     already exist
     * @return Task definition's data
     * @throws LHApiError If there is an error when connecting to the server
     */
    public TaskDef putTaskDef(PutTaskDefRequest req, boolean swallowAlreadyExists) throws LHApiError {
        PutTaskDefResponse response = (PutTaskDefResponse) doRequest(
                () -> {
                    return getGrpcClient().putTaskDef(req);
                },
                swallowAlreadyExists);
        if (response.hasResult()) {
            return response.getResult();
        } else {
            // to prevent any missed validation
            throw new LHApiError(response.getMessage(), response.getCode());
        }
    }

    /**
     * Creates a user task definition. It throws and error if already exists
     *
     * @param req request
     * @return User task definition
     * @throws LHApiError If there is an error when connecting to the server
     */
    public UserTaskDef putUserTaskDef(PutUserTaskDefRequest req) throws LHApiError {
        return putUserTaskDef(req, false);
    }

    /**
     * Creates a user task definition
     *
     * @param req request
     * @param swallowAlreadyExists this flag defines if whether or not to throw an error if it
     *     already exist
     * @return User task definition
     * @throws LHApiError If there is an error when connecting to the server
     */
    public UserTaskDef putUserTaskDef(PutUserTaskDefRequest req, boolean swallowAlreadyExists) throws LHApiError {
        PutUserTaskDefResponse response = (PutUserTaskDefResponse) doRequest(
                () -> {
                    return getGrpcClient().putUserTaskDef(req);
                },
                swallowAlreadyExists);
        if (response.hasResult()) {
            return response.getResult();
        } else {
            // to prevent any missed validation
            throw new LHApiError(response.getMessage(), response.getCode());
        }
    }

    /**
     * Creates a new workflow specification. Increments the version of the workflow if already
     * exists.
     *
     * @param req request
     * @return WfSpec's data
     * @throws LHApiError If there is an error when connecting to the server
     */
    public WfSpec putWfSpec(PutWfSpecRequest req) throws LHApiError {
        PutWfSpecResponse response = (PutWfSpecResponse) doRequest(() -> {
            return getGrpcClient().putWfSpec(req);
        });
        if (response.hasResult()) {
            return response.getResult();
        } else {
            // to prevent any missed validation
            throw new LHApiError(response.getMessage(), response.getCode());
        }
    }

    /**
     * Halts a thread for a given workflow run. Move the tread to RUNNING to HALTED
     *
     * @param wfRunId workflow run identification
     * @param threadRunNumber number of the subsequent thread
     * @throws LHApiError If there is an error when connecting to the server
     */
    public void stopWfRun(String wfRunId, int threadRunNumber) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                    .stopWfRun(StopWfRunRequest.newBuilder()
                            .setWfRunId(wfRunId)
                            .setThreadRunNumber(threadRunNumber)
                            .build());
        });
    }

    /**
     * Resume a thread execution. Move the thread form HALTED to RUNNING
     *
     * @param wfRunId workflow run identification
     * @param threadRunNumber number of the subsequent thread
     * @throws LHApiError If there is an error when connecting to the server
     */
    public void resumeWfRun(String wfRunId, int threadRunNumber) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                    .resumeWfRun(ResumeWfRunRequest.newBuilder()
                            .setWfRunId(wfRunId)
                            .setThreadRunNumber(threadRunNumber)
                            .build());
        });
    }

    /**
     * Deletes a user task definition given a name
     *
     * @param userTaskDefName name of the task to be deleted
     * @throws LHApiError If there is an error when connecting to the server
     */
    public void deleteUserTaskDef(String userTaskDefName) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                    .deleteUserTaskDef(DeleteUserTaskDefRequest.newBuilder()
                            .setName(userTaskDefName)
                            .build());
        });
    }

    /**
     * Deletes a workflow run given its id
     *
     * @param wfRunId id of the workflow run to be deleted
     * @throws LHApiError If there is an error when connecting to the server
     */
    public void deleteWfRun(String wfRunId) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                    .deleteWfRun(
                            DeleteWfRunRequest.newBuilder().setWfRunId(wfRunId).build());
        });
    }

    /**
     * Deletes a task definition given its name
     *
     * @param name of the task definition to be deleted
     * @throws LHApiError If there is an error when connecting to the server
     */
    public void deleteTaskDef(String name) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                    .deleteTaskDef(
                            DeleteTaskDefRequest.newBuilder().setName(name).build());
        });
    }

    /**
     * Delete san external event definition given its name
     *
     * @param name of the external event definition to be deleted
     * @throws LHApiError If there is an error when connecting to the server
     */
    public void deleteExternalEventDef(String name) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                    .deleteExternalEventDef(DeleteExternalEventDefRequest.newBuilder()
                            .setName(name)
                            .build());
        });
    }

    /**
     * Deletes a workflow specification given its name and version
     *
     * @param name of the specification
     * @param version of the specification
     * @throws LHApiError If there is an error when connecting to the server
     */
    public void deleteWfSpec(String name, int version) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                    .deleteWfSpec(DeleteWfSpecRequest.newBuilder()
                            .setName(name)
                            .setVersion(version)
                            .build());
        });
    }

    private MessageOrBuilder doRequest(LHRequest request) throws LHApiError {
        return doRequest(request, false);
    }

    private MessageOrBuilder doRequest(LHRequest request, boolean swallowAlreadyExists) throws LHApiError {
        MessageOrBuilder response;
        try {
            response = request.doRequest();
        } catch (LHApiError exn) {
            throw exn;
        } catch (Exception exn) {
            throw new LHApiError("Failed contacting LH API: " + exn.getMessage(), LHResponseCode.CONNECTION_ERROR);
        }

        try {
            Method getCodeMethod = response.getClass().getMethod("getCode");

            LHResponseCode code = (LHResponseCode) getCodeMethod.invoke(response);
            if (code == LHResponseCode.ALREADY_EXISTS_ERROR && swallowAlreadyExists) {
                return response;
            }

            switch (code) {
                case OK:
                case NOT_FOUND_ERROR:
                    return response;
                case ALREADY_EXISTS_ERROR:
                case CONNECTION_ERROR:
                case BAD_REQUEST_ERROR:
                case REPORTED_BUT_NOT_PROCESSED:
                case VALIDATION_ERROR:
                case UNRECOGNIZED:
                default:
                    Method getMessageMethod = response.getClass().getMethod("getMessage");
                    throw new LHApiError((String) getMessageMethod.invoke(response), code);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exn) {
            exn.printStackTrace();
            throw new RuntimeException("Should be impossible; this is a bug in LH");
        }
    }
}

interface LHRequest {
    public MessageOrBuilder doRequest() throws LHApiError;
}
