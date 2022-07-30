package io.littlehorse.common.model.meta;

import java.util.Date;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.TaskDefPb;
import io.littlehorse.common.proto.TaskDefPbOrBuilder;
import io.littlehorse.common.util.LHUtil;

public class TaskDef extends GETable<TaskDefPbOrBuilder> {
    public String name;
    public Date createdAt;

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getStoreKey() {
        return name;
    }

    public String getPartitionKey() {
        return name;
    }

    public Class<TaskDefPb> getProtoBaseClass() {
        return TaskDefPb.class;
    }

    public TaskDefPb.Builder toProto() {
        return TaskDefPb.newBuilder()
            .setName(name)
            .setCreatedAt(LHUtil.fromDate(createdAt));
    }

    public void initFrom(TaskDefPbOrBuilder proto) {
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
    }
}
