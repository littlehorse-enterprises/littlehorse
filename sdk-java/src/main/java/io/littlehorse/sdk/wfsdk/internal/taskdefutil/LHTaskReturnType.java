package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHArrayType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHMapType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefId;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class LHTaskReturnType {

    private final ReturnType returnType;
    private final boolean isMasked;

    public LHTaskReturnType(
            Method method, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        Map<String, String> resolvedPlaceholderValues =
                placeholderValues == null ? Map.of() : Map.copyOf(placeholderValues);

        LHTypeMetadata metadata = LHTypeMetadata.from(method, resolvedPlaceholderValues);
        this.isMasked = metadata.isMasked();

        Class<?> javaType = method.getReturnType();

        metadata.validateStructDefNameUsage(javaType, LHTypeMetadata.ValidationContext.RETURN_TYPE, method.getName());
        metadata.validateLHArrayUsage(javaType, LHTypeMetadata.ValidationContext.RETURN_TYPE, method.getName());
        metadata.validateLHMapUsage(javaType, LHTypeMetadata.ValidationContext.RETURN_TYPE, method.getName());

        LHClassType returnClassType = null;

        if (void.class.isAssignableFrom(javaType) || Void.class.isAssignableFrom(javaType)) {
            returnClassType = null;
        } else if (metadata.isLHArray()) {
            returnClassType = new LHArrayType(javaType, typeAdapterRegistry, resolvedPlaceholderValues);
        } else if (metadata.isLHMap()) {
            returnClassType = resolveMapReturnType(method, typeAdapterRegistry, resolvedPlaceholderValues);
        } else if (InlineStruct.class.isAssignableFrom(javaType)) {
            returnClassType = new LHStructDefId(metadata.getStructDefName().get());
        } else {
            returnClassType = LHClassType.fromJavaClass(javaType, typeAdapterRegistry, resolvedPlaceholderValues);
        }

        if (returnClassType == null) {
            this.returnType = ReturnType.newBuilder().build();
            return;
        }

        this.returnType = ReturnType.newBuilder()
                .setReturnType(returnClassType.getTypeDefinition().toBuilder()
                        .setMasked(isMasked)
                        .build())
                .build();
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    private static LHMapType resolveMapReturnType(
            Method method, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        Type genericType = method.getGenericReturnType();

        if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length == 2 && typeArgs[0] instanceof Class && typeArgs[1] instanceof Class) {
                return new LHMapType(
                        (Class<?>) typeArgs[0], (Class<?>) typeArgs[1], typeAdapterRegistry, placeholderValues);
            }
        }

        throw new IllegalArgumentException("Method '" + method.getName()
                + "' annotated with @LHType(isLHMap = true) must declare a generic Map return type "
                + "(e.g. Map<String, Integer>). Raw or wildcard Map types are not supported.");
    }
}
