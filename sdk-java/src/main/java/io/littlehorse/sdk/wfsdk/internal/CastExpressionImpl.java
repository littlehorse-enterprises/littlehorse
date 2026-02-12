package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.LHExpression;
import lombok.Getter;

import java.io.Serializable;

/**
 * Represents a cast expression that converts a value from one type to another.
 * This is used for manual type casting in workflows.
 */
@Getter
class CastExpressionImpl implements LHExpression {
    private final LHExpression source;
    private final VariableType targetType;

    public CastExpressionImpl(LHExpression source, VariableType targetType) {
        this.source = source;
        this.targetType = targetType;
    }

    // Delegate all LHExpression methods to create new LHExpressionImpl instances
    @Override
    public LHExpression add(java.io.Serializable other) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.Operation.ADD, other);
    }

    @Override
    public LHExpression subtract(java.io.Serializable other) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.Operation.SUBTRACT, other);
    }

    @Override
    public LHExpression multiply(java.io.Serializable other) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.Operation.MULTIPLY, other);
    }

    @Override
    public LHExpression divide(java.io.Serializable other) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.Operation.DIVIDE, other);
    }

    @Override
    public LHExpression extend(java.io.Serializable other) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.Operation.EXTEND, other);
    }

    @Override
    public LHExpression removeIfPresent(java.io.Serializable other) {
        return new LHExpressionImpl(
                this, io.littlehorse.sdk.common.proto.Operation.REMOVE_IF_PRESENT, other);
    }

    @Override
    public LHExpression removeIndex(int index) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.Operation.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeIndex(LHExpression index) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.Operation.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeKey(java.io.Serializable key) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.Operation.REMOVE_KEY, key);
    }

    @Override
    public LHExpression castTo(VariableType targetType) {
        return new CastExpressionImpl(this, targetType);
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
