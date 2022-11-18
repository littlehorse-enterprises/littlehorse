package io.littlehorse.server.streamsbackend.coreserver;

import io.littlehorse.common.model.command.subcommand.ExternalEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.server.ServerTopology;
import io.littlehorse.server.oldprocessors.util.GenericOutput;
import io.littlehorse.server.oldprocessors.util.WfRunStoreAccess;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class CoreServerProcessorDao implements WfRunStoreAccess {

    public Map<String, NodeRun> nodeRunPuts;
    public Map<String, Variable> variablePuts;
    public Map<String, ExternalEvent> extEvtPuts;

    public CoreServerProcessorDao(final ProcessorContext<String, GenericOutput> ctx) {
        nodeRunPuts = new HashMap<>();
        variablePuts = new HashMap<>();
        extEvtPuts = new HashMap<>();

        KeyValueStore<String, Bytes> coreStore = ctx.getStateStore(
            ServerTopology.coreStore
        );
    }

    public void putNodeRun(NodeRun nr) {
        nodeRunPuts.put(nr.getObjectId(), nr);
    }

    public NodeRun getNodeRun(String wfRunId, int threadNum, int position) {
        String key = NodeRun.getStoreKey(wfRunId, threadNum, position);
        NodeRun out = nodeRunPuts.get(key);
        if (out == null) {
            out = nodeRunStore.get(key);

            // Little trick so that if it gets modified it is automatically saved
            if (out != null) nodeRunPuts.put(key, out);
        }
        return out;
    }

    public void putVariable(Variable var) {
        // TODO
    }

    public Variable getVariable(String wfRunId, String name, int threadNum) {
        // TODO
        return null;
    }

    public ExternalEvent getUnclaimedEvent(
        String wfRunId,
        String externalEventDefName
    ) {
        // TODO
        return null;
    }

    public ExternalEvent getExternalEvent(String externalEventId) {
        // TODO
        return null;
    }

    public void saveExternalEvent(ExternalEvent evt) {
        // TODO
    }

    public void scheduleTask(TaskScheduleRequest tsr) {
        // TODO
    }

    public void scheduleTimer(LHTimer timer) {
        // TODO
    }

    public void commitChanges() {
        // TODO
    }
}
