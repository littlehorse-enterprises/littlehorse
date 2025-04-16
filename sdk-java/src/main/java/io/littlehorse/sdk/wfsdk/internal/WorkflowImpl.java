package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.WfSpec.ParentWfSpecReference;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.TaskDefBuilder;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

public class WorkflowImpl extends Workflow {

    private PutWfSpecRequest compiledWorkflow;
    private Map<String, TaskDefBuilder> taskDefBuilders;
    private Set<String> requiredTaskDefNames;
    private Set<String> requiredEedNames;
    private Set<String> requiredWorkflowEventDefNames;
    private Stack<WorkflowThreadImpl>  threads;

    public WorkflowImpl(String name, ThreadFunc entrypointThreadFunc) {
        super(name, entrypointThreadFunc);
        this.compiledWorkflow = null;
        this.taskDefBuilders = new HashMap<>();
        this.requiredTaskDefNames = new HashSet<>();
        this.requiredWorkflowEventDefNames = new HashSet<>();
        this.requiredEedNames = new HashSet<>();
        this.threads = new Stack<>();
    }

    public Set<PutTaskDefRequest> compileTaskDefs() {
        compileWorkflow();
        Set<PutTaskDefRequest> out = new HashSet<>();
        for (TaskDefBuilder tdb : taskDefBuilders.values()) {
            out.add(tdb.toPutTaskDefRequest());
        }
        return out;
    }

    public PutWfSpecRequest compileWorkflow() {
        if (compiledWorkflow == null) {
            compiledWorkflow = compileWorkflowHelper();
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
    public Set<String> getRequiredTaskDefNames() {
        if (compiledWorkflow == null) {
            compiledWorkflow = compileWorkflowHelper();
        }
        return requiredTaskDefNames;
    }

    @Override
    public Set<String> getRequiredExternalEventDefNames() {
        if (compiledWorkflow == null) {
            compiledWorkflow = compileWorkflowHelper();
        }
        return requiredEedNames;
    }

    @Override
    public Set<String> getRequiredWorkflowEventDefNames() {
        if (compiledWorkflow == null) {
            compiledWorkflow = compileWorkflowHelper();
        }
        return requiredWorkflowEventDefNames;
    }

    private PutWfSpecRequest compileWorkflowHelper() {
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

    public Set<Object> getTaskExecutables() {
        compileWorkflow();
        Set<Object> out = new HashSet<>();
        for (TaskDefBuilder tdb : taskDefBuilders.values()) {
            out.add(tdb.executable);
        }
        return out;
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

    Stack<WorkflowThreadImpl> getThreads() {
        return this.threads;
    }
}
