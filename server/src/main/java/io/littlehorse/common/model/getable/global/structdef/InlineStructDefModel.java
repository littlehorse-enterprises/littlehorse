package io.littlehorse.common.model.getable.global.structdef;

import java.util.List;
import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class InlineStructDefModel extends LHSerializable<InlineStructDef> {

  private List<StructFieldDefModel> fields;

  @Override
  public InlineStructDef.Builder toProto() {
    InlineStructDef.Builder out = InlineStructDef.newBuilder();

    for (StructFieldDefModel structFieldDef : fields) {
      out.addFields(structFieldDef.toProto());
    }

    return out;
  }

  @Override
  public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
    InlineStructDef proto = (InlineStructDef) p;

    for (StructFieldDef structFieldDef : proto.getFieldsList()) {
      fields.add(LHSerializable.fromProto(structFieldDef, StructFieldDefModel.class, context));
    }
  }

  @Override
  public Class<InlineStructDef> getProtoBaseClass() {
    return InlineStructDef.class;
  }
  
}
