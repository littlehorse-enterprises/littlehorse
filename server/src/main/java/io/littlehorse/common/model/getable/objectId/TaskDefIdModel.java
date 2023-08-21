package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDefIdModel extends MetadataId<TaskDefId, TaskDef, TaskDefModel> {

    public String name;

    public TaskDefIdModel() {}

    public TaskDefIdModel(String name) {
        this.name = name;
    }

    @Override
    public Class<TaskDefId> getProtoBaseClass() {
        return TaskDefId.class;
    }

    @Override
    public void initFrom(Message proto) {
        TaskDefId p = (TaskDefId) proto;
        name = p.getName();
    }

    @Override
    public TaskDefId.Builder toProto() {
        TaskDefId.Builder out = TaskDefId.newBuilder().setName(name);
        return out;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void initFromString(String key) {
        name = key;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.TASK_DEF;
    }
}
