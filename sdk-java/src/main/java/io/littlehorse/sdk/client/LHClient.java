package io.littlehorse.sdk.client;

import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Timestamp;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHClientConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb;
import io.littlehorse.sdk.common.proto.DeleteTaskDefPb;
import io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb;
import io.littlehorse.sdk.common.proto.DeleteWfRunPb;
import io.littlehorse.sdk.common.proto.DeleteWfSpecPb;
import io.littlehorse.sdk.common.proto.ExternalEventDefIdPb;
import io.littlehorse.sdk.common.proto.ExternalEventDefPb;
import io.littlehorse.sdk.common.proto.ExternalEventIdPb;
import io.littlehorse.sdk.common.proto.ExternalEventPb;
import io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb;
import io.littlehorse.sdk.common.proto.GetExternalEventReplyPb;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecPb;
import io.littlehorse.sdk.common.proto.GetNodeRunReplyPb;
import io.littlehorse.sdk.common.proto.GetTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.GetTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.GetVariableReplyPb;
import io.littlehorse.sdk.common.proto.GetWfRunReplyPb;
import io.littlehorse.sdk.common.proto.GetWfSpecReplyPb;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.NodeRunIdPb;
import io.littlehorse.sdk.common.proto.NodeRunPb;
import io.littlehorse.sdk.common.proto.PutExternalEventDefPb;
import io.littlehorse.sdk.common.proto.PutExternalEventDefReplyPb;
import io.littlehorse.sdk.common.proto.PutExternalEventPb;
import io.littlehorse.sdk.common.proto.PutExternalEventReplyPb;
import io.littlehorse.sdk.common.proto.PutTaskDefPb;
import io.littlehorse.sdk.common.proto.PutTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.PutUserTaskDefPb;
import io.littlehorse.sdk.common.proto.PutUserTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.PutWfSpecPb;
import io.littlehorse.sdk.common.proto.PutWfSpecReplyPb;
import io.littlehorse.sdk.common.proto.ResumeWfRunPb;
import io.littlehorse.sdk.common.proto.RunWfPb;
import io.littlehorse.sdk.common.proto.RunWfReplyPb;
import io.littlehorse.sdk.common.proto.SearchWfRunPb;
import io.littlehorse.sdk.common.proto.SearchWfRunPb.StatusAndSpecPb;
import io.littlehorse.sdk.common.proto.SearchWfRunPb.StatusAndSpecPb.Builder;
import io.littlehorse.sdk.common.proto.SearchWfRunReplyPb;
import io.littlehorse.sdk.common.proto.StopWfRunPb;
import io.littlehorse.sdk.common.proto.TaskDefIdPb;
import io.littlehorse.sdk.common.proto.TaskDefPb;
import io.littlehorse.sdk.common.proto.TaskRunIdPb;
import io.littlehorse.sdk.common.proto.TaskRunPb;
import io.littlehorse.sdk.common.proto.UserTaskDefPb;
import io.littlehorse.sdk.common.proto.VariableIdPb;
import io.littlehorse.sdk.common.proto.VariablePb;
import io.littlehorse.sdk.common.proto.WfRunIdPb;
import io.littlehorse.sdk.common.proto.WfRunPb;
import io.littlehorse.sdk.common.proto.WfSpecIdPb;
import io.littlehorse.sdk.common.proto.WfSpecPb;
import io.littlehorse.sdk.common.util.Arg;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

public class LHClient {

    private LHClientConfig config;
    private LHPublicApiBlockingStub client;

    /**
     * Creates a client given a properties object
     * @param props settings
     */
    public LHClient(Properties props) {
        this.config = new LHClientConfig(props);
    }

    /**
     * Creates a client given a client config
     * @param config existing client config
     */
    public LHClient(LHClientConfig config) {
        this.config = config;
    }

    /**
     * Returns the underline RPC client
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
     * @param name External event name
     * @return A external event definition
     * @throws LHApiError If it could not connect to the API
     */
    public ExternalEventDefPb getExternalEventDef(String name) throws LHApiError {
        GetExternalEventDefReplyPb reply = (GetExternalEventDefReplyPb) doRequest(() -> {
                return getGrpcClient()
                    .getExternalEventDef(
                        ExternalEventDefIdPb.newBuilder().setName(name).build()
                    );
            }
        );

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    /**
     * Gets a task definition given the name
     * @param name Name of the task
     * @return A task definition
     * @throws LHApiError if it failed contacting to the API
     */
    public TaskDefPb getTaskDef(String name) throws LHApiError {
        GetTaskDefReplyPb reply = (GetTaskDefReplyPb) doRequest(() -> {
            return getGrpcClient()
                .getTaskDef(TaskDefIdPb.newBuilder().setName(name).build());
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    /**
     * Gets the workflow specification for a given workflow name
     * @param name Workflow name
     * @return A workflow specification with the workflow's data and status
     * @throws LHApiError if it failed contacting to the API
     */
    public WfSpecPb getWfSpec(String name) throws LHApiError {
        return getWfSpec(name, null);
    }

    /**
     * Gets the workflow specification for a given workflow name and version
     * @param name Workflow name
     * @param version Version of the registered workflow
     * @return A workflow specification with the workflow's data and status
     * @throws LHApiError if it failed contacting to the API
     */
    public WfSpecPb getWfSpec(String name, Integer version) throws LHApiError {
        GetWfSpecReplyPb reply = (GetWfSpecReplyPb) doRequest(() -> {
            if (version != null) {
                return getGrpcClient()
                    .getWfSpec(
                        WfSpecIdPb
                            .newBuilder()
                            .setName(name)
                            .setVersion(version)
                            .build()
                    );
            } else {
                return getGrpcClient()
                    .getLatestWfSpec(
                        GetLatestWfSpecPb.newBuilder().setName(name).build()
                    );
            }
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    /**
     * Return a task execution data given an id
     * @param id of the task run
     * @return The task run that has already been executed
     * @throws LHApiError if it failed contacting to the API
     */
    public TaskRunPb getTaskRun(TaskRunIdPb id) throws LHApiError {
        GetTaskRunReplyPb reply = (GetTaskRunReplyPb) doRequest(() -> {
            return getGrpcClient().getTaskRun(id);
        });
        if (reply.hasResult()) {
            return reply.getResult();
        }
        return null;
    }

    public NodeRunPb getNodeRun(String wfRunId, int threadRunNumber, int position)
        throws LHApiError {
        GetNodeRunReplyPb reply = (GetNodeRunReplyPb) doRequest(() -> {
            return getGrpcClient()
                .getNodeRun(
                    NodeRunIdPb
                        .newBuilder()
                        .setWfRunId(wfRunId)
                        .setThreadRunNumber(threadRunNumber)
                        .setPosition(position)
                        .build()
                );
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    public VariablePb getVariable(String wfRunId, int threadRunNumber, String name)
        throws LHApiError {
        GetVariableReplyPb reply = (GetVariableReplyPb) doRequest(() -> {
            return getGrpcClient()
                .getVariable(
                    VariableIdPb
                        .newBuilder()
                        .setWfRunId(wfRunId)
                        .setThreadRunNumber(threadRunNumber)
                        .setName(name)
                        .build()
                );
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    public ExternalEventPb getExternalEvent(
        String wfRunId,
        String externalEventName,
        String guid
    ) throws LHApiError {
        GetExternalEventReplyPb reply = (GetExternalEventReplyPb) doRequest(() -> {
            return getGrpcClient()
                .getExternalEvent(
                    ExternalEventIdPb
                        .newBuilder()
                        .setWfRunId(wfRunId)
                        .setGuid(guid)
                        .setExternalEventDefName(externalEventName)
                        .build()
                );
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    public WfRunPb getWfRun(String id) throws LHApiError {
        GetWfRunReplyPb reply = (GetWfRunReplyPb) doRequest(() -> {
            return getGrpcClient().getWfRun(WfRunIdPb.newBuilder().setId(id).build());
        });

        if (reply.hasResult()) {
            return reply.getResult();
        } else {
            return null;
        }
    }

    /**
     * Returns a list of ids which match the input parameters or null otherwise
     * @param workflowName Name to search for
     * @param version Version to search for
     * @param status Status of WfRuns to search for (STARTING, RUNNING, COMPLETED, HALTING, HALTED, ERROR)
     * @param earliestStart Only shows WfRuns more recent than this configuration
     * @param latestStart Only shows WfRuns less recent than this configuration
     * @return A list of workflow ids
     * @throws LHApiError If there is an error when connecting to the server
     */
    public List<WfRunIdPb> searchWfRun(
        String workflowName,
        int version,
        LHStatusPb status,
        Timestamp earliestStart,
        Timestamp latestStart
    ) throws LHApiError {
        Builder statusBuilder = StatusAndSpecPb
            .newBuilder()
            .setWfSpecName(workflowName)
            .setWfSpecVersion(version)
            .setStatus(status);

        if (earliestStart != null) {
            statusBuilder.setEarliestStart(earliestStart);
        }

        if (latestStart != null) {
            statusBuilder.setLatestStart(latestStart);
        }

        StatusAndSpecPb statusAndSpecPb = statusBuilder.build();

        SearchWfRunReplyPb reply = (SearchWfRunReplyPb) doRequest(() -> {
            return getGrpcClient()
                .searchWfRun(
                    SearchWfRunPb
                        .newBuilder()
                        .setStatusAndSpec(statusAndSpecPb)
                        .build()
                );
        });

        if (reply.getResultsCount() > 0) {
            return reply.getResultsList();
        } else {
            return null;
        }
    }

    public String runWf(
        String wfSpecName,
        Integer wfSpecVersion,
        String wfRunId,
        Arg... args
    ) throws LHApiError {
        RunWfPb.Builder req = RunWfPb.newBuilder().setWfSpecName(wfSpecName);
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
                throw new LHApiError(
                    exn,
                    "Couldn't serialize input variable " + arg.name
                );
            }
        }

        RunWfReplyPb response = (RunWfReplyPb) doRequest(() -> {
            return getGrpcClient().runWf(req.build());
        });

        if (response.hasWfRunId()) {
            return response.getWfRunId();
        } else {
            return null;
        }
    }

    public ExternalEventPb putExternalEvent(PutExternalEventPb req)
        throws LHApiError {
        PutExternalEventReplyPb response = (PutExternalEventReplyPb) doRequest(() -> {
            return getGrpcClient().putExternalEvent(req);
        });
        if (response.hasResult()) {
            return response.getResult();
        } else {
            return null;
        }
    }

    public ExternalEventDefPb putExternalEventDef(PutExternalEventDefPb req)
        throws LHApiError {
        return putExternalEventDef(req, false);
    }

    public ExternalEventDefPb putExternalEventDef(
        PutExternalEventDefPb req,
        boolean swallowAlreadyExists
    ) throws LHApiError {
        PutExternalEventDefReplyPb response = (PutExternalEventDefReplyPb) doRequest(
            () -> {
                return getGrpcClient().putExternalEventDef(req);
            },
            swallowAlreadyExists
        );
        if (response.hasResult()) {
            return response.getResult();
        } else {
            return null;
        }
    }

    public TaskDefPb putTaskDef(PutTaskDefPb req) throws LHApiError {
        return putTaskDef(req, false);
    }

    public TaskDefPb putTaskDef(PutTaskDefPb req, boolean swallowAlreadyExists)
        throws LHApiError {
        PutTaskDefReplyPb response = (PutTaskDefReplyPb) doRequest(
            () -> {
                return getGrpcClient().putTaskDef(req);
            },
            swallowAlreadyExists
        );
        if (response.hasResult()) {
            return response.getResult();
        } else {
            return null;
        }
    }

    public UserTaskDefPb putUserTaskDef(
        PutUserTaskDefPb req,
        boolean swallowAlreadyExists
    ) throws LHApiError {
        PutUserTaskDefReplyPb response = (PutUserTaskDefReplyPb) doRequest(() -> {
            return getGrpcClient().putUserTaskDef(req);
        });
        if (response.hasResult()) {
            return response.getResult();
        } else {
            return null;
        }
    }

    public void deleteUserTaskDef(String userTaskDefName) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                .deleteUserTaskDef(
                    DeleteUserTaskDefPb.newBuilder().setName(userTaskDefName).build()
                );
        });
    }

    public WfSpecPb putWfSpec(PutWfSpecPb req) throws LHApiError {
        PutWfSpecReplyPb response = (PutWfSpecReplyPb) doRequest(() -> {
            return getGrpcClient().putWfSpec(req);
        });
        if (response.hasResult()) {
            return response.getResult();
        } else {
            return null;
        }
    }

    public void stopWfRun(String wfRunId, int threadRunNumber) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                .stopWfRun(
                    StopWfRunPb
                        .newBuilder()
                        .setWfRunId(wfRunId)
                        .setThreadRunNumber(threadRunNumber)
                        .build()
                );
        });
    }

    public void resumeWfRun(String wfRunId, int threadRunNumber) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                .resumeWfRun(
                    ResumeWfRunPb
                        .newBuilder()
                        .setWfRunId(wfRunId)
                        .setThreadRunNumber(threadRunNumber)
                        .build()
                );
        });
    }

    public void deleteWfRun(String wfRunId) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                .deleteWfRun(DeleteWfRunPb.newBuilder().setWfRunId(wfRunId).build());
        });
    }

    public void deleteTaskDef(String name) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                .deleteTaskDef(DeleteTaskDefPb.newBuilder().setName(name).build());
        });
    }

    public void deleteExternalEventDef(String name) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                .deleteExternalEventDef(
                    DeleteExternalEventDefPb.newBuilder().setName(name).build()
                );
        });
    }

    public void deleteWfSpec(String name, int version) throws LHApiError {
        doRequest(() -> {
            return getGrpcClient()
                .deleteWfSpec(
                    DeleteWfSpecPb
                        .newBuilder()
                        .setName(name)
                        .setVersion(version)
                        .build()
                );
        });
    }

    private MessageOrBuilder doRequest(LHRequest request) throws LHApiError {
        return doRequest(request, false);
    }

    private MessageOrBuilder doRequest(
        LHRequest request,
        boolean swallowAlreadyExists
    ) throws LHApiError {
        MessageOrBuilder response;
        try {
            response = request.doRequest();
        } catch (LHApiError exn) {
            throw exn;
        } catch (Exception exn) {
            throw new LHApiError(
                "Failed contacting LH API: " + exn.getMessage(),
                LHResponseCodePb.CONNECTION_ERROR
            );
        }

        try {
            Method getCodeMethod = response.getClass().getMethod("getCode");

            LHResponseCodePb code = (LHResponseCodePb) getCodeMethod.invoke(response);
            if (
                code == LHResponseCodePb.ALREADY_EXISTS_ERROR && swallowAlreadyExists
            ) {
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
                    Method getMessageMethod = response
                        .getClass()
                        .getMethod("getMessage");
                    throw new LHApiError(
                        (String) getMessageMethod.invoke(response),
                        code
                    );
            }
        } catch (
            NoSuchMethodException
            | InvocationTargetException
            | IllegalAccessException exn
        ) {
            exn.printStackTrace();
            throw new RuntimeException("Should be impossible; this is a bug in LH");
        }
    }
}

interface LHRequest {
    public MessageOrBuilder doRequest() throws LHApiError;
}
