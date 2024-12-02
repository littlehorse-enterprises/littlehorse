package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.LHExpression;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
public class LHExpressionImpl implements LHExpression {

    private Serializable lhs;
    private Serializable rhs;
    private VariableMutationType operation;

    @Setter
    private VariableType typeToCastTo;

    public LHExpressionImpl(Serializable lhs, VariableMutationType operation, Serializable rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operation = operation;
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
    public LHExpression asStr() {
        this.typeToCastTo = VariableType.STR;
        return this;
    }

    @Override
    public LHExpression asInt() {
        this.typeToCastTo = VariableType.INT;
        return this;
    }

    @Override
    public LHExpression asDouble() {
        this.typeToCastTo = VariableType.DOUBLE;
        return this;
    }
}
