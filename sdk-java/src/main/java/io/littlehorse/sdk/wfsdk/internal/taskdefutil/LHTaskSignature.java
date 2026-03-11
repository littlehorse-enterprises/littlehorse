package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHTaskSignature {

    @Getter
    List<VariableDef> variableDefs;

    LinkedHashSet<LHStructDefType> structDefClasses;

    Method taskMethod;
    boolean hasWorkerContextAtEnd;
    String taskDefName;
    String lhTaskMethodAnnotationValue;
    Object executable;
    ReturnType outputSchema;
    private final LHTypeAdapterRegistry typeAdapterRegistry;
    private final Map<String, String> placeholderValues;

    @Getter
    private String taskDefDescription;

    public LHTaskSignature(String taskDefName, Object executable, String lhTaskMethodAnnotationValue)
            throws TaskSchemaMismatchError {
        this(taskDefName, executable, lhTaskMethodAnnotationValue, LHTypeAdapterRegistry.empty(), Map.of());
    }

    public LHTaskSignature(
            String taskDefName,
            Object executable,
            String lhTaskMethodAnnotationValue,
            List<LHTypeAdapter<?>> typeAdapters)
            throws TaskSchemaMismatchError {
        this(taskDefName, executable, lhTaskMethodAnnotationValue, LHTypeAdapterRegistry.from(typeAdapters), Map.of());
    }

    public LHTaskSignature(String taskDefName, Object executable, LHTypeAdapterRegistry typeAdapterRegistry)
            throws TaskSchemaMismatchError {
        this(taskDefName, executable, taskDefName, typeAdapterRegistry, Map.of());
    }

    public LHTaskSignature(
            String taskDefName,
            Object executable,
            String lhTaskMethodAnnotationValue,
            LHTypeAdapterRegistry typeAdapterRegistry)
            throws TaskSchemaMismatchError {
        this(taskDefName, executable, lhTaskMethodAnnotationValue, typeAdapterRegistry, Map.of());
    }

    public LHTaskSignature(
            String taskDefName,
            Object executable,
            String lhTaskMethodAnnotationValue,
            LHTypeAdapterRegistry typeAdapterRegistry,
            Map<String, String> placeholderValues)
            throws TaskSchemaMismatchError {
        variableDefs = new ArrayList<>();
        hasWorkerContextAtEnd = false;
        this.taskDefName = taskDefName;
        this.executable = executable;
        this.lhTaskMethodAnnotationValue = lhTaskMethodAnnotationValue;
        this.structDefClasses = new LinkedHashSet<>();
        this.typeAdapterRegistry = Objects.requireNonNull(typeAdapterRegistry, "Type adapter registry cannot be null");
        this.placeholderValues = placeholderValues == null ? Map.of() : Map.copyOf(placeholderValues);

        for (Method method : executable.getClass().getMethods()) {
            if (method.isAnnotationPresent(LHTaskMethod.class)) {
                LHTaskMethod lhTaskMethod = method.getAnnotation(LHTaskMethod.class);
                String taskDefForThisMethod = lhTaskMethod.value();

                if (!taskDefForThisMethod.equals(lhTaskMethodAnnotationValue)) {
                    continue;
                }

                String annotationDescription = lhTaskMethod.description();
                if (annotationDescription != null && !annotationDescription.isEmpty()) {
                    taskDefDescription = annotationDescription;
                }

                if (taskMethod != null) {
                    throw new TaskSchemaMismatchError("Found two annotated task methods!");
                }

                taskMethod = method;
            }
        }

        if (taskMethod == null) {
            throw new TaskSchemaMismatchError("Couldn't find annotated @LHTaskMethod for taskDef "
                    + taskDefName
                    + " on "
                    + executable.getClass());
        }

        for (int i = 0; i < taskMethod.getParameterCount(); i++) {
            Parameter param = taskMethod.getParameters()[i];
            if (param.getType().equals(WorkerContext.class)) {
                if (i + 1 != taskMethod.getParameterCount()) {
                    throw new TaskSchemaMismatchError("Can only have WorkerContext as the last parameter.");
                } else {
                    hasWorkerContextAtEnd = true;
                    continue; // could also be `break;`
                }
            }

            variableDefs.add(buildVariableDef(param));
        }
        outputSchema = buildReturnType(taskMethod.getReturnType());
    }

    private VariableDef buildVariableDef(Parameter param) {
        VariableDef.Builder varDef = VariableDef.newBuilder();
        LHClassType lhClassType = LHClassType.fromJavaClass(param.getType(), typeAdapterRegistry);
        LHType lhTypeAnnotation = param.getAnnotation(LHType.class);

        if (lhClassType instanceof LHStructDefType) {
            LHStructDefType lhStructDefType = (LHStructDefType) lhClassType;
            structDefClasses.addAll(lhStructDefType.getDependencyClasses());
        }

        TypeDefinition.Builder typeDef = getTypeDefinition(param.getType(), lhClassType, lhTypeAnnotation, true);

        // If param has `LHType` annotation...
        if (lhTypeAnnotation != null) {
            if (lhTypeAnnotation.name().isBlank()) {
                varDef.setName(varNameFromParameterName(param));
            } else {
                varDef.setName(resolvePlaceholders(lhTypeAnnotation.name()));
            }
            typeDef.setMasked(lhTypeAnnotation.masked());
        } else {
            varDef.setName(varNameFromParameterName(param));
        }

        varDef.setTypeDef(typeDef);
        return varDef.build();
    }

    private ReturnType buildReturnType(Class<?> classReturnType) {
        if (void.class.isAssignableFrom(classReturnType)) {
            // Empty `type` field signifies that it's void.
            return ReturnType.newBuilder().build();
        } else {
            LHClassType lhClassType = LHClassType.fromJavaClass(classReturnType, typeAdapterRegistry);
            LHType typeAnnotation = taskMethod.getAnnotation(LHType.class);

            if (lhClassType instanceof LHStructDefType) {
                LHStructDefType lhStructDefType = (LHStructDefType) lhClassType;
                structDefClasses.addAll(lhStructDefType.getDependencyClasses());
            }

            TypeDefinition.Builder typeDef = getTypeDefinition(classReturnType, lhClassType, typeAnnotation, false);

            if (typeAnnotation != null) {
                typeDef.setMasked(typeAnnotation.masked());
            }

            return ReturnType.newBuilder().setReturnType(typeDef).build();
        }
    }

    private String varNameFromParameterName(Parameter param) {
        if (!param.isNamePresent()) {
            log.warn(
                    "Unable to inspect parameter names using reflection; please either compile with"
                            + " `javac -parameters` to enable this, or specify a name via the LHType annotation. "
                            + "Using the parameter position as its name, which makes the resulting TaskDef harder to understand.");
        }
        return param.getName();
    }

    private TypeDefinition.Builder getAdaptedTypeDefinition(Class<?> javaClass, LHClassType lhClassType) {
        VariableType adapterType = LHLibUtil.getTypeAdapterForClass(javaClass, typeAdapterRegistry)
                .map(LHTypeAdapter::getVariableType)
                .orElse(VariableType.UNRECOGNIZED);

        if (adapterType != VariableType.UNRECOGNIZED) {
            return TypeDefinition.newBuilder().setPrimitiveType(adapterType);
        }

        return lhClassType.getTypeDefinition().toBuilder();
    }

    private TypeDefinition.Builder getTypeDefinition(
            Class<?> javaClass, LHClassType lhClassType, LHType lhTypeAnnotation, boolean isParameter) {
        String maybeStructDefName = getStructDefName(lhTypeAnnotation);
        boolean hasStructDefName = maybeStructDefName != null && !maybeStructDefName.isBlank();

        if (InlineStruct.class.equals(javaClass)) {
            if (!hasStructDefName) {
                throw new TaskSchemaMismatchError("InlineStruct "
                        + (isParameter ? "parameters" : "returns")
                        + " must declare @LHType(structDefName = \"...\").");
            }

            return TypeDefinition.newBuilder()
                    .setStructDefId(StructDefId.newBuilder().setName(maybeStructDefName));
        }

        if (hasStructDefName) {
            throw new TaskSchemaMismatchError(
                    "@LHType(structDefName = ...) can only be used on InlineStruct parameters and returns.");
        }

        return getAdaptedTypeDefinition(javaClass, lhClassType);
    }

    private String getStructDefName(LHType lhTypeAnnotation) {
        if (lhTypeAnnotation == null || lhTypeAnnotation.structDefName().isBlank()) {
            return "";
        }

        return resolvePlaceholders(lhTypeAnnotation.structDefName());
    }

    private String resolvePlaceholders(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return PlaceholderUtil.replacePlaceholders(text, placeholderValues);
    }

    public boolean getHasWorkerContextAtEnd() {
        return hasWorkerContextAtEnd;
    }

    public String getTaskDefName() {
        return taskDefName;
    }

    public Object getExecutable() {
        return executable;
    }

    public Method getTaskMethod() {
        return taskMethod;
    }

    public ReturnType getReturnType() {
        return outputSchema;
    }

    public List<LHStructDefType> getStructDefDependencies() {
        return Collections.unmodifiableList(new ArrayList<>(structDefClasses));
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LHTaskSignature)) return false;

        // TODO: Improve TaskSignature equals!

        return true;
    }
}
