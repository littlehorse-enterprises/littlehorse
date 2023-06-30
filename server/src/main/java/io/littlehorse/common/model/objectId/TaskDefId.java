package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.jlib.common.proto.TaskDefIdPb;
import io.littlehorse.jlib.common.proto.TaskDefPb;

// Used by TaskDef, TaskDef, and ExternalEventDef
public class TaskDefId extends ObjectId<TaskDefIdPb, TaskDefPb, TaskDef> {

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
