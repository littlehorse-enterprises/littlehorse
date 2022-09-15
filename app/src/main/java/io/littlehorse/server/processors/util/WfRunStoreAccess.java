package io.littlehorse.server.processors.util;

import io.littlehorse.common.model.wfrun.TaskRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.VariableValue;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.streams.state.KeyValueStore;

public class WfRunStoreAccess {

    public KeyValueStore<String, TaskRun> taskRunStore;
    public KeyValueStore<String, Variable> variableStore;
    public Map<String, TaskRun> taskPuts;
    public Map<String, Variable> variablePuts;

    private String wfRunId;

    public WfRunStoreAccess(
        KeyValueStore<String, TaskRun> taskRunStore,
        KeyValueStore<String, Variable> variableStore,
        String wfRunId
    ) {
        this.taskRunStore = taskRunStore;
        this.variableStore = variableStore;

        taskPuts = new HashMap<>();
        variablePuts = new HashMap<>();
        this.wfRunId = wfRunId;
    }

    public void putTask(TaskRun task) {
        taskPuts.put(task.getObjectId(), task);
    }

    public TaskRun getTaskRun(int threadNum, int position) {
        String key = TaskRun.getStoreKey(wfRunId, threadNum, position);
        TaskRun out = taskPuts.get(key);
        if (out == null) {
            out = taskRunStore.get(key);
        }
        return out;
    }

    public void putVariable(String name, VariableValue val, int threadNum) {
        Variable var = new Variable();
        var.value = val;
        var.wfRunId = wfRunId;
        var.date = new Date();
        var.name = name;

        variablePuts.put(var.getObjectId(), var);
    }

    public VariableValue getVariable(String name, int threadNum) {
        String key = Variable.getObjectId(wfRunId, threadNum, name);
        Variable out = variablePuts.get(key);
        if (out == null) {
            out = variableStore.get(key);
        }
        return out.value;
    }
}
