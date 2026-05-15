package io.littlehorse.sdk.worker;

import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.worker.internal.util.PlaceholderUtil;
import java.lang.reflect.Method;
import java.util.Map;
import lombok.Getter;

@Getter
public final class LHTaskMethodHandle {

    private final String taskDefName;
    private final String description;
    private final Method taskMethod;

    private LHTaskMethodHandle(String taskDefName, String description, Method taskMethod) {
        this.taskDefName = taskDefName;
        this.description = description;
        this.taskMethod = taskMethod;
    }

    public static LHTaskMethodHandle fromLHTaskMethod(Method taskMethod, Map<String, String> placeholderValues) {
        if (!taskMethod.isAnnotationPresent(LHTaskMethod.class)) {
            throw new TaskSchemaMismatchError(String.format(
                    "Cannot create LHTaskMethodHandle: Provided method %s is missing the required @LHTaskMethod annotation.",
                    taskMethod.getName()));
        }
        LHTaskMethod lhTaskMethod = taskMethod.getAnnotation(LHTaskMethod.class);
        String taskDefName = PlaceholderUtil.replacePlaceholders(lhTaskMethod.value(), placeholderValues);
        return new LHTaskMethodHandle(taskDefName, lhTaskMethod.description(), taskMethod);
    }

    public static LHTaskMethodHandle from(String taskDefName, String description, Method taskMethod) {
        return new LHTaskMethodHandle(taskDefName, description, taskMethod);
    }
}
