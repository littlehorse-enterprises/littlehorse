package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.InlineWfSpec;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.RunInlineWfRequest;
import io.littlehorse.sdk.common.proto.WorkflowRetentionPolicy;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import java.util.Map;

/** Builds a one-off workflow run from the same thread API used by registered workflows. */
public final class InlineWorkflow {
    private final WorkflowImpl workflow;
    private WorkflowRetentionPolicy retentionPolicy;
    private String wfRunId;

    InlineWorkflow(ThreadFunc entrypointThreadFunc) {
        this(entrypointThreadFunc, Map.of());
    }

    InlineWorkflow(ThreadFunc entrypointThreadFunc, Map<String, String> placeholderValues) {
        this.workflow = new WorkflowImpl("inline", entrypointThreadFunc, placeholderValues);
    }

    /** Sets how long the resulting WfRun is retained after it terminates. */
    public InlineWorkflow withRetentionPolicy(WorkflowRetentionPolicy policy) {
        this.retentionPolicy = policy;
        return this;
    }

    /** Sets the ID of the resulting WfRun. */
    public InlineWorkflow withWfRunId(String id) {
        this.wfRunId = id;
        return this;
    }

    /** Compiles this workflow into a request for the RunInlineWf RPC. */
    public RunInlineWfRequest compileWorkflow() {
        return toRunRequest(workflow.compileWorkflow());
    }

    /** Compiles this workflow using the type adapters configured in {@code config}. */
    public RunInlineWfRequest compileWorkflow(LHConfig config) {
        return toRunRequest(workflow.compileWorkflow(config));
    }

    private RunInlineWfRequest toRunRequest(PutWfSpecRequest compiled) {
        InlineWfSpec.Builder spec = InlineWfSpec.newBuilder()
                .putAllThreadSpecs(compiled.getThreadSpecsMap())
                .setEntrypointThreadName(compiled.getEntrypointThreadName());
        if (retentionPolicy != null) {
            spec.setRetentionPolicy(retentionPolicy);
        }

        RunInlineWfRequest.Builder request = RunInlineWfRequest.newBuilder().setWfSpec(spec);
        if (wfRunId != null) {
            request.setId(wfRunId);
        }
        return request.build();
    }
}
