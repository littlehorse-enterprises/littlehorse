package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
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
    private boolean persistent;

    public Class<VariableDef> getProtoBaseClass() {
        return VariableDef.class;
    }

    public void initFrom(Message proto) {
        VariableDef p = (VariableDef) proto;
        type = p.getType();
        name = p.getName();
        persistent = p.getPersistent();

        for (JsonIndex idx : p.getJsonIndexesList()) {
            jsonIndices.add(LHSerializable.fromProto(idx, JsonIndexModel.class));
        }

        if (p.hasIndexType()) indexType = p.getIndexType();

        if (p.hasDefaultValue()) {
            defaultValue = VariableValueModel.fromProto(p.getDefaultValue());
        }
    }

    public VariableDef.Builder toProto() {
        VariableDef.Builder out =
                VariableDef.newBuilder().setType(type).setName(name).setPersistent(persistent);

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

    public boolean isJson() {
        return type == VariableType.JSON_ARR || type == VariableType.JSON_OBJ;
    }

    public boolean isCompatibleWith(VariableDefModel oldVar) {
        if (type != oldVar.getType()) return false;

        // Next must validate index configs
        if (oldVar.isJson()) {

            // Validate that every old json index is present in the new one.
            for (JsonIndexModel idx : oldVar.getJsonIndices()) {
                if (!(getJsonIndices().contains(idx))) {
                    return false;
                }
            }

        } else {
            // If not a json index, just need to make sure that the LOCAL vs REMOTE is the same.
            if (oldVar.getIndexType() != this.getIndexType()) {
                return false;
            }
        }

        return true;
    }

    public boolean hasIndex() {
        return indexType != null || jsonIndices.size() > 0;
    }
}
