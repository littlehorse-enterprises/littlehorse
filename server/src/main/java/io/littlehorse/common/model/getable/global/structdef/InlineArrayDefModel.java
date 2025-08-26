package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class InlineArrayDefModel extends LHSerializable<InlineArrayDef> {

    private TypeDefinitionModel elementType;

    @Override
    public InlineArrayDef.Builder toProto() {
        InlineArrayDef.Builder out = InlineArrayDef.newBuilder();

        out.setElementType(elementType.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        InlineArrayDef p = (InlineArrayDef) proto;

        this.elementType = TypeDefinitionModel.fromProto(p.getElementType(), context);
    }

    @Override
    public Class<InlineArrayDef> getProtoBaseClass() {
        return InlineArrayDef.class;
    }

    public static InlineArrayDefModel fromProto(InlineArrayDef proto, ExecutionContext context) {
        InlineArrayDefModel out = new InlineArrayDefModel();
        out.initFrom(proto, context);
        return out;
    }
}
