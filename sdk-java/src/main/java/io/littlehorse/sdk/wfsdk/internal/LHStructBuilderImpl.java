package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.StructBuilder;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.wfsdk.InlineLHStructBuilder;
import io.littlehorse.sdk.wfsdk.LHStructBuilder;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

class LHStructBuilderImpl implements LHStructBuilder {

    private final WorkflowThreadImpl thread;
    private final String structDefName;
    private final Integer version;
    private final Map<String, Object> fields;

    LHStructBuilderImpl(WorkflowThreadImpl thread, String structDefName) {
        this(thread, structDefName, null);
    }

    LHStructBuilderImpl(WorkflowThreadImpl thread, String structDefName, Integer version) {
        this.thread = thread;
        this.structDefName = structDefName;
        this.version = version;
        this.fields = new LinkedHashMap<>();
    }

    @Override
    public LHStructBuilder put(String fieldName, Serializable value) {
        fields.put(fieldName, value);
        return this;
    }

    @Override
    public LHStructBuilder put(String fieldName, InlineLHStructBuilder nested) {
        fields.put(fieldName, nested);
        return this;
    }

    StructBuilder toProto() {
        return StructBuilder.newBuilder()
                .setStructDefId(StructDefId.newBuilder()
                        .setName(structDefName)
                        .setVersion(version == null ? -1 : version)
                        .build())
                .setValue(StructBuilderUtils.buildInlineProto(fields, thread))
                .build();
    }
}
