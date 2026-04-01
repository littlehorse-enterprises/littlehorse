package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = false)
public class InlineArrayDefModel extends LHSerializable<InlineArrayDef> {

    @Getter
    private TypeDefinitionModel arrayType;

    public InlineArrayDefModel() {}

    public InlineArrayDefModel(TypeDefinitionModel arrayType) {
        this.arrayType = arrayType;
    }

    @Override
    public InlineArrayDef.Builder toProto() {
        InlineArrayDef.Builder out = InlineArrayDef.newBuilder();
        if (arrayType != null) {
            out.setArrayType(arrayType.toProto().build());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        InlineArrayDef p = (InlineArrayDef) proto;
        arrayType = TypeDefinitionModel.fromProto(p.getArrayType(), TypeDefinitionModel.class, context);
    }

    @Override
    public Class<InlineArrayDef> getProtoBaseClass() {
        return InlineArrayDef.class;
    }

    public static InlineArrayDefModel fromProto(InlineArrayDef p, ExecutionContext context) {
        InlineArrayDefModel out = new InlineArrayDefModel();
        out.initFrom(p, context);
        return out;
    }

    @Override
    public String toString() {
        return "Array<" + arrayType.toString() + ">";
    }
}
