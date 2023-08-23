package io.littlehorse.test;

import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.exception.LHTestInitializationException;
import java.util.Collection;

public class LHClientTestWrapper {

    private final LHPublicApiBlockingStub lhClient;

    public LHClientTestWrapper(LHPublicApiBlockingStub lhClient) {
        this.lhClient = lhClient;
    }

    public NodeRun getNodeRun(String wfRunId, int threadRunNumber, int nodeRunNumber) {
        try {
            return lhClient.getNodeRun(
                NodeRunId.newBuilder().setWfRunId(wfRunId).setThreadRunNumber(threadRunNumber).setPosition(nodeRunNumber).build());

        } catch (StatusRuntimeException e) {
            throw new LHTestInitializationException(e);
        }
    }

    public TaskRun getTaskRun(TaskRunId taskRunId) {
        try {
            return lhClient.getTaskRun(taskRunId);
        } catch (StatusRuntimeException e) {
            throw new LHTestInitializationException(e);
        }
    }

    public LHStatus getWfRunStatus(String wfRunId) {
        try {
            WfRun wfRun = getWfRun(wfRunId);
            if (wfRun != null) {
                return wfRun.getStatus();
            }
            return null;
        } catch (StatusRuntimeException e) {
            throw new LHTestInitializationException(e);
        }
    }

    public VariableValue getVariableValue(String wfRunId, int threadRunNumber, String variableName) {
        VariableId variableId = VariableId.newBuilder().setName(variableName).setWfRunId(wfRunId).setThreadRunNumber(threadRunNumber).build();
        return lhClient.getVariable(variableId).getValue();
    }

    public WfRun getWfRun(String wfRunId) {
        return lhClient.getWfRun(WfRunId.newBuilder().setId(wfRunId).build());
    }

    public boolean runWf(WfSpec wfSpec, String wfId, Collection<Arg> args) throws StatusRuntimeException {
        RunWfRequest.Builder req = RunWfRequest.newBuilder()
            .setWfSpecName(wfSpec.getName())
            .setId(wfId)
            .setWfSpecVersion(wfSpec.getVersion());

        for (Arg arg : args) {
            try {
                req.putVariables(arg.name, LHLibUtil.objToVarVal(arg.value));
            } catch(LHSerdeError exn) {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withCause(exn).withDescription("Couldn't serialize workflow input"));
            }
        }
        lhClient.runWf(req.build());

        // Why boolean? Wouldn't returning the WfRun or the wfRun.getId() make more sense?
        return true;
    }

    public void registerWfSpec(Workflow workflow) {
        try {
            workflow.registerWfSpec(lhClient);
        } catch (StatusRuntimeException e) {
            throw new LHTestInitializationException(e);
        }
    }

    public WfSpec getWfSpec(Workflow workflow) {
        return lhClient.getLatestWfSpec(GetLatestWfSpecRequest.newBuilder().setName(workflow.getName()).build());
    }
}
