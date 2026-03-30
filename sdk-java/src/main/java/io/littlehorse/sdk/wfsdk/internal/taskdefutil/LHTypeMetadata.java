package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.worker.LHType;
import io.littlehorse.sdk.worker.internal.util.PlaceholderUtil;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Optional;

final class LHTypeMetadata {

    enum ValidationContext {
        PARAMETER,
        RETURN_TYPE
    }

    private final boolean masked;
    private final Optional<String> name;
    private final Optional<String> structDefName;

    private LHTypeMetadata(boolean masked, Optional<String> name, Optional<String> structDefName) {
        this.masked = masked;
        this.name = name;
        this.structDefName = structDefName;
    }

    static LHTypeMetadata from(AnnotatedElement element, Map<String, String> placeholderValues) {
        Map<String, String> placeholders = placeholderValues == null ? Map.of() : placeholderValues;

        if (!element.isAnnotationPresent(LHType.class)) {
            return new LHTypeMetadata(false, Optional.empty(), Optional.empty());
        }

        LHType typeAnnotation = element.getAnnotation(LHType.class);

        Optional<String> parsedName = normalize(typeAnnotation.name());
        Optional<String> parsedStructDefName = normalize(typeAnnotation.structDefName())
                .map(value -> PlaceholderUtil.replacePlaceholders(value, placeholders));

        return new LHTypeMetadata(typeAnnotation.masked(), parsedName, parsedStructDefName);
    }

    boolean isMasked() {
        return masked;
    }

    Optional<String> getName() {
        return name;
    }

    Optional<String> getStructDefName() {
        return structDefName;
    }

    void validateStructDefNameUsage(Class<?> javaType, ValidationContext context, String contextName) {
        boolean isInlineStruct = InlineStruct.class.isAssignableFrom(javaType);

        if (isInlineStruct && structDefName.isEmpty()) {
            throw new TaskSchemaMismatchError(buildMissingStructDefNameMessage(context, contextName, javaType));
        }

        if (!isInlineStruct && structDefName.isPresent()) {
            throw new TaskSchemaMismatchError(buildUnexpectedStructDefNameMessage(context, contextName, javaType));
        }
    }

    private static Optional<String> normalize(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    private static String buildMissingStructDefNameMessage(
            ValidationContext context, String contextName, Class<?> javaType) {
        if (context == ValidationContext.PARAMETER) {
            return "InlineStruct parameters must declare @LHType(structDefName = \"...\"). Parameter "
                    + contextName
                    + " of type "
                    + javaType.getName()
                    + " did not.";
        }

        return "Methods that return InlineStruct must declare @LHType(structDefName = \"...\"). Method "
                + contextName
                + " returns "
                + javaType.getName()
                + " but did not.";
    }

    private static String buildUnexpectedStructDefNameMessage(
            ValidationContext context, String contextName, Class<?> javaType) {
        String noun = context == ValidationContext.PARAMETER ? "parameter " : "return type for method ";

        return "@LHType(structDefName = ...) can only be used on InlineStruct parameters and return types. "
                + "Invalid "
                + noun
                + contextName
                + " with Java type "
                + javaType.getName()
                + ".";
    }
}
