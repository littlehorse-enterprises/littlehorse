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
        if (comparator == null) {
            throw new RuntimeException("Cannot reverse non-comparator expression!");
        }
        return new LHExpressionImpl(lhs, reverseComparator(comparator), rhs);
    }

    private static Comparator reverseComparator(Comparator comparator) {
        return switch (comparator) {
            case LESS_THAN -> Comparator.GREATER_THAN_EQ;
            case GREATER_THAN -> Comparator.LESS_THAN_EQ;
            case LESS_THAN_EQ -> Comparator.GREATER_THAN;
            case GREATER_THAN_EQ -> Comparator.LESS_THAN;
            case IN -> Comparator.NOT_IN;
            case NOT_IN -> Comparator.IN;
            case EQUALS -> Comparator.NOT_EQUALS;
            case NOT_EQUALS -> Comparator.EQUALS;
            case UNRECOGNIZED -> throw new RuntimeException("Unexpect comparator: " + comparator);
        };
    }
}
