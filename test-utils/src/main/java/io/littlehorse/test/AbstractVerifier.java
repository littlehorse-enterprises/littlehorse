package io.littlehorse.test;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.internal.TestContext;
import io.littlehorse.test.internal.TestExecutionContext;
import io.littlehorse.test.internal.step.Step;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AbstractVerifier implements Verifier {
    protected final LittleHorseBlockingStub lhClient;
    protected final Workflow workflow;
    private final Collection<Arg> workflowArgs;

    private final TestContext context;
    protected final List<Step> steps = new ArrayList<>();

    public AbstractVerifier(TestContext context, Workflow workflow, Collection<Arg> workflowArgs) {
        this.lhClient = context.getLhClient();
        this.workflow = workflow;
        this.workflowArgs = workflowArgs;
        this.context = context;
    }

    @Override
    public WfRunId start(WfRunId wfRunId) {
        WfSpec wfSpec = context.registerWfSpecIfNotPresent(workflow);

        // All we need to do is wait until the WfSpec is created. After that, LittleHorse
        // should guarantee read-your-own-writes. Any error returned by the API at this point
        // is considered a bug. Therefore, we do not need any awaitility await's() here.
        runWf(wfSpec, wfRunId, workflowArgs);
        TestExecutionContext pedro = new TestExecutionContext(wfRunId);

        for (Step step : steps) {
            step.execute(pedro, lhClient);
        }

        return wfRunId;
    }

    private WfRunId runWf(WfSpec wfSpec, WfRunId wfId, Collection<Arg> args) throws StatusRuntimeException {
        RunWfRequest.Builder req = RunWfRequest.newBuilder()
                .setWfSpecName(wfSpec.getId().getName())
                .setMajorVersion(wfSpec.getId().getMajorVersion())
                .setRevision(wfSpec.getId().getRevision())
                .setId(wfId.getId());

        if (wfId.hasParentWfRunId()) {
            req.setParentWfRunId(wfId.getParentWfRunId());
        }

        for (Arg arg : args) {
            try {
                req.putVariables(arg.name, LHLibUtil.objToVarVal(arg.value));
            } catch (LHSerdeException exn) {
                // This should NOT throw a StatusRuntimeException; rather, it should immediately fail the
                // test because retries will cause the same error over and over.
                throw new RuntimeException("Couldn't serialize workflow input: " + exn.getMessage(), exn);
            }
        }
        WfRun wfRun = lhClient.runWf(req.build());
        return wfRun.getId();
    }
}
