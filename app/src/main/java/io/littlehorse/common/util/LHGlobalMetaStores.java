package io.littlehorse.common.util;

import io.littlehorse.common.model.GlobalPOSTable;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.server.oldprocessors.ApiStreamsContext;
import io.littlehorse.server.oldprocessors.util.GenericOutput;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class LHGlobalMetaStores {

    private ReadOnlyKeyValueStore<String, WfSpec> wfSpecStore;
    private ReadOnlyKeyValueStore<String, TaskDef> taskDefStore;
    private ReadOnlyKeyValueStore<String, ExternalEventDef> eedStore;

    public LHGlobalMetaStores(final ProcessorContext<String, GenericOutput> ctx) {
        wfSpecStore =
            ctx.getStateStore(GlobalPOSTable.getGlobalStoreName(WfSpec.class));
        taskDefStore =
            ctx.getStateStore(GlobalPOSTable.getGlobalStoreName(TaskDef.class));

        eedStore =
            ctx.getStateStore(
                GlobalPOSTable.getGlobalStoreName(ExternalEventDef.class)
            );
    }

    public LHGlobalMetaStores(ApiStreamsContext streams) {
        wfSpecStore = streams.getGlobalStore(WfSpec.class);
        taskDefStore = streams.getGlobalStore(TaskDef.class);
        eedStore = streams.getGlobalStore(ExternalEventDef.class);
    }

    public WfSpec getWfSpec(String id) {
        return wfSpecStore.get(id);
    }

    public TaskDef getTaskDef(String id) {
        return taskDefStore.get(id);
    }

    public ExternalEventDef getExternalEventDef(String id) {
        return eedStore.get(id);
    }
}
