package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.VariableAssignment.SizeOf;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SizeOfModel extends LHSerializable<SizeOf> {

    private VariableAssignmentModel operand;

    @Override
    public Class<SizeOf> getProtoBaseClass() {
        return SizeOf.class;
    }

    @Override
    public SizeOf.Builder toProto() {
        return SizeOf.newBuilder().setOperand(operand.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SizeOf p = (SizeOf) proto;
        operand = VariableAssignmentModel.fromProto(p.getOperand(), context);
    }
}
