package io.littlehorse.common.model;

import com.google.protobuf.Message;
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
import io.littlehorse.common.model.objectId.TaskWorkerGroupId;
import io.littlehorse.common.model.objectId.UserTaskDefId;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.model.objectId.WfRunId;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.model.objectId.WfSpecMetricsId;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.server.streamsimpl.storeinternals.GETableIndex;
import java.util.Date;
import java.util.List;

public abstract class GETable<T extends Message> extends Storeable<T> {

    public abstract Date getCreatedAt();

    public String getPartitionKey() {
        return getObjectId().getPartitionKey();
    }

    public static GETableClassEnumPb getTypeEnum(Class<? extends GETable<?>> cls) {
        if (cls.equals(WfRun.class)) {
            return GETableClassEnumPb.WF_RUN;
        } else if (cls.equals(NodeRun.class)) {
            return GETableClassEnumPb.NODE_RUN;
        } else if (cls.equals(WfSpec.class)) {
            return GETableClassEnumPb.WF_SPEC;
        } else if (cls.equals(TaskDef.class)) {
            return GETableClassEnumPb.TASK_DEF;
        } else if (cls.equals(Variable.class)) {
            return GETableClassEnumPb.VARIABLE;
        } else if (cls.equals(ExternalEventDef.class)) {
            return GETableClassEnumPb.EXTERNAL_EVENT_DEF;
        } else if (cls.equals(ExternalEvent.class)) {
            return GETableClassEnumPb.EXTERNAL_EVENT;
        } else if (cls.equals(TaskDefMetrics.class)) {
            return GETableClassEnumPb.TASK_DEF_METRICS;
        } else if (cls.equals(WfSpecMetrics.class)) {
            return GETableClassEnumPb.WF_SPEC_METRICS;
        } else if (cls.equals(TaskWorkerGroup.class)) {
            return GETableClassEnumPb.TASK_WORKER_GROUP;
        } else if (cls.equals(UserTaskDef.class)) {
            return GETableClassEnumPb.USER_TASK_DEF;
        } else {
            throw new RuntimeException("Uh oh, unrecognized: " + cls.getName());
        }
    }

    public static Class<? extends GETable<?>> getCls(GETableClassEnumPb type) {
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
            case UNRECOGNIZED:
            default:
                throw new RuntimeException("Uh oh, unrecognized enum");
        }
    }

    public static Class<? extends ObjectId<?, ?, ?>> getIdCls(
        GETableClassEnumPb type
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
            case UNRECOGNIZED:
            default:
                throw new RuntimeException("Uh oh, unrecognized enum");
        }
    }

    public abstract List<GETableIndex> getIndexes();

    public abstract ObjectId<?, T, ?> getObjectId();

    public String getStoreKey() {
        return getObjectId().getStoreKey();
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
