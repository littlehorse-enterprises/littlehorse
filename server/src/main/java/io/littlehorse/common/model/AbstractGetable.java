package io.littlehorse.common.model;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.getable.ObjectId;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefMetricsIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TaskWorkerGroupIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecMetricsIdModel;
import io.littlehorse.common.model.getable.repartitioned.taskmetrics.TaskDefMetricsModel;
import io.littlehorse.common.model.getable.repartitioned.workflowmetrics.WfSpecMetricsModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
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
public abstract class AbstractGetable<T extends Message> extends LHSerializable<T> {

    public abstract Date getCreatedAt();

    // This is here for convenience. It's to be set by the LHDAOImpl, but only
    // when doing processing (not interactive queries).
    private CoreProcessorDAO dao;

    public String getPartitionKey() {
        return getObjectId().getPartitionKey();
    }

    public static GetableClassEnum getTypeEnum(Class<? extends AbstractGetable<?>> cls) {
        if (cls.equals(WfRunModel.class)) {
            return GetableClassEnum.WF_RUN;
        } else if (cls.equals(NodeRunModel.class)) {
            return GetableClassEnum.NODE_RUN;
        } else if (cls.equals(WfSpecModel.class)) {
            return GetableClassEnum.WF_SPEC;
        } else if (cls.equals(TaskDefModel.class)) {
            return GetableClassEnum.TASK_DEF;
        } else if (cls.equals(VariableModel.class)) {
            return GetableClassEnum.VARIABLE;
        } else if (cls.equals(ExternalEventDefModel.class)) {
            return GetableClassEnum.EXTERNAL_EVENT_DEF;
        } else if (cls.equals(ExternalEventModel.class)) {
            return GetableClassEnum.EXTERNAL_EVENT;
        } else if (cls.equals(TaskDefMetricsModel.class)) {
            return GetableClassEnum.TASK_DEF_METRICS;
        } else if (cls.equals(WfSpecMetricsModel.class)) {
            return GetableClassEnum.WF_SPEC_METRICS;
        } else if (cls.equals(TaskWorkerGroupModel.class)) {
            return GetableClassEnum.TASK_WORKER_GROUP;
        } else if (cls.equals(UserTaskDefModel.class)) {
            return GetableClassEnum.USER_TASK_DEF;
        } else if (cls.equals(TaskRunModel.class)) {
            return GetableClassEnum.TASK_RUN;
        } else if (cls.equals(UserTaskRunModel.class)) {
            return GetableClassEnum.USER_TASK_RUN;
        } else {
            throw new IllegalArgumentException("Uh oh, unrecognized: " + cls.getName());
        }
    }

    public static Class<? extends AbstractGetable<?>> getCls(GetableClassEnum type) {
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
                return VariableModel.class;
            case EXTERNAL_EVENT_DEF:
                return ExternalEventDefModel.class;
            case EXTERNAL_EVENT:
                return ExternalEventModel.class;
            case TASK_DEF_METRICS:
                return TaskDefMetricsModel.class;
            case WF_SPEC_METRICS:
                return WfSpecMetricsModel.class;
            case TASK_WORKER_GROUP:
                return TaskWorkerGroupModel.class;
            case USER_TASK_DEF:
                return UserTaskDefModel.class;
            case TASK_RUN:
                return TaskRunModel.class;
            case USER_TASK_RUN:
                return UserTaskRunModel.class;
            case UNRECOGNIZED:
                // default:
        }
        throw new IllegalArgumentException("Unrecognized/unimplemented GetableClassEnum");
    }

    public static Class<? extends ObjectId<?, ?, ?>> getIdCls(GetableClassEnum type) {
        switch (type) {
            case WF_RUN:
                return WfRunIdModel.class;
            case NODE_RUN:
                return NodeRunIdModel.class;
            case WF_SPEC:
                return WfSpecIdModel.class;
            case TASK_DEF:
                return TaskDefIdModel.class;
            case VARIABLE:
                return VariableIdModel.class;
            case EXTERNAL_EVENT_DEF:
                return ExternalEventDefIdModel.class;
            case EXTERNAL_EVENT:
                return ExternalEventIdModel.class;
            case TASK_DEF_METRICS:
                return TaskDefMetricsIdModel.class;
            case WF_SPEC_METRICS:
                return WfSpecMetricsIdModel.class;
            case TASK_WORKER_GROUP:
                return TaskWorkerGroupIdModel.class;
            case USER_TASK_DEF:
                return UserTaskDefIdModel.class;
            case TASK_RUN:
                return TaskRunIdModel.class;
            case USER_TASK_RUN:
                return UserTaskRunIdModel.class;
            case UNRECOGNIZED:
        }
        throw new IllegalArgumentException("Unrecognized/unimplemented GetableClassEnum");
    }

    public abstract List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations();

    public abstract ObjectId<?, T, ?> getObjectId();

    public String getStoreKey() {
        return getObjectId().getStoreKey();
    }

    public abstract List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType);

    public List<Tag> getIndexEntries() {
        List<Tag> out = new ArrayList<>();
        for (GetableIndex<? extends AbstractGetable<?>> indexConfiguration : this.getIndexConfigurations()) {
            if (!indexConfiguration.isValid(this)) {
                continue;
            }
            Optional<TagStorageType> tagStorageType = indexConfiguration.getTagStorageType();
            List<IndexedField> singleIndexedValues = indexConfiguration.getAttributes().stream()
                    .filter(stringValueTypePair -> {
                        return stringValueTypePair.getValue().equals(GetableIndex.ValueType.SINGLE);
                    })
                    .map(stringValueTypePair -> {
                        return this.getIndexValues(stringValueTypePair.getKey(), tagStorageType)
                                .get(0);
                    })
                    .toList();
            List<IndexedField> dynamicIndexedFields = indexConfiguration.getAttributes().stream()
                    .filter(stringValueTypePair -> {
                        return stringValueTypePair.getValue().equals(GetableIndex.ValueType.DYNAMIC);
                    })
                    .flatMap(stringValueTypePair -> this.getIndexValues(stringValueTypePair.getKey(), tagStorageType)
                            .stream())
                    .toList();
            List<List<IndexedField>> combine = combine(singleIndexedValues, dynamicIndexedFields);
            for (List<IndexedField> list : combine) {
                TagStorageType storageType = list.stream()
                        .map(IndexedField::getTagStorageType)
                        .filter(tagStorageTypePb1 -> tagStorageTypePb1 == TagStorageType.REMOTE)
                        .findAny()
                        .orElse(TagStorageType.LOCAL);
                List<Pair<String, String>> pairs = list.stream()
                        .map(indexedField -> Pair.of(
                                indexedField.getKey(), indexedField.getValue().toString()))
                        .toList();
                out.add(new Tag(this, storageType, pairs));
            }
        }
        return out;
    }

    private List<List<IndexedField>> combine(List<IndexedField> source, List<IndexedField> multiple) {
        if (multiple.isEmpty()) {
            return List.of(source);
        }
        List<List<IndexedField>> result = new ArrayList<>();
        for (IndexedField dynamicIndexedField : multiple) {
            List<IndexedField> list = Stream.concat(source.stream(), Stream.of(dynamicIndexedField))
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
 * - currently no. It would double the storage costs.
 */
