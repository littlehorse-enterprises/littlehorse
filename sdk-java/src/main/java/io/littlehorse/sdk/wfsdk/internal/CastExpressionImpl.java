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
public class CastExpressionImpl implements LHExpression {
    private final LHExpression source;
    private final VariableType targetType;

    public CastExpressionImpl(LHExpression source, VariableType targetType) {
        this.source = source;
        this.targetType = targetType;
    }
}
