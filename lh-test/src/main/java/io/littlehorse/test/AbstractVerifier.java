package io.littlehorse.test;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.internal.TestContext;
import io.littlehorse.test.internal.step.Step;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.awaitility.Awaitility;

public class AbstractVerifier implements Verifier {
    protected final LHPublicApiBlockingStub lhClient;
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
    public void start() {
        WfSpec wfSpec = context.registerWfSpecIfNotPresent(workflow);
        WfRunId wfRunId =
                WfRunId.newBuilder().setId(UUID.randomUUID().toString()).build();

        Awaitility.await()
                .ignoreException(StatusRuntimeException.class)
                .ignoreException(LHMisconfigurationException.class)
                .until(() -> !runWf(wfSpec, wfRunId.getId(), workflowArgs).isEmpty());

        WfRun wfRun = Awaitility.await()
                .ignoreException(StatusRuntimeException.class)
                .ignoreException(LHMisconfigurationException.class)
                .until(() -> lhClient.getWfRun(wfRunId), Objects::nonNull);
        for (Step step : steps) {
            step.execute(wfRun.getId(), lhClient);
        }
    }

    private String runWf(WfSpec wfSpec, String wfId, Collection<Arg> args) throws StatusRuntimeException {
        RunWfRequest.Builder req = RunWfRequest.newBuilder()
                .setWfSpecName(wfSpec.getName())
                .setId(wfId)
                .setWfSpecVersion(wfSpec.getVersion());

        for (Arg arg : args) {
            try {
                req.putVariables(arg.name, LHLibUtil.objToVarVal(arg.value));
            } catch (LHSerdeError exn) {
                throw new StatusRuntimeException(
                        Status.INVALID_ARGUMENT.withCause(exn).withDescription("Couldn't serialize workflow input"));
            }
        }
        WfRun wfRun = lhClient.runWf(req.build());
        return wfRun.getId();
    }
}
