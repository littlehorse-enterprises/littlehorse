package io.littlehorse.common.util;

import io.littlehorse.common.model.GlobalPOSTable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.server.ApiStreamsContext;
import io.littlehorse.server.processors.util.GenericOutput;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class LHGlobalMetaStores {

    private ReadOnlyKeyValueStore<String, WfSpec> wfSpecStore;
    private ReadOnlyKeyValueStore<String, TaskDef> taskDefStore;

    public LHGlobalMetaStores(final ProcessorContext<String, GenericOutput> ctx) {
        wfSpecStore =
            ctx.getStateStore(GlobalPOSTable.getGlobalStoreName(WfSpec.class));
        taskDefStore =
            ctx.getStateStore(GlobalPOSTable.getGlobalStoreName(TaskDef.class));
    }

    public LHGlobalMetaStores(ApiStreamsContext streams) {
        wfSpecStore = streams.getGlobalStore(WfSpec.class);
        taskDefStore = streams.getGlobalStore(TaskDef.class);
    }

    public WfSpec getWfSpec(String id) {
        return wfSpecStore.get(id);
    }

    public TaskDef getTaskDef(String id) {
        return taskDefStore.get(id);
    }
}
