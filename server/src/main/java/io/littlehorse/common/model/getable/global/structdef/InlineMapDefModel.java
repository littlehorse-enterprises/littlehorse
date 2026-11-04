package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineMapDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = false)
public class InlineMapDefModel extends LHSerializable<InlineMapDef> {

    @Getter
    private TypeDefinitionModel keyType;

    @Getter
    private TypeDefinitionModel valueType;

    public InlineMapDefModel() {}

    public InlineMapDefModel(TypeDefinitionModel keyType, TypeDefinitionModel valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public InlineMapDefModel(InlineMapDefModel other) {
        if (other == null) {
            this.keyType = null;
            this.valueType = null;
        } else {
            this.keyType = other.keyType == null ? null : new TypeDefinitionModel(other.keyType);
            this.valueType = other.valueType == null ? null : new TypeDefinitionModel(other.valueType);
        }
    }

    @Override
    public InlineMapDef.Builder toProto() {
        InlineMapDef.Builder out = InlineMapDef.newBuilder();
        if (keyType != null) {
            out.setKeyType(keyType.toProto().build());
        }
        if (valueType != null) {
            out.setValueType(valueType.toProto().build());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        InlineMapDef p = (InlineMapDef) proto;
        keyType = TypeDefinitionModel.fromProto(p.getKeyType(), TypeDefinitionModel.class, context);
        valueType = TypeDefinitionModel.fromProto(p.getValueType(), TypeDefinitionModel.class, context);
    }

    @Override
    public Class<InlineMapDef> getProtoBaseClass() {
        return InlineMapDef.class;
    }

    public static InlineMapDefModel fromProto(InlineMapDef p, ExecutionContext context) {
        InlineMapDefModel out = new InlineMapDefModel();
        out.initFrom(p, context);
        return out;
    }

    @Override
    public String toString() {
        return "Map<" + keyType + ", " + valueType + ">";
    }
}
