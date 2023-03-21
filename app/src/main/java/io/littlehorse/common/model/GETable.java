package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.GETableClassEnumPb;
import java.util.Date;

public abstract class GETable<T extends Message> extends Storeable<T> {

    public abstract Date getCreatedAt();

    public abstract String getPartitionKey();

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
        } else if (cls.equals(TaskScheduleRequest.class)) {
            return GETableClassEnumPb.TASK_SCHEDULE_REQUEST;
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
            case TASK_SCHEDULE_REQUEST:
                return TaskScheduleRequest.class;
            case UNRECOGNIZED:
            default:
                throw new RuntimeException("Uh oh, unrecognized enum");
        }
    }
}
/*
 * Some random thoughts:
 * - each GETable has a partition key and an ID. They may be different.
 * - For example, we want TaskRun's for a WfRun to end up on the same host
 * - VariableValue's for a ThreadRun will end up on the same node as each other
 *
 * Will we make it possible to deploy the Scheduler separately from the API?
 *   - currently no.
 */
