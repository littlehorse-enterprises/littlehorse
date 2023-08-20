package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.IndexType;
import io.littlehorse.sdk.common.proto.JsonIndex;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariableDefModel extends LHSerializable<VariableDef> {

    public VariableType type;
    public String name;

    public ThreadSpecModel threadSpecModel;
    private IndexType indexType;
    private List<JsonIndexModel> jsonIndices = new ArrayList<>();
    private VariableValueModel defaultValue;

    public Class<VariableDef> getProtoBaseClass() {
        return VariableDef.class;
    }

    public void initFrom(Message proto) {
        VariableDef p = (VariableDef) proto;
        type = p.getType();
        name = p.getName();

        for (JsonIndex idx : p.getJsonIndexesList()) {
            jsonIndices.add(LHSerializable.fromProto(idx, JsonIndexModel.class));
        }

        if (p.hasIndexType()) indexType = p.getIndexType();

        if (p.hasDefaultValue()) {
            defaultValue = VariableValueModel.fromProto(p.getDefaultValue());
        }
    }

    public VariableDef.Builder toProto() {
        VariableDef.Builder out = VariableDef.newBuilder().setType(type).setName(name);

        if (defaultValue != null) out.setDefaultValue(defaultValue.toProto());
        if (indexType != null) out.setIndexType(indexType);

        for (JsonIndexModel idx : jsonIndices) {
            out.addJsonIndexes(idx.toProto());
        }

        return out;
    }

    public static VariableDefModel fromProto(VariableDef proto) {
        VariableDefModel o = new VariableDefModel();
        o.initFrom(proto);
        return o;
    }

    public TagStorageType getTagStorageType() {
        if (indexType == null) return null;

        return indexType == IndexType.LOCAL_INDEX ? TagStorageType.LOCAL : TagStorageType.REMOTE;
    }
}
