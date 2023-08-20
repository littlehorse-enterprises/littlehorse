package io.littlehorse.test;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.WfRun;
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

    public WorkflowExecutor waitForStatus(LHStatus lhStatus) {
        steps.add(new WaitForStatusStep(lhStatus, System.out::println));
        return this;
    }

    private WfRun findWfRun(String wfRunId) {
        try {
            return lhClient.getWfRun(wfRunId);
        } catch (LHApiError e) {
            throw new RuntimeException(e);
        }
    }

    public WorkflowExecutor andThenExecute(
        CompleteUserTaskRunRequest completeUserTaskRun
    ) {
        return this;
    }

    public WorkflowExecutor andThenSend(PutExternalEventRequest externalEvent) {
        return this;
    }

    public WfRunVerifier verify() {
        WfRunVerifier wfRunVerifier = new WfRunVerifier(lhClient, workflow, steps);
        wfRunVerifier.start();
        return wfRunVerifier;
    }

    public class Step {

        protected final Consumer<WfRun> wfRunConsumer;

        protected final Duration timeout;

        Step(Consumer<WfRun> wfRunConsumer) {
            this.wfRunConsumer = wfRunConsumer;
            this.timeout = Duration.ofSeconds(3);
        }

        void execute(WfRun wfRun) {
            this.wfRunConsumer.accept(wfRun);
        }
    }

    private class WaitForStatusStep extends Step {

        private final LHStatus lhStatusPb;

        WaitForStatusStep(LHStatus lhStatusPb, Consumer<WfRun> wfRunConsumer) {
            super(wfRunConsumer);
            this.lhStatusPb = lhStatusPb;
        }

        @Override
        void execute(WfRun wfRun) {
            try {
                LocalDateTime expiration = LocalDateTime.now().plus(timeout);
                while (LocalDateTime.now().isBefore(expiration)) {
                    WfRun refreshed = lhClient.getWfRun(wfRun.getId());
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
