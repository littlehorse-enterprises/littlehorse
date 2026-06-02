package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.InlineStructBuilder;
import io.littlehorse.sdk.wfsdk.InlineLHStructBuilder;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

class InlineLHStructBuilderImpl implements InlineLHStructBuilder {

    private final WorkflowThreadImpl thread;
    private final Map<String, Object> fields;

    InlineLHStructBuilderImpl(WorkflowThreadImpl thread) {
        this.thread = thread;
        this.fields = new LinkedHashMap<>();
    }

    @Override
    public InlineLHStructBuilder put(String fieldName, Serializable value) {
        fields.put(fieldName, value);
        return this;
    }

    @Override
    public InlineLHStructBuilder put(String fieldName, InlineLHStructBuilder nested) {
        fields.put(fieldName, nested);
        return this;
    }

    InlineStructBuilder toInlineProto() {
        InlineStructBuilder.Builder out = InlineStructBuilder.newBuilder();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            out.putFields(entry.getKey(), StructBuilderUtils.buildFieldValue(entry.getValue(), thread));
        }
        return out.build();
    }
}
