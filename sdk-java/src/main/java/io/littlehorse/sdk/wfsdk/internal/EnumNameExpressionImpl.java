package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.exception.LHWfSpecBuilderException;
import io.littlehorse.sdk.wfsdk.LHExpression;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class EnumNameExpressionImpl implements LHExpression {
    private final Serializable operand;

    public EnumNameExpressionImpl(Serializable operand) {
        this.operand = operand;
    }

    @Override
    public LHExpression name() {
        throw new LHWfSpecBuilderException("Cannot use name() on the result of name().");
    }
}
