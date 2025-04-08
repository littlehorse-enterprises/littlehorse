package io.littlehorse.common.model.getable.core.variable;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.proto.VariableMutationType;
import org.junit.jupiter.api.Test;

public class VariableValueModelTest {

    @Test
    public void shouldSupportNullValues() {
        VariableValueModel variableValueModel = new VariableValueModel();
        assertThat(variableValueModel.isNull()).isTrue();
    }

    @Test
    public void shouldAssignNullValueWhenRhsIsNull() throws Exception {
        VariableValueModel lhs = new VariableValueModel("hi there");
        VariableValueModel rhs = new VariableValueModel();
        VariableValueModel variableOutput = lhs.operate(VariableMutationType.ASSIGN, rhs, rhs.getType());
        assertThat(variableOutput.isNull()).isTrue();
    }
}
