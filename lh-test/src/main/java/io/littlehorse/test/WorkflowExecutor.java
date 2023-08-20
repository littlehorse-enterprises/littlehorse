package io.littlehorse.test;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.PutExternalEventPb;
import io.littlehorse.sdk.common.proto.WfRunPb;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.exception.LHTestTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

public class WorkflowExecutor {

    private final LHClient lhClient;
    private Workflow workflow;

    private final List<Step> steps = new ArrayList<>();

    public WorkflowExecutor(LHClient lhClient) {
        this.lhClient = lhClient;
    }

    public WorkflowExecutor prepare(Workflow workflow) {
        this.workflow = workflow;
        return this;
    }

    public WorkflowExecutor waitForStatus(LHStatusPb lhStatus) {
        steps.add(new WaitForStatusStep(lhStatus, System.out::println));
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
        WfRunVerifier wfRunVerifier = new WfRunVerifier(lhClient, workflow, steps);
        wfRunVerifier.start();
        return wfRunVerifier;
    }

    public class Step {

        protected final Consumer<WfRunPb> wfRunConsumer;

        protected final Duration timeout;

        Step(Consumer<WfRunPb> wfRunConsumer) {
            this.wfRunConsumer = wfRunConsumer;
            this.timeout = Duration.ofSeconds(3);
        }

        void execute(WfRunPb wfRun) {
            this.wfRunConsumer.accept(wfRun);
        }
    }

    private class WaitForStatusStep extends Step {

        private final LHStatusPb lhStatusPb;

        WaitForStatusStep(LHStatusPb lhStatusPb, Consumer<WfRunPb> wfRunConsumer) {
            super(wfRunConsumer);
            this.lhStatusPb = lhStatusPb;
        }

        @Override
        void execute(WfRunPb wfRun) {
            try {
                LocalDateTime expiration = LocalDateTime.now().plus(timeout);
                while (LocalDateTime.now().isBefore(expiration)) {
                    WfRunPb refreshed = lhClient.getWfRun(wfRun.getId());
                    if (refreshed.getStatus().equals(lhStatusPb)) {
                        return;
                    }
                }
                throw new LHTestTimeoutException("Wait for status timeout");
            } catch (LHApiError e) {
                throw new RuntimeException(e);
            }
        }
    }
}
