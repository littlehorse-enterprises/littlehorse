package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.WfSpec.ParentWfSpecReference;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.TaskDefBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class WorkflowImpl extends Workflow {

    private PutWfSpecRequest compiledWorkflow;
    private Map<String, TaskDefBuilder> taskDefBuilders;
    private Set<String> requiredTaskDefNames;
    private Set<String> requiredEedNames;
    private Set<String> requiredWorkflowEventDefNames;

    public WorkflowImpl(String name, ThreadFunc entrypointThreadFunc) {
        super(name, entrypointThreadFunc);
        this.compiledWorkflow = null;
        this.taskDefBuilders = new HashMap<>();
        this.requiredTaskDefNames = new HashSet<>();
        this.requiredWorkflowEventDefNames = new HashSet<>();
        this.requiredEedNames = new HashSet<>();
    }

    public PutWfSpecRequest compileWorkflow(LittleHorseBlockingStub client) {
        if (compiledWorkflow == null) {
            compiledWorkflow = compileWorkflowHelper(client);
        }
        return compiledWorkflow;
    }

    void addTaskDefName(String taskDefName) {
        requiredTaskDefNames.add(taskDefName);
    }

    void addExternalEventDefName(String eedName) {
        requiredEedNames.add(eedName);
    }

    void addWorkflowEventDefName(String name) {
        requiredWorkflowEventDefNames.add(name);
    }

    void addTaskDefBuilder(TaskDefBuilder tdb) {
        TaskDefBuilder previous = taskDefBuilders.get(tdb.getTaskDefName());
        if (previous != null) {
            if (!previous.signature.equals(tdb.signature)) {
                throw new RuntimeException("Tried to register two DIFFERENT tasks named " + tdb.getTaskDefName());
            }
        } else {
            taskDefBuilders.put(tdb.getTaskDefName(), tdb);
        }
    }

    @Override
    public Set<String> getRequiredTaskDefNames(LittleHorseBlockingStub client) {
        if (compiledWorkflow == null) {
            compiledWorkflow = compileWorkflowHelper(client);
        }
        return requiredTaskDefNames;
    }

    @Override
    public Set<String> getRequiredExternalEventDefNames(LittleHorseBlockingStub client) {
        if (compiledWorkflow == null) {
            compiledWorkflow = compileWorkflowHelper(client);
        }
        return requiredEedNames;
    }

    @Override
    public Set<String> getRequiredWorkflowEventDefNames(LittleHorseBlockingStub client) {
        if (compiledWorkflow == null) {
            compiledWorkflow = compileWorkflowHelper(client);
        }
        return requiredWorkflowEventDefNames;
    }

    private PutWfSpecRequest compileWorkflowHelper(LittleHorseBlockingStub client) {
        String entrypointThreadName = this.addSubThread("entrypoint", entrypointThread);
        spec.setEntrypointThreadName(entrypointThreadName);

        while (!threadFuncs.isEmpty()) {
            Pair<String, ThreadFunc> nextFunc = threadFuncs.remove();
            ThreadFunc threadObj = nextFunc.getValue();
            String funcName = nextFunc.getKey();
            WorkflowThreadImpl thr = new WorkflowThreadImpl(name, this, threadObj);
            spec.putThreadSpecs(funcName, thr.getSpec().build());
        }

        if (wfRetentionPolicy != null) {
            spec.setRetentionPolicy(wfRetentionPolicy);
        }
        if (parentWfSpecName != null) {
            spec.setParentWfSpec(ParentWfSpecReference.newBuilder().setWfSpecName(parentWfSpecName));
        }

        return spec.build();
    }

    ThreadRetentionPolicy getDefaultThreadRetentionPolicy() {
        return defaultThreadRetentionPolicy;
    }

    public String addSubThread(String subThreadName, ThreadFunc subThreadFunc) {
        for (Pair<String, ThreadFunc> pair : threadFuncs) {
            if (pair.getKey().equals(subThreadName)) {
                throw new LHMisconfigurationException(String.format("Thread %s already exists", subThreadName));
            }
        }
        threadFuncs.add(Pair.of(subThreadName, subThreadFunc));
        return subThreadName;
    }

    protected Optional<ExponentialBackoffRetryPolicy> getDefaultExponentialBackoffRetryPolicy() {
        return Optional.ofNullable(defaultExponentialBackoff);
    }

    protected int getDefaultSimpleRetries() {
        return defaultSimpleRetries;
    }
}
