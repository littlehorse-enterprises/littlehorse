package io.littlehorse.common.model;

import java.util.Date;
import java.util.List;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.server.GETableClassEnumPb;
import io.littlehorse.server.model.internal.IndexEntry;
import io.littlehorse.server.model.wfrun.TaskRun;
import io.littlehorse.server.model.wfrun.ThreadRun;
import io.littlehorse.server.model.wfrun.WfRun;

public abstract class GETable<T extends MessageOrBuilder> extends LHSerializable<T> {
    public abstract Date getCreatedAt();

    public abstract String getPartitionKey();

    public abstract String getObjectId();

    public abstract List<IndexEntry> getIndexEntries();

    public static String getBaseStoreName(Class<? extends GETable<?>> cls) {
        return cls.getSimpleName() + "_BaseStore";
    }

    public static String getIndexStoreName(Class<? extends GETable<?>> cls) {
        return cls.getSimpleName() + "_IdxCache";
    }

    public static GETableClassEnumPb getTypeEnum(Class<? extends GETable<?>> cls) {
        if (cls.equals(WfRun.class)) {
            return GETableClassEnumPb.WF_RUN;

        } else if (cls.equals(ThreadRun.class)) {
            return GETableClassEnumPb.THREAD_RUN;

        } else if (cls.equals(TaskRun.class)) {
            return GETableClassEnumPb.TASK_RUN;

        } else if (cls.equals(WfSpec.class)) {
            return GETableClassEnumPb.WF_SPEC;

        } else if (cls.equals(TaskDef.class)) {
            return GETableClassEnumPb.TASK_DEF;

        } else {
            throw new RuntimeException("Uh oh, unrecognized: " + cls.getName());
        }
    }

    public static Class<? extends GETable<?>> getCls(GETableClassEnumPb type) {
        switch(type){
        case WF_RUN:
            return WfRun.class;
        case THREAD_RUN:
            return ThreadRun.class;
        case TASK_RUN:
            return TaskRun.class;
        case WF_SPEC:
            return WfSpec.class;
        case TASK_DEF:
            return TaskDef.class;
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
