package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class InlineArrayDefModel extends LHSerializable<InlineArrayDef> {
    private TypeDefinitionModel arrayType;

    public InlineArrayDefModel() {}

    public InlineArrayDefModel(TypeDefinitionModel arrayType) {
        this.arrayType = arrayType;
    }

    public InlineArrayDefModel(InlineArrayDefModel other) {
        if (other == null || other.arrayType == null) {
            this.arrayType = null;
        } else {
            this.arrayType = new TypeDefinitionModel(other.arrayType);
        }
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

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof InlineArrayDefModel)) return false;
        final InlineArrayDefModel other = (InlineArrayDefModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$arrayType = this.getArrayType();
        final Object other$arrayType = other.getArrayType();
        if (this$arrayType == null ? other$arrayType != null : !this$arrayType.equals(other$arrayType)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof InlineArrayDefModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $arrayType = this.getArrayType();
        result = result * PRIME + ($arrayType == null ? 43 : $arrayType.hashCode());
        return result;
    }

    public TypeDefinitionModel getArrayType() {
        return this.arrayType;
    }
}
