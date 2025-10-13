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
    void validateVariableAllowJsonPath() {
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", threadFunction);
        WorkflowThreadImpl wfThread = new WorkflowThreadImpl("wf-thread", workflow, threadFunction);
        WfRunVariableImpl variable = WfRunVariableImpl.createPrimitiveVar("my-var", VariableType.STR, wfThread);

        LHMisconfigurationException e =
                assertThrows(LHMisconfigurationException.class, () -> variable.jsonPath("&.myPath"));
        assertThat(e.getMessage()).isEqualTo("JsonPath not allowed in a STR variable");
    }

    @Test
    void validateStringVarDoesNotAllowGet() {
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", threadFunction);
        WorkflowThreadImpl wfThread = new WorkflowThreadImpl("wf-thread", workflow, threadFunction);
        WfRunVariableImpl variable = WfRunVariableImpl.createPrimitiveVar("str-var", VariableType.STR, wfThread);

        LHMisconfigurationException e = assertThrows(LHMisconfigurationException.class, () -> variable.get("model"));
        assertThat(e.getMessage()).isEqualTo("Can only use get() on JSON_OBJ or Struct variables");
    }

    @Test
    void shouldThrowAnExceptionWhenVariableHaveNullParentThread() {
        NullPointerException e = assertThrows(
                NullPointerException.class,
                () -> WfRunVariableImpl.createPrimitiveVar("my-var", VariableType.STR, null));

        assertEquals("Parent thread cannot be null.", e.getMessage());
    }
}
