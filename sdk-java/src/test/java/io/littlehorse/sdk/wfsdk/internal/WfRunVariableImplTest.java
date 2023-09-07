package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.VariableType;
import org.junit.jupiter.api.Test;

public class WfRunVariableImplTest {
    @Test
    void validateVariableAllowJsonPah() {
        WfRunVariableImpl variable = new WfRunVariableImpl("my-var", VariableType.STR);

        LHMisconfigurationException e =
                assertThrows(LHMisconfigurationException.class, () -> variable.jsonPath("&.myPath"));
        assertThat(e.getMessage()).isEqualTo("JsonPath not allowed in a STR variable");
    }
}
