package io.littlehorse.common.model.getable.core.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

public class StructFieldModel extends LHSerializable<StructField> {
    @Getter
    private VariableValueModel value;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        StructField p = (StructField) proto;

        this.value = VariableValueModel.fromProto(p.getValue(), context);
    }

    @Override
    public StructField.Builder toProto() {
        StructField.Builder out = StructField.newBuilder().setValue(value.toProto());

        return out;
    }

    @Override
    public Class<StructField> getProtoBaseClass() {
        return StructField.class;
    }
}
