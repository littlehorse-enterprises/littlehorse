package io.littlehorse.common.model.meta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.proto.TaskDefPb;
import io.littlehorse.common.proto.TaskDefPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.model.internal.IndexEntry;

public class TaskDef extends POSTable<TaskDefPbOrBuilder> {
    public String name;
    public Date createdAt;
    public long lastOffset;

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
            .setCreatedAt(LHUtil.fromDate(createdAt))
            .setLastUpdatedOffset(lastOffset);
    }

    public void initFrom(MessageOrBuilder p) {
        TaskDefPbOrBuilder proto = (TaskDefPbOrBuilder) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        lastOffset = proto.getLastUpdatedOffset();
    }

    public long getLastUpdatedOffset() {
        return lastOffset;
    }

    public void setLastUpdatedOffset(long newOffset) {
        lastOffset = newOffset;
    }

    public void handlePost(POSTable<TaskDefPbOrBuilder> old) {
        // if (!(old == null || old instanceof TaskDef)) {
        //     throw new RuntimeException("Bad method call.");
        // }
        // TaskDef oldTd = old == null ? null : (TaskDef) old;
    }

    public boolean handleDelete() {
        return true;
    }

    public List<IndexEntry> getIndexEntries() {
        return new ArrayList<>();
    }
}
