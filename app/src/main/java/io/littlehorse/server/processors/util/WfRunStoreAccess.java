package io.littlehorse.server.processors.util;

import io.littlehorse.common.model.event.ExternalEvent;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.model.wfrun.noderun.NodeRun;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.streams.state.KeyValueStore;

public class WfRunStoreAccess {

    public KeyValueStore<String, NodeRun> nodeRunStore;
    public KeyValueStore<String, Variable> variableStore;
    public KeyValueStore<String, ExternalEvent> extEvtStore;
    public Map<String, NodeRun> nodeRunPuts;
    public Map<String, Variable> variablePuts;
    public Map<String, ExternalEvent> extEvtPuts;

    private String wfRunId;

    public WfRunStoreAccess(
        KeyValueStore<String, NodeRun> taskRunStore,
        KeyValueStore<String, Variable> variableStore,
        KeyValueStore<String, ExternalEvent> extEvtStore,
        String wfRunId
    ) {
        this.nodeRunStore = taskRunStore;
        this.variableStore = variableStore;
        this.extEvtStore = extEvtStore;

        nodeRunPuts = new HashMap<>();
        variablePuts = new HashMap<>();
        extEvtPuts = new HashMap<>();
        this.wfRunId = wfRunId;
    }

    public void putNodeRun(NodeRun nr) {
        nodeRunPuts.put(nr.getObjectId(), nr);
    }

    public NodeRun getNodeRun(int threadNum, int position) {
        String key = NodeRun.getStoreKey(wfRunId, threadNum, position);
        NodeRun out = nodeRunPuts.get(key);
        if (out == null) {
            out = nodeRunStore.get(key);
        }
        return out;
    }

    public void putVariable(String name, VariableValue val, int threadNum) {
        Variable var = new Variable();
        var.value = val;
        var.wfRunId = wfRunId;
        var.date = new Date();
        var.name = name;

        putVariable(var);
    }

    public void putVariable(Variable var) {
        variablePuts.put(var.getObjectId(), var);
    }

    public Variable getVariable(String name, int threadNum) {
        String key = Variable.getObjectId(wfRunId, threadNum, name);
        Variable out = variablePuts.get(key);
        if (out == null) {
            out = variableStore.get(key);
        }
        return out;
    }
}
