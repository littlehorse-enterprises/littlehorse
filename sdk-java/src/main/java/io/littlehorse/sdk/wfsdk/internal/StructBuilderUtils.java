package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.InlineStructBuilder;
import io.littlehorse.sdk.common.proto.InlineStructFieldValue;

/**
 * Shared logic for building {@link InlineStructFieldValue} protos from field maps,
 * used by both {@link LHStructBuilderImpl} and {@link InlineLHStructBuilderImpl}.
 */
final class StructBuilderUtils {

    private StructBuilderUtils() {}

    static InlineStructFieldValue buildFieldValue(Object value, WorkflowThreadImpl thread) {
        if (value instanceof InlineLHStructBuilderImpl) {
            InlineLHStructBuilderImpl nested = (InlineLHStructBuilderImpl) value;
            return InlineStructFieldValue.newBuilder()
                    .setSubStructure(nested.toInlineProto())
                    .build();
        }

        return InlineStructFieldValue.newBuilder()
                .setSimpleValue(
                        BuilderUtil.assignVariable(value, thread.getParent().getTypeAdapterRegistry()))
                .build();
    }

    static InlineStructBuilder buildInlineProto(java.util.Map<String, Object> fields, WorkflowThreadImpl thread) {
        InlineStructBuilder.Builder out = InlineStructBuilder.newBuilder();
        for (java.util.Map.Entry<String, Object> entry : fields.entrySet()) {
            out.putFields(entry.getKey(), buildFieldValue(entry.getValue(), thread));
        }
        return out.build();
    }
}
