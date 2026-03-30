package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefId;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHTaskParameter {

    private final Parameter parameter;

    @Getter
    private final LHClassType variableClassType;

    @Getter
    private final String variableName;

    private final boolean isMasked;

    private VariableDef variableDef;

    public LHTaskParameter(
            Parameter parameter, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        this.parameter = parameter;
        Map<String, String> resolvedPlaceholderValues =
                placeholderValues == null ? Map.of() : Map.copyOf(placeholderValues);

        LHTypeMetadata metadata = LHTypeMetadata.from(parameter, resolvedPlaceholderValues);
        Optional<String> structDefName = metadata.getStructDefName();

        this.variableName = metadata.getName().orElseGet(this::getVarNameFromParameterName);
        this.isMasked = metadata.isMasked();

        metadata.validateStructDefNameUsage(
                parameter.getType(), LHTypeMetadata.ValidationContext.PARAMETER, parameter.getName());

        this.variableClassType = buildVariableClassType(typeAdapterRegistry, structDefName);
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

    private LHClassType buildVariableClassType(
            LHTypeAdapterRegistry typeAdapterRegistry, Optional<String> structDefName) {
        if (InlineStruct.class.isAssignableFrom(parameter.getType())) {
            return new LHStructDefId(structDefName.get());
        }

        return LHClassType.fromJavaClass(parameter.getType(), typeAdapterRegistry);
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
