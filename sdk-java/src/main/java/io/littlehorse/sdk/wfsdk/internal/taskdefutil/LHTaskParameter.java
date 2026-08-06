package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHArrayType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHMapType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefId;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHTaskParameter {

    private final Parameter parameter;

    @Getter
    private final String variableName;

    @Getter
    private final VariableDef variableDef;

    public LHTaskParameter(
            Parameter parameter, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        this.parameter = parameter;
        Map<String, String> resolvedPlaceholderValues =
                placeholderValues == null ? Map.of() : Map.copyOf(placeholderValues);

        LHTypeMetadata metadata = LHTypeMetadata.from(parameter, resolvedPlaceholderValues);
        Optional<String> structDefName = metadata.getStructDefName();

        this.variableName = metadata.getName().orElseGet(this::getVarNameFromParameterName);

        metadata.validateStructDefNameUsage(
                parameter.getType(), LHTypeMetadata.ValidationContext.PARAMETER, parameter.getName());
        metadata.validateLHArrayUsage(
                parameter.getType(), LHTypeMetadata.ValidationContext.PARAMETER, parameter.getName());
        metadata.validateLHMapUsage(
                parameter.getType(), LHTypeMetadata.ValidationContext.PARAMETER, parameter.getName());

        LHClassType variableClassType;

        if (metadata.isLHArray()) {
            variableClassType = new LHArrayType(parameter.getType(), typeAdapterRegistry, resolvedPlaceholderValues);
        } else if (metadata.isLHMap()) {
            variableClassType = resolveMapType(parameter, typeAdapterRegistry, resolvedPlaceholderValues);
        } else if (InlineStruct.class.isAssignableFrom(parameter.getType())) {
            variableClassType = new LHStructDefId(structDefName.get());
        } else {
            variableClassType =
                    LHClassType.fromJavaClass(parameter.getType(), typeAdapterRegistry, resolvedPlaceholderValues);
        }

        this.variableDef = VariableDef.newBuilder()
                .setName(variableName)
                .setTypeDef(variableClassType.getTypeDefinition().toBuilder()
                        .setMasked(metadata.isMasked())
                        .build())
                .build();
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

    public Class<?> getParameterType() {
        return parameter.getType();
    }

    private static LHMapType resolveMapType(
            Parameter parameter, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        Type genericType = parameter.getParameterizedType();

        if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length == 2 && typeArgs[0] instanceof Class && typeArgs[1] instanceof Class) {
                return new LHMapType(
                        (Class<?>) typeArgs[0], (Class<?>) typeArgs[1], typeAdapterRegistry, placeholderValues);
            }
        }

        throw new IllegalArgumentException("Map parameter '" + parameter.getName()
                + "' annotated with @LHType(isLHMap = true) must declare generic type parameters "
                + "(e.g. Map<String, Integer>). Raw or wildcard Map types are not supported.");
    }
}
