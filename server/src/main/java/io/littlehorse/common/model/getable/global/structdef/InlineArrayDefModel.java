package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

public class InlineArrayDefModel extends LHSerializable<InlineArrayDef> {

  public InlineArrayDefModel() {}
  
  public InlineArrayDefModel(TypeDefinitionModel arrayType) {
    this.arrayType = arrayType;
  }

  @Getter
  private TypeDefinitionModel arrayType;

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
}
