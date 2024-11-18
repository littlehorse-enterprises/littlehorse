package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.JsonIndex;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
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
}
