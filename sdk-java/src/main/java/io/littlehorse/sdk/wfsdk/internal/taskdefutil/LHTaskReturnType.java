package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefId;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

public class LHTaskReturnType {

    @Getter
    private final Optional<LHClassType> returnClassType;

    private ReturnType returnType;

    private final boolean isMasked;

    public LHTaskReturnType(
            Method method, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        Map<String, String> resolvedPlaceholderValues =
                placeholderValues == null ? Map.of() : Map.copyOf(placeholderValues);

        LHTypeMetadata metadata = LHTypeMetadata.from(method, resolvedPlaceholderValues);
        this.isMasked = metadata.isMasked();

        metadata.validateStructDefNameUsage(
                method.getReturnType(), LHTypeMetadata.ValidationContext.RETURN_TYPE, method.getName());

        this.returnClassType =
                buildVariableClassType(method.getReturnType(), typeAdapterRegistry, metadata.getStructDefName());
    }

    private Optional<LHClassType> buildVariableClassType(
            Class<?> javaType, LHTypeAdapterRegistry typeAdapterRegistry, Optional<String> structDefName) {
        if (void.class.isAssignableFrom(javaType)) {
            return Optional.empty();
        } else if (InlineStruct.class.isAssignableFrom(javaType)) {
            return Optional.of(new LHStructDefId(structDefName.get()));
        } else {
            return Optional.of(LHClassType.fromJavaClass(javaType, typeAdapterRegistry));
        }
    }

    private void buildReturnType() {
        if (!returnClassType.isPresent()) {
            returnType = ReturnType.newBuilder().build();
            return;
        }

        returnType = ReturnType.newBuilder()
                .setReturnType(returnClassType.get().getTypeDefinition().toBuilder()
                        .setMasked(isMasked)
                        .build())
                .build();
    }

    public ReturnType getReturnType() {
        if (returnType == null) {
            buildReturnType();
        }

        return returnType;
    }
}
