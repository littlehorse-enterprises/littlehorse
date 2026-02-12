package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LegacyEdgeCondition;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.wfsdk.LHExpression;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class LHExpressionImpl implements LHExpression {

    private Serializable lhs;
    private Serializable rhs;
    private Operation operation;

    public LHExpressionImpl(Serializable lhs, Operation operation, Serializable rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operation = operation;
    }

    @Override
    public LHExpression add(Serializable other) {
        return new LHExpressionImpl(this, Operation.ADD, other);
    }

    @Override
    public LHExpression subtract(Serializable other) {
        return new LHExpressionImpl(this, Operation.SUBTRACT, other);
    }

    @Override
    public LHExpression multiply(Serializable other) {
        return new LHExpressionImpl(this, Operation.MULTIPLY, other);
    }

    @Override
    public LHExpression divide(Serializable other) {
        return new LHExpressionImpl(this, Operation.DIVIDE, other);
    }

    @Override
    public LHExpression extend(Serializable other) {
        return new LHExpressionImpl(this, Operation.EXTEND, other);
    }

    @Override
    public LHExpression removeIfPresent(Serializable other) {
        return new LHExpressionImpl(this, Operation.REMOVE_IF_PRESENT, other);
    }

    @Override
    public LHExpression removeIndex(int index) {
        return new LHExpressionImpl(this, Operation.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeIndex(LHExpression index) {
        return new LHExpressionImpl(this, Operation.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeKey(Serializable key) {
        return new LHExpressionImpl(this, Operation.REMOVE_KEY, key);
    }

    @Override
    public LHExpression castTo(io.littlehorse.sdk.common.proto.VariableType targetType) {
        return new CastExpressionImpl(this, targetType);
    }

    public LegacyEdgeCondition getLegacySpec() {
        return LegacyEdgeCondition.newBuilder()
                .setLeft(BuilderUtil.assignVariable(lhs))
                .setRight(BuilderUtil.assignVariable(rhs))
                .setComparator(operation)
                .build();
    }

    public LegacyEdgeCondition getReverse() {
        return new WorkflowConditionImpl(getLegacySpec()).getReverse();
    }

    @Override
    public LHExpression or(LHExpression elseExpr) {
        return null;
    }

    @Override
    public LHExpression isLessThan(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isLessThanEq(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isGreaterThanEq(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isGreaterThan(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isEqualTo(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isNotEqualTo(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression doesContain(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression doesNotContain(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isIn(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isNotIn(Serializable rhs) {
        return null;
    }
}
