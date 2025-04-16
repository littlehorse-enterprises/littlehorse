package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import org.junit.jupiter.api.Test;

public class WfRunVariableImplTest {

    ThreadFunc threadFunction = new ThreadFunc() {
        @Override
        public void threadFunction(WorkflowThread thread) {}
    };

    @Test
    void validateVariableAllowJsonPah() {
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", threadFunction);
        WorkflowThreadImpl wfThread = new WorkflowThreadImpl("wf-thread", workflow, threadFunction);
        WfRunVariableImpl variable = new WfRunVariableImpl("my-var", VariableType.STR, wfThread);

        LHMisconfigurationException e =
                assertThrows(LHMisconfigurationException.class, () -> variable.jsonPath("&.myPath"));
        assertThat(e.getMessage()).isEqualTo("JsonPath not allowed in a STR variable");
    }

    @Test
    void shouldThrowAnExceptionWhenVariableHaveNullParentThread() {
        NullPointerException e =
                assertThrows(NullPointerException.class, () -> new WfRunVariableImpl("my-var", VariableType.STR, null));

        assertEquals("Parent thread cannot be null.", e.getMessage());
    }
}
