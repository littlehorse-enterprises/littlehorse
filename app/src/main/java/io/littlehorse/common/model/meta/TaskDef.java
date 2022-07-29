package io.littlehorse.common.model.meta;

import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.TaskDefPb;
import io.littlehorse.common.proto.TaskDefPbOrBuilder;

public class TaskDef extends GETable {
    public String name;

    public TaskDefPb.Builder toProtoBuilder() {
        return TaskDefPb.newBuilder().setName(name);
    }

    public static TaskDef fromProto(TaskDefPbOrBuilder proto) {
        TaskDef out = new TaskDef();
        out.name = proto.getName();
        return out;
    }
}
