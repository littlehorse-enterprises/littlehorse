package io.littlehorse.common.model.meta;

import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.TaskDefPb;
import io.littlehorse.common.proto.TaskDefPbOrBuilder;

public class TaskDef extends GETable<TaskDefPbOrBuilder> {
    public String name;

    public Class<TaskDefPb> getProtoBaseClass() {
        return TaskDefPb.class;
    }

    public TaskDefPb.Builder toProto() {
        return TaskDefPb.newBuilder().setName(name);
    }

    public void initFrom(TaskDefPbOrBuilder proto) {
        name = proto.getName();
    }
}
