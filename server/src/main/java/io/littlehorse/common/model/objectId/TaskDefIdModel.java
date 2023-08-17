package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDefIdModel extends ObjectId<TaskDefId, TaskDef, TaskDefModel> {

    public String name;

    public TaskDefIdModel() {}

    public TaskDefIdModel(String name) {
        this.name = name;
    }

    public Class<TaskDefId> getProtoBaseClass() {
        return TaskDefId.class;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public void initFrom(Message proto) {
        TaskDefIdModel p = (TaskDefIdModel) proto;
        name = p.getName();
    }

    public TaskDefId.Builder toProto() {
        TaskDefId.Builder out = TaskDefId.newBuilder().setName(name);
        return out;
    }

    public String getStoreKey() {
        return name;
    }

    public void initFrom(String storeKey) {
        name = storeKey;
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.TASK_DEF;
    }
}
