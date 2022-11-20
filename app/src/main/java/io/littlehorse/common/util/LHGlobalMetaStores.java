package io.littlehorse.common.util;

import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;

public interface LHGlobalMetaStores {
    public WfSpec getWfSpec(String name, Integer version);

    public TaskDef getTaskDef(String name, Integer version);

    public ExternalEventDef getExternalEventDef(String name, Integer version);
}
