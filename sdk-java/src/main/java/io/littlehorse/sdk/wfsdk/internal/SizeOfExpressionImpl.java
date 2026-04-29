package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.wfsdk.LHExpression;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class SizeOfExpressionImpl implements LHExpression {

    private final Serializable operand;

    public SizeOfExpressionImpl(Serializable operand) {
        this.operand = operand;
    }
}
