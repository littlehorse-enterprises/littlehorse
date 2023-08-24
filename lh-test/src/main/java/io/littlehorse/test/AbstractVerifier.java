package io.littlehorse.test;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.exception.LHTestInitializationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.awaitility.Awaitility;

public class AbstractVerifier implements Verifier {
    protected final LHClientTestWrapper lhClientTestWrapper;
    protected final Workflow workflow;
    private final Collection<Arg> workflowArgs;
    protected final List<WfRunVerifier.Step> steps = new ArrayList<>();

    public AbstractVerifier(LHClientTestWrapper lhClientTestWrapper, Workflow workflow, Collection<Arg> workflowArgs) {
        this.lhClientTestWrapper = lhClientTestWrapper;
        this.workflow = workflow;
        this.workflowArgs = workflowArgs;
    }

    @Override
    public void start() {
        try {
            lhClientTestWrapper.registerWfSpec(workflow);
            WfSpec wfSpec = Awaitility.await().until(() -> lhClientTestWrapper.getWfSpec(workflow), Objects::nonNull);
            String wfId = UUID.randomUUID().toString();

            Awaitility.await()
                    .ignoreException(StatusRuntimeException.class)
                    .until(() -> lhClientTestWrapper.runWf(wfSpec, wfId, workflowArgs));

            WfRun wfRun = Awaitility.await().until(() -> lhClientTestWrapper.getWfRun(wfId), Objects::nonNull);
            steps.forEach(step -> step.execute(wfRun.getId()));
        } catch (Exception e) {
            throw new LHTestInitializationException(e);
        }
    }
}
