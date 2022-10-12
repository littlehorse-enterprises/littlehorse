package io.littlehorse.server.processors.util;

import io.littlehorse.common.model.event.ExternalEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.VariableValue;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

public class WfRunStoreAccess {

    public KeyValueStore<String, NodeRun> nodeRunStore;
    public KeyValueStore<String, Variable> variableStore;
    public KeyValueStore<String, ExternalEvent> extEvtStore;
    public Map<String, NodeRun> nodeRunPuts;
    public Map<String, Variable> variablePuts;
    public Map<String, ExternalEvent> extEvtPuts;

    public List<TaskScheduleRequest> tasksToSchedule;
    public List<LHTimer> timersToSchedule;

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

        this.tasksToSchedule = new ArrayList<>();
        this.timersToSchedule = new ArrayList<>();
    }

    public void putNodeRun(NodeRun nr) {
        nodeRunPuts.put(nr.getObjectId(), nr);
    }

    public NodeRun getNodeRun(int threadNum, int position) {
        String key = NodeRun.getStoreKey(wfRunId, threadNum, position);
        NodeRun out = nodeRunPuts.get(key);
        if (out == null) {
            out = nodeRunStore.get(key);

            // Little trick so that if it gets modified it is automatically saved
            if (out != null) nodeRunPuts.put(key, out);
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

    public ExternalEvent getUnclaimedEvent(String externalEventDefName) {
        // Need to load all of them and then get the least recent that hasn't been
        // claimed yet.
        String prefix = ExternalEvent.getStorePrefix(wfRunId, externalEventDefName);

        // TODO: This is O(N) for number of events correlated with the WfRun.
        // Generally that will only be a small number, but there could be weird
        // use-cases where this could take a long time (if there's 1000 events or
        // so then it could take seconds, which holds up the entire scheduling).
        ExternalEvent out = null;
        try (
            KeyValueIterator<String, ExternalEvent> iter = extEvtStore.prefixScan(
                prefix,
                Serdes.String().serializer()
            )
        ) {
            while (iter.hasNext()) {
                KeyValue<String, ExternalEvent> kvp = iter.next();
                ExternalEvent candidate;
                if (extEvtPuts.containsKey(kvp.key)) {
                    candidate = extEvtPuts.get(kvp.key);
                } else {
                    candidate = kvp.value;
                    extEvtPuts.put(kvp.key, candidate); // TODO: Is this necessary?
                }

                if (candidate.claimed) {
                    continue;
                }

                if (
                    out == null ||
                    out.getCreatedAt().getTime() > candidate.getCreatedAt().getTime()
                ) {
                    out = candidate;
                }
            }
        }

        return out;
    }

    public void saveExternalEvent(ExternalEvent evt) {
        extEvtPuts.put(evt.getObjectId(), evt);
    }

    public void scheduleTask(TaskScheduleRequest tsr) {
        tasksToSchedule.add(tsr);
    }

    public void scheduleTimer(LHTimer timer) {
        timersToSchedule.add(timer);
    }
}
