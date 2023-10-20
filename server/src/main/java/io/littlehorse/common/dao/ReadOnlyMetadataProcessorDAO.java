package io.littlehorse.common.dao;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;

public interface ReadOnlyMetadataProcessorDAO {

    UserTaskDefModel getUserTaskDef(String name, Integer version);

    ExternalEventDefModel getExternalEventDef(String name);

    TaskDefModel getTaskDef(String name);

    WfSpecModel getWfSpec(String name, Integer version);

    <U extends Message, T extends AbstractGetable<U>> T get(ObjectIdModel<?, U, T> id);
}
