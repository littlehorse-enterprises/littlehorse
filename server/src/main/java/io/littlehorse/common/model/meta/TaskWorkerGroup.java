package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import com.google.protobuf.util.Timestamps;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.TaskWorkerGroupId;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TaskWorkerGroupPb;
import io.littlehorse.sdk.common.proto.TaskWorkerMetadataPb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.*;
import java.util.stream.Collectors;

public class TaskWorkerGroup extends Getable<TaskWorkerGroupPb> {

    public String taskDefName;
    public Date createdAt;
    public Map<String, TaskWorkerMetadata> taskWorkers = new HashMap<String, TaskWorkerMetadata>();

    public TaskWorkerGroup() {}

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public TaskWorkerGroupId getObjectId() {
        return new TaskWorkerGroupId(taskDefName);
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageTypePb> tagStorageTypePb
    ) {
        return List.of();
    }

    @Override
    public TaskWorkerGroupPb.Builder toProto() {
        return TaskWorkerGroupPb
            .newBuilder()
            .setTaskDefName(taskDefName)
            .setCreatedAt(Timestamps.fromDate(createdAt))
            .putAllTaskWorkers(taskWorkersToProto());
    }

    @Override
    public void initFrom(Message p) {
        TaskWorkerGroupPb proto = (TaskWorkerGroupPb) p;
        taskDefName = proto.getTaskDefName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        taskWorkers =
            proto
                .getTaskWorkersMap()
                .entrySet()
                .stream()
                .collect(
                    Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> {
                            TaskWorkerMetadata metadata = new TaskWorkerMetadata();
                            metadata.initFrom(entry.getValue());
                            return metadata;
                        }
                    )
                );
    }

    @Override
    public Class<TaskWorkerGroupPb> getProtoBaseClass() {
        return TaskWorkerGroupPb.class;
    }

    public Map<String, TaskWorkerMetadataPb> taskWorkersToProto() {
        return taskWorkers
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey(),
                    entry -> entry.getValue().toProto().build()
                )
            );
    }
}
