package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.common.model.meta.TaskWorkerGroup;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.metrics.TaskDefMetricsModel;
import io.littlehorse.common.model.metrics.WfSpecMetricsModel;
import io.littlehorse.common.model.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.objectId.ExternalEventId;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.model.objectId.TaskDefId;
import io.littlehorse.common.model.objectId.TaskDefMetricsIdModel;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.model.objectId.TaskWorkerGroupId;
import io.littlehorse.common.model.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.model.objectId.WfRunId;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.model.objectId.WfSpecMetricsIdModel;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
public abstract class Getable<T extends Message> extends Storeable<T> {

    public abstract Date getCreatedAt();

    // This is here for convenience. It's to be set by the LHDAOImpl, but only
    // when doing processing (not interactive queries).
    private LHDAO dao;

    public String getPartitionKey() {
        return getObjectId().getPartitionKey();
    }

    public static GetableClassEnumPb getTypeEnum(Class<? extends Getable<?>> cls) {
        if (cls.equals(WfRunModel.class)) {
            return GetableClassEnumPb.WF_RUN;
        } else if (cls.equals(NodeRunModel.class)) {
            return GetableClassEnumPb.NODE_RUN;
        } else if (cls.equals(WfSpecModel.class)) {
            return GetableClassEnumPb.WF_SPEC;
        } else if (cls.equals(TaskDefModel.class)) {
            return GetableClassEnumPb.TASK_DEF;
        } else if (cls.equals(Variable.class)) {
            return GetableClassEnumPb.VARIABLE;
        } else if (cls.equals(ExternalEventDefModel.class)) {
            return GetableClassEnumPb.EXTERNAL_EVENT_DEF;
        } else if (cls.equals(ExternalEvent.class)) {
            return GetableClassEnumPb.EXTERNAL_EVENT;
        } else if (cls.equals(TaskDefMetricsModel.class)) {
            return GetableClassEnumPb.TASK_DEF_METRICS;
        } else if (cls.equals(WfSpecMetricsModel.class)) {
            return GetableClassEnumPb.WF_SPEC_METRICS;
        } else if (cls.equals(TaskWorkerGroup.class)) {
            return GetableClassEnumPb.TASK_WORKER_GROUP;
        } else if (cls.equals(UserTaskDefModel.class)) {
            return GetableClassEnumPb.USER_TASK_DEF;
        } else if (cls.equals(TaskRun.class)) {
            return GetableClassEnumPb.TASK_RUN;
        } else if (cls.equals(UserTaskRun.class)) {
            return GetableClassEnumPb.USER_TASK_RUN;
        } else {
            throw new IllegalArgumentException(
                "Uh oh, unrecognized: " + cls.getName()
            );
        }
    }

    public static Class<? extends Getable<?>> getCls(GetableClassEnumPb type) {
        switch (type) {
            case WF_RUN:
                return WfRunModel.class;
            case NODE_RUN:
                return NodeRunModel.class;
            case WF_SPEC:
                return WfSpecModel.class;
            case TASK_DEF:
                return TaskDefModel.class;
            case VARIABLE:
                return Variable.class;
            case EXTERNAL_EVENT_DEF:
                return ExternalEventDefModel.class;
            case EXTERNAL_EVENT:
                return ExternalEvent.class;
            case TASK_DEF_METRICS:
                return TaskDefMetricsModel.class;
            case WF_SPEC_METRICS:
                return WfSpecMetricsModel.class;
            case TASK_WORKER_GROUP:
                return TaskWorkerGroup.class;
            case USER_TASK_DEF:
                return UserTaskDefModel.class;
            case TASK_RUN:
                return TaskRun.class;
            case USER_TASK_RUN:
                return UserTaskRun.class;
            case UNRECOGNIZED:
            // default:
        }
        throw new IllegalArgumentException(
            "Unrecognized/unimplemented GetableClassEnumPb"
        );
    }

    public static Class<? extends ObjectId<?, ?, ?>> getIdCls(
        GetableClassEnumPb type
    ) {
        switch (type) {
            case WF_RUN:
                return WfRunId.class;
            case NODE_RUN:
                return NodeRunId.class;
            case WF_SPEC:
                return WfSpecId.class;
            case TASK_DEF:
                return TaskDefId.class;
            case VARIABLE:
                return VariableId.class;
            case EXTERNAL_EVENT_DEF:
                return ExternalEventDefIdModel.class;
            case EXTERNAL_EVENT:
                return ExternalEventId.class;
            case TASK_DEF_METRICS:
                return TaskDefMetricsIdModel.class;
            case WF_SPEC_METRICS:
                return WfSpecMetricsIdModel.class;
            case TASK_WORKER_GROUP:
                return TaskWorkerGroupId.class;
            case USER_TASK_DEF:
                return UserTaskDefIdModel.class;
            case TASK_RUN:
                return TaskRunId.class;
            case USER_TASK_RUN:
                return UserTaskRunId.class;
            case UNRECOGNIZED:
        }
        throw new IllegalArgumentException(
            "Unrecognized/unimplemented GetableClassEnumPb"
        );
    }

    public abstract List<GetableIndex<? extends Getable<?>>> getIndexConfigurations();

    public abstract ObjectId<?, T, ?> getObjectId();

    public String getStoreKey() {
        return getObjectId().getStoreKey();
    }

    public abstract List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageTypePb> tagStorageTypePb
    );

    public List<Tag> getIndexEntries() {
        List<Tag> out = new ArrayList<>();
        for (GetableIndex<? extends Getable<?>> indexConfiguration : this.getIndexConfigurations()) {
            if (!indexConfiguration.isValid(this)) {
                continue;
            }
            Optional<TagStorageTypePb> tagStorageTypePb = indexConfiguration.getTagStorageTypePb();
            List<IndexedField> singleIndexedValues = indexConfiguration
                .getAttributes()
                .stream()
                .filter(stringValueTypePair -> {
                    return stringValueTypePair
                        .getValue()
                        .equals(GetableIndex.ValueType.SINGLE);
                })
                .map(stringValueTypePair -> {
                    return this.getIndexValues(
                            stringValueTypePair.getKey(),
                            tagStorageTypePb
                        )
                        .get(0);
                })
                .toList();
            List<IndexedField> dynamicIndexedFields = indexConfiguration
                .getAttributes()
                .stream()
                .filter(stringValueTypePair -> {
                    return stringValueTypePair
                        .getValue()
                        .equals(GetableIndex.ValueType.DYNAMIC);
                })
                .flatMap(stringValueTypePair ->
                    this.getIndexValues(
                            stringValueTypePair.getKey(),
                            tagStorageTypePb
                        )
                        .stream()
                )
                .toList();
            List<List<IndexedField>> combine = combine(
                singleIndexedValues,
                dynamicIndexedFields
            );
            for (List<IndexedField> list : combine) {
                TagStorageTypePb storageType = list
                    .stream()
                    .map(IndexedField::getTagStorageTypePb)
                    .filter(tagStorageTypePb1 ->
                        tagStorageTypePb1 == TagStorageTypePb.REMOTE
                    )
                    .findAny()
                    .orElse(TagStorageTypePb.LOCAL);
                List<Pair<String, String>> pairs = list
                    .stream()
                    .map(indexedField ->
                        Pair.of(
                            indexedField.getKey(),
                            indexedField.getValue().toString()
                        )
                    )
                    .toList();
                out.add(new Tag(this, storageType, pairs));
            }
        }
        return out;
    }

    private List<List<IndexedField>> combine(
        List<IndexedField> source,
        List<IndexedField> multiple
    ) {
        if (multiple.isEmpty()) {
            return List.of(source);
        }
        List<List<IndexedField>> result = new ArrayList<>();
        for (IndexedField dynamicIndexedField : multiple) {
            List<IndexedField> list = Stream
                .concat(source.stream(), Stream.of(dynamicIndexedField))
                .toList();
            result.add(list);
        }
        return result;
    }
}
/*
 * Some random thoughts:
 * - each GETable has a partition key and an ID. They may be different.
 * - For example, we want TaskRun's for a WfRun to end up on the same host
 * - VariableValue's for a ThreadRun will end up on the same node as each other
 *
 * Will we make it possible to deploy the Scheduler separately from the API?
 *   - currently no. It would double the storage costs.
 */
