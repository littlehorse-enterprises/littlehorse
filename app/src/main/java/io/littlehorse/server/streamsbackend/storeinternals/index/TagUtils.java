package io.littlehorse.server.streamsbackend.storeinternals.index;

import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.NodeRunPb.NodeTypeCase;
import io.littlehorse.common.proto.TagStorageTypePb;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class TagUtils {

    @SuppressWarnings("unchecked")
    public static List<Tag> tagThing(GETable<?> thing) {
        switch (
            GETable.getTypeEnum((Class<? extends GETable<?>>) (thing.getClass()))
        ) {
            case EXTERNAL_EVENT_DEF:
                return tag((ExternalEventDef) thing);
            case TASK_DEF:
                return tag((TaskDef) thing);
            case WF_SPEC:
                return tag((WfSpec) thing);
            case NODE_RUN:
                return tag((NodeRun) thing);
            case WF_RUN:
                return tag((WfRun) thing);
            case EXTERNAL_EVENT:
                return tag((ExternalEvent) thing);
            case VARIABLE:
                return tag((Variable) thing);
            case TASK_SCHEDULE_REQUEST:
                return tag((TaskScheduleRequest) thing);
            case UNRECOGNIZED:
            default:
                throw new RuntimeException("Not possible");
        }
    }

    private static List<Tag> tag(ExternalEventDef thing) {
        return Arrays.asList(
            new Tag(
                thing,
                TagStorageTypePb.LOCAL_UNCOUNTED,
                Pair.of("name", thing.name)
            )
        );
    }

    private static List<Tag> tag(TaskDef thing) {
        return Arrays.asList(
            new Tag(
                thing,
                TagStorageTypePb.LOCAL_UNCOUNTED,
                Pair.of("name", thing.name)
            )
        );
    }

    private static List<Tag> tag(WfSpec thing) {
        return Arrays.asList(
            new Tag(
                thing,
                TagStorageTypePb.LOCAL_UNCOUNTED,
                Pair.of("name", thing.name)
            )
        );
    }

    private static List<Tag> tag(WfRun thing) {
        return Arrays.asList(
            new Tag(
                thing,
                TagStorageTypePb.LOCAL_UNCOUNTED,
                Pair.of("wfSpecName", thing.wfSpecName),
                Pair.of("status", thing.status.toString())
            )
        );
    }

    private static List<Tag> tag(Variable thing) {
        // TODO: Figure out a good way to tag Variables.
        return new ArrayList<>();
    }

    private static List<Tag> tag(ExternalEvent thing) {
        // I don't think there's anything to do for ExternalEvent's yet.
        // Perhaps once we add schemas, it will make sense.k
        return new ArrayList<>();
    }

    private static List<Tag> tag(NodeRun thing) {
        List<Tag> out = new ArrayList<>();
        out.add(
            new Tag(
                thing,
                TagStorageTypePb.LOCAL_UNCOUNTED,
                Pair.of("type", thing.type.toString()),
                Pair.of("status", thing.status.toString())
            )
        );

        if (thing.type == NodeTypeCase.EXTERNAL_EVENT) {
            out.add(
                new Tag(
                    thing,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("type", "EXTERNAL_EVENT"),
                    Pair.of(
                        "externalEventDefName",
                        thing.externalEventRun.externalEventDefName
                    )
                )
            );
        } else if (thing.type == NodeTypeCase.TASK) {
            out.add(
                new Tag(
                    thing,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("taskDefName", thing.taskRun.taskDefName),
                    Pair.of("status", thing.status.toString())
                )
            );
        }

        return out;
    }

    private static List<Tag> tag(TaskScheduleRequest thing) {
        return Arrays.asList(
            new Tag(
                thing,
                TagStorageTypePb.LOCAL_COUNTED,
                Pair.of("taskDefName", thing.taskDefName)
            ),
            new Tag(
                thing,
                TagStorageTypePb.LOCAL_UNCOUNTED,
                Pair.of("wfRunId", thing.wfRunId)
            )
        );
    }
}
