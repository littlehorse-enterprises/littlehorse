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

    @Override
    public LHExpression add(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.ADD, other);
    }

    @Override
    public LHExpression subtract(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.SUBTRACT, other);
    }

    @Override
    public LHExpression multiply(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.MULTIPLY, other);
    }

    @Override
    public LHExpression divide(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.DIVIDE, other);
    }

    @Override
    public LHExpression extend(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.EXTEND, other);
    }

    @Override
    public LHExpression removeIfPresent(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_IF_PRESENT, other);
    }

    @Override
    public LHExpression removeIndex(int index) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeIndex(LHExpression index) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeKey(Serializable key) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_KEY, key);
    }

    @Override
    public LHExpression castTo(io.littlehorse.sdk.common.proto.VariableType targetType) {
        return new CastExpressionImpl(this, targetType);
    }

    @Override
    public LHExpression isLessThan(Serializable other) {
        return new LHExpressionImpl(this, Comparator.LESS_THAN, other);
    }

    @Override
    public LHExpression isGreaterThan(Serializable other) {
        return null;
    }

    @Override
    public LHExpression isEqualTo(Serializable other) {
        return null;
    }

    @Override
    public LHExpression isNotEqualTo(Serializable other) {
        return null;
    }

    @Override
    public LHExpression doesContain(Serializable other) {
        return null;
    }

    @Override
    public LHExpression doesNotContain(Serializable other) {
        return null;
    }

    @Override
    public LHExpression isIn(Serializable other) {
        return null;
    }

    @Override
    public LHExpression isNotIn(Serializable other) {
        return null;
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
        condition.setComparator(comparator);
        return VariableAssignment.newBuilder().setExpression(condition).build();
    }

    public LHExpression getReverse() {
        LegacyEdgeCondition legacyCondition = new WorkflowConditionImpl(getLegacyCondition()).getReverse();
        return new LHExpressionImpl(
                legacyCondition.getLeft(), legacyCondition.getComparator(), legacyCondition.getRight());
    }
}
