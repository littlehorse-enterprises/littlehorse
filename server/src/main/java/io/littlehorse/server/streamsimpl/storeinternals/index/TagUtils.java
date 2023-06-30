package io.littlehorse.server.streamsimpl.storeinternals.index;

import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.NodeRunPb.NodeTypeCase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class TagUtils {

    @SuppressWarnings("unchecked")
    public static List<Tag> tagThing(Getable<?> thing) {
        switch (
            Getable.getTypeEnum((Class<? extends Getable<?>>) (thing.getClass()))
        ) {
            case EXTERNAL_EVENT_DEF:
                return tag((ExternalEventDef) thing);
            case TASK_DEF:
                return tag((TaskDef) thing);
            case WF_SPEC:
                return tag((WfSpec) thing);
            case NODE_RUN:
                return tag((NodeRun) thing);
            case EXTERNAL_EVENT:
                return tag((ExternalEvent) thing);
            case VARIABLE:
                return tag((Variable) thing);
            case WF_RUN:
            case TASK_WORKER_GROUP:
            case TASK_DEF_METRICS:
            case WF_SPEC_METRICS:
            case USER_TASK_DEF:
                // No tags here...we might make it possible to do more fancy
                // searches on metadata's in the future, but for now we don't.
                return new ArrayList<>();
            case UNRECOGNIZED:
        }
        throw new RuntimeException("Not possible");
    }

    private static List<Tag> tag(ExternalEventDef thing) {
        return Arrays.asList(
            new Tag(thing, TagStorageTypePb.LOCAL, Pair.of("name", thing.name))
        );
    }

    private static List<Tag> tag(TaskDef thing) {
        return Arrays.asList(
            new Tag(thing, TagStorageTypePb.LOCAL, Pair.of("name", thing.name))
        );
    }

    private static List<Tag> tag(WfSpec thing) {
        return Arrays.asList(
            new Tag(thing, TagStorageTypePb.LOCAL, Pair.of("name", thing.name))
        );
    }

    private static List<Tag> tag(Variable thing) {
        Pair<String, String> valuePair = thing.value.getValueTagPair();

        if (valuePair != null) {
            WfSpec spec = thing.getWfSpec();
            return Arrays.asList(
                new Tag(
                    thing,
                    TagStorageTypePb.LOCAL,
                    valuePair,
                    Pair.of("name", thing.name),
                    Pair.of("wfSpecName", spec.name),
                    Pair.of("wfSpecVersion", LHUtil.toLHDbVersionFormat(spec.version))
                )
            );
        } else {
            return new ArrayList<>();
        }
    }

    private static List<Tag> tag(ExternalEvent thing) {
        // I don't think there's anything to do for ExternalEvent's yet.
        // Perhaps once we add schemas, it will make sense.
        return new ArrayList<>();
    }

    private static List<Tag> tag(NodeRun thing) {
        List<Tag> out = new ArrayList<>();
        out.add(
            new Tag(
                thing,
                TagStorageTypePb.LOCAL,
                Pair.of("type", thing.type.toString()),
                Pair.of("status", thing.status.toString())
            )
        );

        if (thing.type == NodeTypeCase.EXTERNAL_EVENT) {
            out.add(
                new Tag(
                    thing,
                    TagStorageTypePb.LOCAL,
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
                    TagStorageTypePb.LOCAL,
                    Pair.of("taskDefName", thing.taskRun.taskDefName),
                    Pair.of("status", thing.status.toString())
                )
            );
            out.add(
                new Tag(
                    thing,
                    TagStorageTypePb.LOCAL,
                    Pair.of("taskDefName", thing.taskRun.taskDefName)
                )
            );
        } else if (thing.type == NodeTypeCase.USER_TASK) {
            out.addAll(thing.userTaskRun.getTags());
        }

        return out;
    }
}
