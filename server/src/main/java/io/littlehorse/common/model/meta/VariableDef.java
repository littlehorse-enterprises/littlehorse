package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.sdk.common.proto.IndexTypePb;
import io.littlehorse.sdk.common.proto.JsonIndexPb;
import io.littlehorse.sdk.common.proto.VariableDefPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariableDef extends LHSerializable<VariableDefPb> {

    public VariableTypePb type;
    public String name;

    public ThreadSpec threadSpec;
    private IndexTypePb indexType;
    private List<JsonIndex> jsonIndices = new ArrayList<>();
    private VariableValue defaultValue;

    public Class<VariableDefPb> getProtoBaseClass() {
        return VariableDefPb.class;
    }

    public void initFrom(Message proto) {
        VariableDefPb p = (VariableDefPb) proto;
        type = p.getType();
        name = p.getName();

        for (JsonIndexPb idx : p.getJsonIndexesList()) {
            jsonIndices.add(LHSerializable.fromProto(idx, JsonIndex.class));
        }

        if (p.hasIndexType()) indexType = p.getIndexType();

        if (p.hasDefaultValue()) {
            defaultValue = VariableValue.fromProto(p.getDefaultValue());
        }
    }

    public VariableDefPb.Builder toProto() {
        VariableDefPb.Builder out = VariableDefPb
            .newBuilder()
            .setType(type)
            .setName(name);

        if (defaultValue != null) out.setDefaultValue(defaultValue.toProto());
        if (indexType != null) out.setIndexType(indexType);

        for (JsonIndex idx : jsonIndices) {
            out.addJsonIndexes(idx.toProto());
        }

        return out;
    }

    public static VariableDef fromProto(VariableDefPb proto) {
        VariableDef o = new VariableDef();
        o.initFrom(proto);
        return o;
    }

    public TagStorageTypePb getTagStorageType() {
        if (indexType == null) return null;

        return indexType == IndexTypePb.LOCAL_INDEX
            ? TagStorageTypePb.LOCAL
            : TagStorageTypePb.REMOTE;
    }
}
