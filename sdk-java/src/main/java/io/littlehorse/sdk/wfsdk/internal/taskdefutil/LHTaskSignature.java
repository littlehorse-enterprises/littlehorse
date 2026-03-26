package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.sdk.worker.internal.util.PlaceholderUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHTaskSignature {

    @Getter
    List<LHTaskParameter> variableDefs;

    LHTaskReturnType returnType;

    LinkedHashSet<LHStructDefType> structDefClasses;

    Method taskMethod;

    @Getter
    private String taskDefName;

    @Getter
    private Optional<String> taskDefDescription;

    boolean hasWorkerContext;

    Object executable;
    private final LHTypeAdapterRegistry typeAdapterRegistry;
    private final Map<String, String> placeholderValues;

    public LHTaskSignature(
            Method taskMethod, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        if (!taskMethod.isAnnotationPresent(LHTaskMethod.class)) {
            throw new TaskSchemaMismatchError(String.format(
                    "Cannot create LHTaskSignature: Provided method %s is missing the required @LHTaskMethod annotation.",
                    taskMethod.getName()));
        }

        this.taskMethod = taskMethod;
        LHTaskMethod lhTaskMethod = this.taskMethod.getAnnotation(LHTaskMethod.class);

        this.typeAdapterRegistry = Objects.requireNonNull(typeAdapterRegistry);
        this.placeholderValues = placeholderValues == null ? Map.of() : Map.copyOf(placeholderValues);
        taskDefName = PlaceholderUtil.replacePlaceholders(lhTaskMethod.value(), this.placeholderValues);
        taskDefDescription = Optional.of(lhTaskMethod.description());
        variableDefs = new ArrayList<>();
        structDefClasses = new LinkedHashSet<>();

        initializeVariableDefs();
    }

    private void initializeVariableDefs() {
        for (int i = 0; i < taskMethod.getParameterCount(); i++) {
            Parameter param = taskMethod.getParameters()[i];
            if (param.getType().equals(WorkerContext.class)) {
                if (i + 1 != taskMethod.getParameterCount()) {
                    throw new TaskSchemaMismatchError("Can only have WorkerContext as the last parameter.");
                } else {
                    hasWorkerContext = true;
                    continue; // could also be `break;`
                }
            }

            variableDefs.add(new LHTaskParameter(param, typeAdapterRegistry, placeholderValues));
        }
        returnType = new LHTaskReturnType(taskMethod, typeAdapterRegistry, placeholderValues);
    }

    public boolean hasWorkerContext() {
        return hasWorkerContext;
    }

    public Method getTaskMethod() {
        return taskMethod;
    }

    public ReturnType getReturnType() {
        return returnType.getReturnType();
    }

    public List<LHStructDefType> getStructDefDependencies() {
        return Collections.unmodifiableList(new ArrayList<>(structDefClasses));
    }

    public PutTaskDefRequest toPutTaskDefRequest() {
        PutTaskDefRequest.Builder out = PutTaskDefRequest.newBuilder();

        out.addAllInputVars(
                this.variableDefs.stream().map(LHTaskParameter::getVariableDef).toList());
        out.setName(this.taskDefName);
        if (this.returnType != null) {
            out.setReturnType(returnType.getReturnType());
        }
        if (this.taskDefDescription.isPresent()) {
            out.setDescription(this.taskDefDescription.get());
        }

        return out.build();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LHTaskSignature)) return false;

        // TODO: Improve TaskSignature equals!

        return true;
    }
}
