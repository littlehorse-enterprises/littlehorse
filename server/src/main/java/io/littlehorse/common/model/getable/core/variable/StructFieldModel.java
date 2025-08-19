package io.littlehorse.common.model.getable.core.variable;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.StructField.StructValueCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class StructFieldModel extends LHSerializable<StructField> {
  private StructValueCase structValueCase;

  private VariableValueModel variableValue;
  private InlineStructModel inlineStructValue;

	@Override
	public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
		StructField p = (StructField) proto;

    this.structValueCase = p.getStructValueCase();
    
    switch (structValueCase) {
			case PRIMITIVE:
        this.variableValue = VariableValueModel.fromProto(p.getPrimitive(), context);
				break;
			case STRUCT:
        this.inlineStructValue = InlineStructModel.fromProto(p.getStruct(), InlineStructModel.class, context);
				break;
			case STRUCTVALUE_NOT_SET:
			case LIST:
			default:
				break;
    }
	}

	@Override
	public StructField.Builder toProto() {
		StructField.Builder out = StructField.newBuilder();

    switch (structValueCase) {
			case PRIMITIVE:
        out.setPrimitive(variableValue.toProto());
				break;
			case STRUCT:
        out.setStruct(inlineStructValue.toProto());
        break;
			case STRUCTVALUE_NOT_SET:
			case LIST:
			default:
				break;
    }

    return out;
	}

	@Override
	public Class<StructField> getProtoBaseClass() {
		return StructField.class;
	}
  
}
