package io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.common.model.GETable;
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
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class GETableIndexRegistry {

    private static GETableIndexRegistry INSTANCE;

    private GETableIndexRegistry() {
        // Hide constructor
    }

    public static GETableIndexRegistry getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        INSTANCE = new GETableIndexRegistry();
        getables()
            .forEach(getableClass -> {
                try {
                    GETable<?> geTable = getableClass
                        .getDeclaredConstructor()
                        .newInstance();
                    List<GETableIndex> geTableIndexes = geTable.getIndexes();
                    INSTANCE.register(getableClass, geTableIndexes);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });
        return INSTANCE;
    }

    private static List<Class<? extends GETable<?>>> getables() {
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
            UserTaskDef.class
        );
    }

    private final Map<Class<?>, List<GETableIndex>> indexes = new HashMap<>();

    public GETableIndexRegistry register(
        Class<?> target,
        List<GETableIndex> getableIndexes
    ) {
        List<GETableIndex> registeredIndexes = indexes.getOrDefault(
            target,
            new ArrayList<>()
        );
        registeredIndexes.addAll(getableIndexes);
        indexes.put(target, registeredIndexes);
        return this;
    }

    public List<GETableIndex> findIndexesFor(Class<?> getableClass) {
        return indexes.get(getableClass);
    }

    public GETableIndex findConfigurationForAttributes(
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
