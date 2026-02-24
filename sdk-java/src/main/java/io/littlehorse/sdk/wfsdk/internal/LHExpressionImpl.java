package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LegacyEdgeCondition;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.wfsdk.LHExpression;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class LHExpressionImpl implements LHExpression {

    private final Serializable lhs;
    private final Serializable rhs;
    private final VariableMutationType operation;
    private final Comparator comparator;

    public LHExpressionImpl(Serializable lhs, VariableMutationType operation, Serializable rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operation = operation;
        this.comparator = null;
    }

    public LHExpressionImpl(Serializable lhs, Comparator comparator, Serializable rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operation = null;
        this.comparator = comparator;
    }

    public LegacyEdgeCondition getLegacyCondition() {
        return LegacyEdgeCondition.newBuilder()
                .setLeft(BuilderUtil.assignVariable(lhs))
                .setRight(BuilderUtil.assignVariable(rhs))
                .setComparator(comparator)
                .build();
    }

    public VariableAssignment getCondition() {
        VariableAssignment.Expression.Builder condition = VariableAssignment.Expression.newBuilder();
        condition.setLhs(BuilderUtil.assignVariable(lhs));
        condition.setRhs(BuilderUtil.assignVariable(rhs));
        if (comparator != null) {
            condition.setComparator(comparator);
        } else {
            condition.setMutationType(operation);
        }
        return VariableAssignment.newBuilder().setExpression(condition).build();
    }

    public LHExpression getReverse() {
        LegacyEdgeCondition legacyCondition = new WorkflowConditionImpl(getLegacyCondition()).getReverse();
        return new LHExpressionImpl(
                legacyCondition.getLeft(), legacyCondition.getComparator(), legacyCondition.getRight());
    }
}
