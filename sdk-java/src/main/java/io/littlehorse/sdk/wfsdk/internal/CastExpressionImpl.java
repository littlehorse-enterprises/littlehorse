package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.LHExpression;
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
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.VariableMutationType.ADD, other);
    }

    @Override
    public LHExpression subtract(java.io.Serializable other) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.VariableMutationType.SUBTRACT, other);
    }

    @Override
    public LHExpression multiply(java.io.Serializable other) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.VariableMutationType.MULTIPLY, other);
    }

    @Override
    public LHExpression divide(java.io.Serializable other) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.VariableMutationType.DIVIDE, other);
    }

    @Override
    public LHExpression extend(java.io.Serializable other) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.VariableMutationType.EXTEND, other);
    }

    @Override
    public LHExpression removeIfPresent(java.io.Serializable other) {
        return new LHExpressionImpl(
                this, io.littlehorse.sdk.common.proto.VariableMutationType.REMOVE_IF_PRESENT, other);
    }

    @Override
    public LHExpression removeIndex(int index) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.VariableMutationType.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeIndex(LHExpression index) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.VariableMutationType.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeKey(java.io.Serializable key) {
        return new LHExpressionImpl(this, io.littlehorse.sdk.common.proto.VariableMutationType.REMOVE_KEY, key);
    }
}
