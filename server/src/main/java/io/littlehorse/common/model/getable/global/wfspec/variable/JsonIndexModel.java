package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.JsonIndex;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class JsonIndexModel extends LHSerializable<JsonIndex> {
    private String fieldPath;
    private VariableType fieldType;

    @Override
    public Class<JsonIndex> getProtoBaseClass() {
        return JsonIndex.class;
    }

    @Override
    public JsonIndex.Builder toProto() {
        JsonIndex.Builder out = JsonIndex.newBuilder().setFieldPath(fieldPath).setFieldType(fieldType);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        JsonIndex p = (JsonIndex) proto;
        fieldPath = p.getFieldPath();
        fieldType = p.getFieldType();
    }

    public String getFieldPath() {
        return this.fieldPath;
    }

    public VariableType getFieldType() {
        return this.fieldType;
    }

    public JsonIndexModel(final String fieldPath, final VariableType fieldType) {
        this.fieldPath = fieldPath;
        this.fieldType = fieldType;
    }

    public JsonIndexModel() {}

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof JsonIndexModel)) return false;
        final JsonIndexModel other = (JsonIndexModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$fieldPath = this.getFieldPath();
        final Object other$fieldPath = other.getFieldPath();
        if (this$fieldPath == null ? other$fieldPath != null : !this$fieldPath.equals(other$fieldPath)) return false;
        final Object this$fieldType = this.getFieldType();
        final Object other$fieldType = other.getFieldType();
        if (this$fieldType == null ? other$fieldType != null : !this$fieldType.equals(other$fieldType)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof JsonIndexModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $fieldPath = this.getFieldPath();
        result = result * PRIME + ($fieldPath == null ? 43 : $fieldPath.hashCode());
        final Object $fieldType = this.getFieldType();
        result = result * PRIME + ($fieldType == null ? 43 : $fieldType.hashCode());
        return result;
    }
}
