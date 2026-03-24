package io.littlehorse.sdk.worker.internal.util;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.TaskDef;
import java.lang.reflect.Method;

public class TaskDefMapping {
    private final TaskDef taskDef;
    private final Method taskMethod;
    private final LHTypeAdapterRegistry typeAdapterRegistry;

    public TaskDefMapping(TaskDef taskDef, Method taskMethod, LHTypeAdapterRegistry typeAdapterRegistry) {
        this.taskDef = taskDef;
        this.taskMethod = taskMethod;
        this.typeAdapterRegistry = typeAdapterRegistry;
    }
}
