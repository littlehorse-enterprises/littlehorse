package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.worker.LHType;
import io.littlehorse.sdk.worker.internal.util.PlaceholderUtil;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public class LHTaskReturnType {
    private final Method method;
    private final Map<String, String> placeholderValues;
    private Optional<LHClassType> returnClassType;
    private ReturnType returnType;

    public LHTaskReturnType(
            Method method, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        this.method = method;
        this.placeholderValues = placeholderValues == null ? Map.of() : Map.copyOf(placeholderValues);

        if (void.class.isAssignableFrom(method.getReturnType())) {
            returnClassType = Optional.empty();
        } else {
            returnClassType = Optional.of(LHClassType.fromJavaClass(method.getReturnType(), typeAdapterRegistry));
        }
    }

    private String getInlineStructDefNameFromAnnotation() {
        LHType typeAnnotation = method.getAnnotation(LHType.class);
        if (typeAnnotation == null || typeAnnotation.structDefName().isBlank()) {
            throw new TaskSchemaMismatchError(
                    "Returns of type InlineStruct must declare @LHType(structDefName = \"...\").");
        }

        return PlaceholderUtil.replacePlaceholders(typeAnnotation.structDefName(), placeholderValues);
    }

    private void buildReturnType() {
        if (!returnClassType.isPresent()) {
            returnType = ReturnType.newBuilder().build();
            return;
        }

        if (InlineStruct.class.isAssignableFrom(method.getReturnType())) {
            String structDefName = getInlineStructDefNameFromAnnotation();
            returnType = ReturnType.newBuilder()
                    .setReturnType(TypeDefinition.newBuilder()
                            .setStructDefId(StructDefId.newBuilder().setName(structDefName)))
                    .build();
            return;
        }

        returnType = ReturnType.newBuilder()
                .setReturnType(returnClassType.get().getTypeDefinition())
                .build();
    }

    public ReturnType getReturnType() {
        if (returnType == null) {
            buildReturnType();
        }

        return returnType;
    }
}
