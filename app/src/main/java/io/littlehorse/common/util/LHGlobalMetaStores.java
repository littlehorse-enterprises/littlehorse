package io.littlehorse.common.util;

import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class LHGlobalMetaStores {

  private ReadOnlyKeyValueStore<String, WfSpec> wfSpecStore;
  private ReadOnlyKeyValueStore<String, TaskDef> taskDefStore;

  public <T extends POSTable<?>> LHGlobalMetaStores(
    final ProcessorContext<String, T> ctx
  ) {
    wfSpecStore = ctx.getStateStore(POSTable.getGlobalStoreName(WfSpec.class));
    taskDefStore = ctx.getStateStore(POSTable.getGlobalStoreName(TaskDef.class));
  }

  public WfSpec getWfSpec(String id) {
    return wfSpecStore.get(id);
  }

  public TaskDef getTaskDef(String id) {
    return taskDefStore.get(id);
  }
}
