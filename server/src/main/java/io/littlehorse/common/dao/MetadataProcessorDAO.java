package io.littlehorse.common.dao;

import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;

public interface MetadataProcessorDAO extends ReadOnlyMetadataStore {

    public void putWfSpec(WfSpecModel spec);

    public void putTaskDef(TaskDefModel spec);

    public void putUserTaskDef(UserTaskDefModel spec);

    public void putExternalEventDef(ExternalEventDefModel eed);

    public DeleteObjectReply deleteTaskDef(String name);

    public DeleteObjectReply deleteExternalEventDef(String name);

    public DeleteObjectReply deleteWfSpec(String name, int version);

    public DeleteObjectReply deleteUserTaskDef(String name, int version);
}
