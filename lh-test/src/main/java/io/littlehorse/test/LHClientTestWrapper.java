package io.littlehorse.test;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.exception.LHTestInitializationException;
import java.util.Collection;

public class LHClientTestWrapper {

    private final LHClient lhClient;

    public LHClientTestWrapper(LHClient lhClient) {
        this.lhClient = lhClient;
    }

    public NodeRun getNodeRun(String wfRunId, int threadRunNumber, int nodeRunNumber) {
        try {
            return lhClient.getNodeRun(wfRunId, threadRunNumber, nodeRunNumber);
        } catch (LHApiError e) {
            throw new LHTestInitializationException(e);
        }
    }

    public TaskRun getTaskRun(TaskRunId taskRunId) {
        try {
            return lhClient.getTaskRun(taskRunId);
        } catch (LHApiError e) {
            throw new LHTestInitializationException(e);
        }
    }

    public LHStatus getWfRunStatus(String wfRunId) {
        try {
            WfRun wfRun = lhClient.getWfRun(wfRunId);
            if (wfRun != null) {
                return wfRun.getStatus();
            }
            return null;
        } catch (LHApiError e) {
            throw new LHTestInitializationException(e);
        }
    }

    public WfRun getWfRun(String wfId) {
        try {
            return lhClient.getWfRun(wfId);
        } catch (LHApiError e) {
            throw new LHTestInitializationException(e);
        }
    }

    public boolean runWf(WfSpec wfSpec, String wfId, Collection<Arg> args) throws LHApiError {
        lhClient.runWf(wfSpec.getName(), wfSpec.getVersion(), wfId, args.toArray(new Arg[] {}));
        return true;
    }

    public void registerWfSpec(Workflow workflow) {
        try {
            workflow.registerWfSpec(lhClient);
        } catch (LHApiError e) {
            throw new LHTestInitializationException(e);
        }
    }

    public WfSpec getWfSpec(Workflow workflow) {
        try {
            return lhClient.getWfSpec(workflow.getName());
        } catch (LHApiError e) {
            throw new LHTestInitializationException(e);
        }
    }
}
