package io.littlehorse.common.util;

import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskDef;

public interface LHGlobalMetaStores {
    public WfSpecModel getWfSpec(String name, Integer version);

    public TaskDef getTaskDef(String name);

    public ExternalEventDef getExternalEventDef(String name);

    public UserTaskDef getUserTaskDef(String name, Integer version);
}
