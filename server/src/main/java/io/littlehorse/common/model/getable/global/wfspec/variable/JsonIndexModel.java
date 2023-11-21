package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.JsonIndex;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
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
    public void initFrom(Message proto) {
        JsonIndex p = (JsonIndex) proto;
        fieldPath = p.getFieldPath();
        fieldType = p.getFieldType();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;

        if (!(other instanceof JsonIndexModel)) {
            return false;
        }

        JsonIndexModel o = (JsonIndexModel) other;
        return Objects.equals(fieldPath, o.getFieldPath()) && Objects.equals(fieldType, o.getFieldType());
    }
}
