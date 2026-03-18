package io.littlehorse.common.model.getable.core.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import lombok.Getter;
import lombok.Setter;

public class StructFieldModel extends LHSerializable<StructField> {
    @Getter
    @Setter
    private VariableValueModel value;

    @Getter
    @Setter
    private boolean masked;

    private ExecutionContext context;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        StructField p = (StructField) proto;

        this.context = context;
        this.value = VariableValueModel.fromProto(p.getValue(), context);
        this.masked = p.getMasked();
    }

    @Override
    public StructField.Builder toProto() {
        StructField.Builder out = StructField.newBuilder().setMasked(masked);

        if (masked && context != null && context.support(RequestExecutionContext.class)) {
            out.setValue(new VariableValueModel(LHConstants.STRING_MASK).toProto());
        } else {
            out.setValue(value.toProto());
        }

        return out;
    }

    @Override
    public Class<StructField> getProtoBaseClass() {
        return StructField.class;
    }
}
