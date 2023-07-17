package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.TaskWorkerGroup;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.meta.usertasks.UserTaskDef;
import io.littlehorse.common.model.metrics.TaskDefMetrics;
import io.littlehorse.common.model.metrics.WfSpecMetrics;
import io.littlehorse.common.model.objectId.ExternalEventDefId;
import io.littlehorse.common.model.objectId.ExternalEventId;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.model.objectId.TaskDefId;
import io.littlehorse.common.model.objectId.TaskDefMetricsId;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.model.objectId.TaskWorkerGroupId;
import io.littlehorse.common.model.objectId.UserTaskDefId;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.model.objectId.WfRunId;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.model.objectId.WfSpecMetricsId;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

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
        if (cls.equals(WfRun.class)) {
            return GetableClassEnumPb.WF_RUN;
        } else if (cls.equals(NodeRun.class)) {
            return GetableClassEnumPb.NODE_RUN;
        } else if (cls.equals(WfSpec.class)) {
            return GetableClassEnumPb.WF_SPEC;
        } else if (cls.equals(TaskDef.class)) {
            return GetableClassEnumPb.TASK_DEF;
        } else if (cls.equals(Variable.class)) {
            return GetableClassEnumPb.VARIABLE;
        } else if (cls.equals(ExternalEventDef.class)) {
            return GetableClassEnumPb.EXTERNAL_EVENT_DEF;
        } else if (cls.equals(ExternalEvent.class)) {
            return GetableClassEnumPb.EXTERNAL_EVENT;
        } else if (cls.equals(TaskDefMetrics.class)) {
            return GetableClassEnumPb.TASK_DEF_METRICS;
        } else if (cls.equals(WfSpecMetrics.class)) {
            return GetableClassEnumPb.WF_SPEC_METRICS;
        } else if (cls.equals(TaskWorkerGroup.class)) {
            return GetableClassEnumPb.TASK_WORKER_GROUP;
        } else if (cls.equals(UserTaskDef.class)) {
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
                return WfRun.class;
            case NODE_RUN:
                return NodeRun.class;
            case WF_SPEC:
                return WfSpec.class;
            case TASK_DEF:
                return TaskDef.class;
            case VARIABLE:
                return Variable.class;
            case EXTERNAL_EVENT_DEF:
                return ExternalEventDef.class;
            case EXTERNAL_EVENT:
                return ExternalEvent.class;
            case TASK_DEF_METRICS:
                return TaskDefMetrics.class;
            case WF_SPEC_METRICS:
                return WfSpecMetrics.class;
            case TASK_WORKER_GROUP:
                return TaskWorkerGroup.class;
            case USER_TASK_DEF:
                return UserTaskDef.class;
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
                return ExternalEventDefId.class;
            case EXTERNAL_EVENT:
                return ExternalEventId.class;
            case TASK_DEF_METRICS:
                return TaskDefMetricsId.class;
            case WF_SPEC_METRICS:
                return WfSpecMetricsId.class;
            case TASK_WORKER_GROUP:
                return TaskWorkerGroupId.class;
            case USER_TASK_DEF:
                return UserTaskDefId.class;
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
