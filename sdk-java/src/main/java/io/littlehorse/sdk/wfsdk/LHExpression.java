package io.littlehorse.sdk.wfsdk;

import java.io.Serializable;

public interface LHExpression extends Serializable {

    LHExpression add(Serializable other);

    LHExpression subtract(Serializable other);

    LHExpression multiply(Serializable other);

    LHExpression divide(Serializable other);

    LHExpression extend(Serializable other);

    LHExpression removeIfPresent(Serializable other);

    LHExpression removeIndex(int index);

    LHExpression removeIndex(LHExpression index);

    LHExpression removeKey(Serializable key);
}
