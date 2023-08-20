package io.littlehorse.common.dao;

import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;

public interface ReadOnlyMetadataStore {
    public WfSpecModel getWfSpec(String name, Integer version);

    public TaskDefModel getTaskDef(String name);

    public ExternalEventDefModel getExternalEventDef(String name);

    public UserTaskDefModel getUserTaskDef(String name, Integer version);
}
