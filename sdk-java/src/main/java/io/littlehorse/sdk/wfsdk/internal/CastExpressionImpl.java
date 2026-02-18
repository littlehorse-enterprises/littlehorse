package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.LHExpression;
import java.io.Serializable;
import lombok.Getter;

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
        return new LHExpressionImpl(this, VariableMutationType.ADD, other);
    }

    @Override
    public LHExpression subtract(java.io.Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.SUBTRACT, other);
    }

    @Override
    public LHExpression multiply(java.io.Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.MULTIPLY, other);
    }

    @Override
    public LHExpression divide(java.io.Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.DIVIDE, other);
    }

    @Override
    public LHExpression extend(java.io.Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.EXTEND, other);
    }

    @Override
    public LHExpression removeIfPresent(java.io.Serializable other) {
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
    public LHExpression removeKey(java.io.Serializable key) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_KEY, key);
    }

    @Override
    public LHExpression castTo(VariableType targetType) {
        return new CastExpressionImpl(this, targetType);
    }

    @Override
    public LHExpression isLessThan(Serializable other) {
        return null;
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
}
