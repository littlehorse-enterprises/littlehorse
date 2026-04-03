package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
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
    void shouldThrowAnExceptionWhenVariableHaveNullParentThread() {
        NullPointerException e = assertThrows(
                NullPointerException.class,
                () -> WfRunVariableImpl.createPrimitiveVar("my-var", VariableType.STR, null));

        assertEquals("Parent thread cannot be null.", e.getMessage());
    }

    @Test
    void shouldSerializeDefaultArrayAsNativeLHArray() {
        WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
            io.littlehorse.sdk.wfsdk.WfRunVariable arrVar = thread.declareArray("my-array", Long.class);
            arrVar.withDefault(new Long[] {1L, 2L});
        });

        PutWfSpecRequest pwf = wf.compileWorkflow();
        ThreadVarDef varDef =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getVariableDefs(0);
        VariableValue def = varDef.getVarDef().getDefaultValue();

        assertThat(def.getValueCase()).isEqualTo(VariableValue.ValueCase.ARRAY);
        assertThat(def.getArray().getItemsCount()).isEqualTo(2);
        assertThat(def.getArray().getItems(0).getInt()).isEqualTo(1L);
        assertThat(def.getArray().getItems(1).getInt()).isEqualTo(2L);
    }
}
