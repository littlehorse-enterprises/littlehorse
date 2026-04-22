package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.InlineStructBuilder;
import io.littlehorse.sdk.common.proto.InlineStructFieldValue;
import io.littlehorse.sdk.common.proto.StructBuilder;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.wfsdk.LHStructBuilder;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

class LHStructBuilderImpl implements LHStructBuilder {

    private final WorkflowThreadImpl thread;
    private final String structDefName;
    private Integer version;
    private final Map<String, Serializable> fields;
    private final boolean inlineOnly;

    LHStructBuilderImpl(WorkflowThreadImpl thread, String structDefName, boolean inlineOnly) {
        this.thread = thread;
        this.structDefName = structDefName;
        this.inlineOnly = inlineOnly;
        this.fields = new LinkedHashMap<>();
    }

    @Override
    public LHStructBuilder put(String fieldName, Serializable value) {
        fields.put(fieldName, value);
        return this;
    }

    @Override
    public LHStructBuilder withVersion(int version) {
        if (inlineOnly) {
            throw new IllegalStateException("Inline Struct builders do not support version pinning");
        }
        this.version = version;
        return this;
    }

    StructBuilder toProto() {
        if (inlineOnly) {
            throw new IllegalStateException("Inline Struct builders do not have a top-level StructDefId");
        }

        return StructBuilder.newBuilder()
                .setStructDefId(StructDefId.newBuilder()
                        .setName(structDefName)
                        .setVersion(version == null ? -1 : version)
                        .build())
                .setValue(toInlineProto())
                .build();
    }

    InlineStructBuilder toInlineProto() {
        InlineStructBuilder.Builder out = InlineStructBuilder.newBuilder();
        for (Map.Entry<String, Serializable> entry : fields.entrySet()) {
            out.putFields(entry.getKey(), buildFieldValue(entry.getValue()));
        }
        return out.build();
    }

    private InlineStructFieldValue buildFieldValue(Serializable value) {
        if (value instanceof LHStructBuilderImpl) {
            LHStructBuilderImpl nested = (LHStructBuilderImpl) value;
            if (nested.inlineOnly) {
                return InlineStructFieldValue.newBuilder()
                        .setSubStructure(nested.toInlineProto())
                        .build();
            }

            return InlineStructFieldValue.newBuilder()
                    .setSimpleValue(BuilderUtil.assignVariable(
                            nested, thread.getParent().getTypeAdapterRegistry()))
                    .build();
        }

        return InlineStructFieldValue.newBuilder()
                .setSimpleValue(
                        BuilderUtil.assignVariable(value, thread.getParent().getTypeAdapterRegistry()))
                .build();
    }
}
