package io.littlehorse.common.model;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.model.wfrun.noderun.NodeRun;
import io.littlehorse.common.proto.GETableClassEnumPb;
import java.util.Date;
import java.util.List;

public abstract class GETable<T extends MessageOrBuilder> extends LHSerializable<T> {

    public abstract Date getCreatedAt();

    public abstract String getPartitionKey();

    public abstract String getObjectId();

    public abstract List<Tag> getTags();

    public static String getBaseStoreName(Class<? extends GETable<?>> cls) {
        return cls.getSimpleName() + "_BaseStore";
    }

    public static String getTagStoreName(Class<? extends GETable<?>> cls) {
        return cls.getSimpleName() + "_TagCache";
    }

    public static String getTaggingProcessorName(Class<? extends GETable<?>> cls) {
        return cls.getSimpleName() + "_TaggingProcessor";
    }

    public static GETableClassEnumPb getTypeEnum(Class<? extends GETable<?>> cls) {
        if (cls.equals(WfRun.class)) {
            return GETableClassEnumPb.WF_RUN;
        } else if (cls.equals(ThreadRun.class)) {
            return GETableClassEnumPb.THREAD_RUN;
        } else if (cls.equals(NodeRun.class)) {
            return GETableClassEnumPb.TASK_RUN;
        } else if (cls.equals(WfSpec.class)) {
            return GETableClassEnumPb.WF_SPEC;
        } else if (cls.equals(TaskDef.class)) {
            return GETableClassEnumPb.TASK_DEF;
        } else if (cls.equals(Variable.class)) {
            return GETableClassEnumPb.VARIABLE;
        } else if (cls.equals(ExternalEventDef.class)) {
            return GETableClassEnumPb.EXTERNAL_EVENT_DEF;
        } else {
            throw new RuntimeException("Uh oh, unrecognized: " + cls.getName());
        }
    }

    public static Class<? extends GETable<?>> getCls(GETableClassEnumPb type) {
        switch (type) {
            case WF_RUN:
                return WfRun.class;
            case TASK_RUN:
                return NodeRun.class;
            case WF_SPEC:
                return WfSpec.class;
            case TASK_DEF:
                return TaskDef.class;
            case VARIABLE:
                return Variable.class;
            case EXTERNAL_EVENT_DEF:
                return ExternalEventDef.class;
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
 * - Will we query VariableValue's from the Scheduler topology or from the
 *   API topology?
 *
 * Will we make it possible to deploy the Scheduler separately from the API?
 *   - yes we will.
 */
