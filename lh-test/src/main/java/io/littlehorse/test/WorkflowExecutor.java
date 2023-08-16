package io.littlehorse.test;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb;
import io.littlehorse.sdk.common.proto.ExternalEventPb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.PutExternalEventPb;
import io.littlehorse.sdk.common.proto.WfRunPb;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WorkflowExecutor {

    private final LHClient lhClient;
    private Workflow workflow;

    private final List<WorkflowExecutorStep> steps = new ArrayList<>();

    public WorkflowExecutor(LHClient lhClient) {
        this.lhClient = lhClient;
    }

    public WorkflowExecutor prepare(Workflow workflow) {
        this.workflow = workflow;
        return this;
    }

    public WorkflowExecutor waitForStatus(LHStatusPb lhStatus) {
        /*steps.add(new WorkflowExecutorStep(wfRun -> {
            Stream.generate(() -> wfRun)
                    .map(WfRunPb::getId)
                    .map(this::findWfRun)
                            .collect()
            lhClient.getWfRun(wfRun.getId()).getStatus() == lhStatus;
        }));*/
        return this;
    }

    private WfRunPb findWfRun(String wfRunId) {
        try {
            return lhClient.getWfRun(wfRunId);
        } catch (LHApiError e) {
            throw new RuntimeException(e);
        }
    }

    public WorkflowExecutor andThenExecute(
        CompleteUserTaskRunPb completeUserTaskRun
    ) {
        return this;
    }

    public WorkflowExecutor andThenSend(PutExternalEventPb externalEvent) {
        return this;
    }

    public WfRunVerifier verify() {
        return null;
    }

    private class WorkflowExecutorStep {

        private final Consumer<WfRunPb> wfRunConsumer;

        private final Duration timeout;

        WorkflowExecutorStep(Consumer<WfRunPb> wfRunConsumer) {
            this.wfRunConsumer = wfRunConsumer;
            this.timeout = Duration.ofSeconds(3);
        }

        void execute(WfRunPb wfRun) {
            this.wfRunConsumer.accept(wfRun);
        }
    }
}
