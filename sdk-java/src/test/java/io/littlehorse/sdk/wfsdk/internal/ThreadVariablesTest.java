package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.IndexTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ThreadVariablesTest {

    @Test
    public void shouldThrowMisconfigurationExceptionWhenAddingJsonIndexToANonJsonVariable() {
        WorkflowImpl wf = new WorkflowImpl(
            "my-workflow",
            thread -> {
                WfRunVariable var = thread
                    .addVariable("my-var", VariableTypePb.INT)
                    .withJsonIndex("$.somePath", IndexTypePb.REMOTE_INDEX);
                thread.sleepUntil(var);
            }
        );
        Throwable throwable = Assertions.catchThrowable(wf::compileWorkflow);
        Assertions
            .assertThat(throwable)
            .isInstanceOf(LHMisconfigurationException.class)
            .hasMessage("Non-Json my-var varibale contains jsonIndex");
    }

    @Test
    public void shouldThrowMisconfigurationExceptionForInvalidJsonPath() {
        WorkflowImpl wf = new WorkflowImpl(
            "my-workflow",
            thread -> {
                WfRunVariable var = thread
                    .addVariable("my-var", VariableTypePb.JSON_OBJ)
                    .withJsonIndex("somePath", IndexTypePb.REMOTE_INDEX);
                thread.sleepUntil(var);
            }
        );
        Throwable throwable = Assertions.catchThrowable(wf::compileWorkflow);
        Assertions
            .assertThat(throwable)
            .isInstanceOf(LHMisconfigurationException.class)
            .hasMessage("Invalid JsonPath: somePath");
    }
}
