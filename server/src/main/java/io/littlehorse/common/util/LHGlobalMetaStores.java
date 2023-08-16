package io.littlehorse.common.util;

import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;

public interface LHGlobalMetaStores {
    public WfSpecModel getWfSpec(String name, Integer version);

    public TaskDefModel getTaskDef(String name);

    public ExternalEventDefModel getExternalEventDef(String name);

    public UserTaskDefModel getUserTaskDef(String name, Integer version);
}
