package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefId;
import io.littlehorse.sdk.worker.LHType;
import io.littlehorse.sdk.worker.internal.util.PlaceholderUtil;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHTaskParameter {

    private Parameter parameter;
    protected LHTypeAdapterRegistry lhTypeAdapterRegistry;

    @Getter
    private LHClassType variableClassType;

    @Getter
    private String variableName;

    private boolean isMasked;
    private boolean isLHArray;

    private Optional<String> structDefName;
    private final Map<String, String> placeholderValues;

    private VariableDef variableDef;

    public LHTaskParameter(
            Parameter parameter, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        this.parameter = parameter;
        this.lhTypeAdapterRegistry = typeAdapterRegistry;
        this.placeholderValues = placeholderValues == null ? Map.of() : Map.copyOf(placeholderValues);

        variableName = getVarNameFromParameterName();
        isMasked = false;
        structDefName = Optional.empty();

        maybeInitializeFromLHType();

        if (InlineStruct.class.isAssignableFrom(parameter.getType())) {
            if (!structDefName.isPresent()) {
                throw new TaskSchemaMismatchError(
                        "InlineStruct parameters must declare @LHType(structDefName = \\\"...\\\") via the LHType annotation, but parameter "
                                + parameter.getName() + " of type "
                                + parameter.getType().getName() + " did not.");
            } else {
                this.variableClassType = new LHStructDefId(structDefName.get());
            }
        } else {
            if (structDefName.isPresent()) {
                throw new TaskSchemaMismatchError(
                        "@LHType(structDefName = ...) can only be used on InlineStruct parameters and returns.");
            }

            this.variableClassType = LHClassType.fromJavaClass(parameter.getType(), typeAdapterRegistry);
        }
    }

    private String getVarNameFromParameterName() {
        if (!parameter.isNamePresent()) {
            log.warn(
                    "Unable to inspect parameter names using reflection; please either compile with"
                            + " `javac -parameters` to enable this, or specify a name via the LHType annotation. "
                            + "Using the parameter position as its name, which makes the resulting TaskDef harder to understand.");
        }
        return parameter.getName();
    }

    private void maybeInitializeFromLHType() {
        if (!parameter.isAnnotationPresent(LHType.class)) return;

        LHType typeAnnotation = parameter.getAnnotation(LHType.class);
        isMasked = typeAnnotation.masked();

        if (typeAnnotation.name() != null && !typeAnnotation.name().isBlank()) {
            variableName = typeAnnotation.name();
        }

        if (typeAnnotation.structDefName() != null
                && !typeAnnotation.structDefName().isBlank()) {
            structDefName =
                    Optional.of(PlaceholderUtil.replacePlaceholders(typeAnnotation.structDefName(), placeholderValues));
        }
    }

    private void buildVariableDef() {
        variableDef = VariableDef.newBuilder()
                .setName(variableName)
                .setTypeDef(variableClassType.getTypeDefinition().toBuilder()
                        .setMasked(isMasked)
                        .build())
                .build();
    }

    public VariableDef getVariableDef() {
        if (variableDef == null) {
            buildVariableDef();
        }

        return variableDef;
    }

    public Class<?> getParameterType() {
        return parameter.getType();
    }
}
