package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import com.google.protobuf.util.Timestamps;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.TaskWorkerGroupIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TaskWorkerGroup;
import io.littlehorse.sdk.common.proto.TaskWorkerMetadata;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.*;
import java.util.stream.Collectors;

public class TaskWorkerGroupModel extends Getable<TaskWorkerGroup> {

    public String taskDefName;
    public Date createdAt;
    public Map<String, TaskWorkerMetadataModel> taskWorkers = new HashMap<String, TaskWorkerMetadataModel>();

    public TaskWorkerGroupModel() {}

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
    public TaskWorkerGroupIdModel getObjectId() {
        return new TaskWorkerGroupIdModel(taskDefName);
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageType> tagStorageType
    ) {
        return List.of();
    }

    @Override
    public TaskWorkerGroup.Builder toProto() {
        return TaskWorkerGroup
            .newBuilder()
            .setTaskDefName(taskDefName)
            .setCreatedAt(Timestamps.fromDate(createdAt))
            .putAllTaskWorkers(taskWorkersToProto());
    }

    @Override
    public void initFrom(Message p) {
        TaskWorkerGroup proto = (TaskWorkerGroup) p;
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
                            TaskWorkerMetadataModel metadata = new TaskWorkerMetadataModel();
                            metadata.initFrom(entry.getValue());
                            return metadata;
                        }
                    )
                );
    }

    @Override
    public Class<TaskWorkerGroup> getProtoBaseClass() {
        return TaskWorkerGroup.class;
    }

    public Map<String, TaskWorkerMetadata> taskWorkersToProto() {
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
