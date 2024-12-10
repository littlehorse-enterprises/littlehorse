package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.VariableAssignerFunc;
import io.littlehorse.sdk.common.proto.VariableAssignment.Expression;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class ExpressionModel extends LHSerializable<Expression> {

    private VariableAssignmentModel lhs;
    private VariableAssignmentModel rhs;
    private VariableMutationType operation;

    @Override
    public Class<Expression> getProtoBaseClass() {
        return Expression.class;
    }

    @Override
    public Expression.Builder toProto() {
        return Expression.newBuilder()
                .setOperation(operation)
                .setLhs(lhs.toProto())
                .setRhs(rhs.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        Expression p = (Expression) proto;
        lhs = LHSerializable.fromProto(p.getLhs(), VariableAssignmentModel.class, ignored);
        rhs = LHSerializable.fromProto(p.getRhs(), VariableAssignmentModel.class, ignored);
        operation = p.getOperation();
    }

    public VariableValueModel evaluate(VariableAssignerFunc variableFinder) throws LHVarSubError {
        VariableValueModel lhsVal = variableFinder.assign(lhs);
        VariableValueModel rhsVal = variableFinder.assign(rhs);

        VariableType typeToCoerceTo = lhsVal.getType();

        if (lhsVal.getType() == VariableType.INT && rhsVal.getType() == VariableType.DOUBLE) {
            typeToCoerceTo = VariableType.DOUBLE;
        }

        return lhsVal.operate(operation, rhsVal, typeToCoerceTo);
    }
}
