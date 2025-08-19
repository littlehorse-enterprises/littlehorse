package io.littlehorse.common.model.getable.core.variable;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class StructModel extends LHSerializable<Struct> {

  private StructDefIdModel structDefId;
  private InlineStructModel inlineStruct;

	@Override
	public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
    Struct p = (Struct) proto;

    this.structDefId = StructDefIdModel.fromProto(p.getStructDefId(), context);
    this.inlineStruct = InlineStructModel.fromProto(p.getStruct(), InlineStructModel.class, context);
	}

	@Override
	public Struct.Builder toProto() {
		Struct.Builder out = Struct.newBuilder();

    out.setStructDefId(structDefId.toProto());
    out.setStruct(inlineStruct.toProto());

    return out;
	}

	@Override
	public Class<Struct> getProtoBaseClass() {
		return Struct.class;
	}
  
}
