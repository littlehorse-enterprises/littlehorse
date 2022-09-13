package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.proto.TaskDefPb;
import io.littlehorse.common.proto.TaskDefPbOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class TaskDef extends POSTable<TaskDefPbOrBuilder> {

    public String name;
    public Date createdAt;

    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    public String getObjectId() {
        return name;
    }

    public String getPartitionKey() {
        return name;
    }

    public Class<TaskDefPb> getProtoBaseClass() {
        return TaskDefPb.class;
    }

    public TaskDefPb.Builder toProto() {
        return TaskDefPb
            .newBuilder()
            .setName(name)
            .setCreatedAt(LHUtil.fromDate(getCreatedAt()));
    }

    public void initFrom(MessageOrBuilder p) {
        TaskDefPbOrBuilder proto = (TaskDefPbOrBuilder) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
    }

    public void handlePost(
        POSTable<TaskDefPbOrBuilder> old,
        LHGlobalMetaStores c,
        LHConfig config
    ) throws LHValidationError {
        if (!(old == null || old instanceof TaskDef)) {
            throw new RuntimeException("Bad method call.");
        }
        TaskDef oldTd = old == null ? null : (TaskDef) old;
        if (oldTd != null) {
            throw new LHValidationError(
                null,
                "Conflict: Cannot mutate taskdef"
            );
        }
    }

    public boolean handleDelete() {
        return true;
    }

    public List<Tag> getTags() {
        return Arrays.asList(new Tag(this, Pair.of("name", name)));
    }
}
