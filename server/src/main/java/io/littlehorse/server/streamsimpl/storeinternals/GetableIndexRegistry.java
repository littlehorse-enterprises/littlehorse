package io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.TaskWorkerGroup;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.meta.usertasks.UserTaskDef;
import io.littlehorse.common.model.metrics.TaskDefMetrics;
import io.littlehorse.common.model.metrics.WfSpecMetrics;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import java.util.*;

public class GetableIndexRegistry {

    private static GetableIndexRegistry INSTANCE;

    private GetableIndexRegistry() {
        // Hide constructor
    }

    public static GetableIndexRegistry getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        INSTANCE = new GetableIndexRegistry();
        getables()
            .forEach(getableClass -> {
                try {
                    Getable<?> geTable = getableClass
                        .getDeclaredConstructor()
                        .newInstance();
                    List<GetableIndex> geTableIndexes = geTable.getIndexes();
                    INSTANCE.register(getableClass, geTableIndexes);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });
        return INSTANCE;
    }

    private static List<Class<? extends Getable<?>>> getables() {
        return List.of(
            WfRun.class,
            ExternalEvent.class,
            ExternalEventDef.class,
            NodeRun.class,
            TaskDefMetrics.class,
            TaskWorkerGroup.class,
            Variable.class,
            WfSpec.class,
            WfSpecMetrics.class,
            TaskDef.class,
            UserTaskDef.class,
            TaskRun.class
        );
    }

    private final Map<Class<?>, List<GetableIndex>> indexes = new HashMap<>();

    public GetableIndexRegistry register(
        Class<?> target,
        List<GetableIndex> getableIndexes
    ) {
        List<GetableIndex> registeredIndexes = indexes.getOrDefault(
            target,
            new ArrayList<>()
        );
        registeredIndexes.addAll(getableIndexes);
        indexes.put(target, registeredIndexes);
        return this;
    }

    public List<GetableIndex> findIndexesFor(Class<?> getableClass) {
        return indexes.get(getableClass);
    }

    public GetableIndex findConfigurationForAttributes(
        Class<?> getableClass,
        Collection<String> attributes
    ) {
        return findIndexesFor(getableClass)
            .stream()
            .filter(geTableIndex ->
                exactlyMatchInAnyOrder(geTableIndex.getKeys(), attributes)
            )
            .findFirst()
            .orElse(null);
    }

    private boolean exactlyMatchInAnyOrder(
        Collection<String> keys,
        Collection<String> attributes
    ) {
        return new TreeSet<>(keys).equals(new TreeSet<>(attributes));
    }
}
