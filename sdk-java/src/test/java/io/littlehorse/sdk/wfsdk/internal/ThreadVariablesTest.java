package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ThreadVariablesTest {

    @Test
    public void shouldThrowMisconfigurationExceptionWhenAddingJsonIndexToANonJsonVariable() {
        WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
            WfRunVariable var =
                    thread.addVariable("my-var", VariableType.INT).searchableOn("$.somePath", VariableType.DOUBLE);
            thread.sleepUntil(var);
        });
        Throwable throwable = Assertions.catchThrowable(wf::compileWorkflow);
        Assertions.assertThat(throwable)
                .isInstanceOf(LHMisconfigurationException.class)
                .hasMessage("Non-Json my-var variable contains jsonIndex");
    }

    @Test
    public void shouldThrowMisconfigurationExceptionForInvalidJsonPath() {
        WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
            WfRunVariable var =
                    thread.addVariable("my-var", VariableType.JSON_OBJ).searchableOn("somePath", VariableType.INT);
            thread.sleepUntil(var);
        });
        Throwable throwable = Assertions.catchThrowable(wf::compileWorkflow);
        Assertions.assertThat(throwable)
                .isInstanceOf(LHMisconfigurationException.class)
                .hasMessage("Invalid JsonPath: somePath");
    }

    @Test
    public void shouldBeAbleToCreateSearchableVariable() {
        WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
            thread.addVariable("my-var", VariableType.STR).searchable();
        });

        PutWfSpecRequest pwf = wf.compileWorkflow();
        List<ThreadVarDef> varDefs =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getVariableDefsList();

        Assertions.assertThat(varDefs.size()).isEqualTo(1);
        Assertions.assertThat(varDefs.get(0).getSearchable()).isTrue();
    }
}
