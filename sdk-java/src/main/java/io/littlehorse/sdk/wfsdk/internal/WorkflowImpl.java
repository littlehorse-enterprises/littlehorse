package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.TaskDefBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class WorkflowImpl extends Workflow {

    private PutWfSpecRequest compiledWorkflow;
    private Map<String, TaskDefBuilder> taskDefBuilders;
    private Set<String> requiredTaskDefNames;
    private Set<String> requiredEedNames;

    public WorkflowImpl(String name, ThreadFunc entrypointThreadFunc) {
        super(name, entrypointThreadFunc);
        compiledWorkflow = null;
        taskDefBuilders = new HashMap<>();
        requiredTaskDefNames = new HashSet<>();
        requiredEedNames = new HashSet<>();
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

    public void addTaskDefName(String taskDefName) {
        requiredTaskDefNames.add(taskDefName);
    }

    public void addExternalEventDefName(String eedName) {
        requiredEedNames.add(eedName);
    }

    public void addTaskDefBuilder(TaskDefBuilder tdb) {
        TaskDefBuilder previous = taskDefBuilders.get(tdb.taskDefName);
        if (previous != null) {
            if (!previous.signature.equals(tdb.signature)) {
                throw new RuntimeException("Tried to register two DIFFERENT tasks named " + tdb.taskDefName);
            }
        } else {
            taskDefBuilders.put(tdb.taskDefName, tdb);
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

    private PutWfSpecRequest compileWorkflowHelper() {
        String entrypointThreadName = this.addSubThread("entrypoint", entrypointThread);
        spec.setEntrypointThreadName(entrypointThreadName);

        while (!threadFuncs.isEmpty()) {
            Pair<String, ThreadFunc> nextFunc = threadFuncs.remove();
            ThreadFunc threadObj = nextFunc.getValue();
            String funcName = nextFunc.getKey();
            ThreadBuilderImpl thr = new ThreadBuilderImpl(name, this, threadObj);
            spec.putThreadSpecs(funcName, thr.getSpec().build());
        }

        return spec.build();
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
}
