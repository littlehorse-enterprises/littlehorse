package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefIdPb;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDefId extends ObjectId<TaskDefIdPb, TaskDef, TaskDefModel> {

    public String name;

    public TaskDefId() {}

    public TaskDefId(String name) {
        this.name = name;
    }

    public Class<TaskDefIdPb> getProtoBaseClass() {
        return TaskDefIdPb.class;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public void initFrom(Message proto) {
        TaskDefIdPb p = (TaskDefIdPb) proto;
        name = p.getName();
    }

    public TaskDefIdPb.Builder toProto() {
        TaskDefIdPb.Builder out = TaskDefIdPb.newBuilder().setName(name);
        return out;
    }

    public String getStoreKey() {
        return name;
    }

    public void initFrom(String storeKey) {
        name = storeKey;
    }

    public GetableClassEnumPb getType() {
        return GetableClassEnumPb.TASK_DEF;
    }
}
