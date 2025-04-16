package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorkflowCompilationTest {

    @Test
    // @DisabledUntil(date = "2023-07-22")
    public void shouldThrowMisconfigurationExceptionWhenMultiplesThreadsWithSameName() {
        WorkflowImpl wf = new WorkflowImpl("asdf", thread -> {
            WfRunVariable var = thread.addVariable("my-var", VariableType.INT);
            thread.sleepUntil(var);

            thread.spawnThread(
                    thread1 -> {
                        WfRunVariable var2 = thread1.addVariable("my-var2", VariableType.INT);
                        thread1.sleepUntil(var2);
                    },
                    "my-thread",
                    Map.of());

            thread.spawnThread(
                    thread1 -> {
                        WfRunVariable var3 = thread1.addVariable("my-var3", VariableType.INT);
                        thread1.sleepUntil(var3);
                    },
                    "my-thread",
                    Map.of());
        });
        Throwable throwable = Assertions.catchThrowable(() -> wf.compileWorkflow());
        Assertions.assertThat(throwable).isNotNull();
        Assertions.assertThat(throwable)
                .isInstanceOf(LHMisconfigurationException.class)
                .hasMessage("Thread my-thread already exists");
    }

    @Test
    void shouldCompileAWorkflowWhenAParentVarIsAssignedFromChildNestedThreads() {
        WorkflowImpl wf = new WorkflowImpl("asdf", abuelo -> {
            WfRunVariable abueloVar = abuelo.declareStr("abuelo");
            abuelo.spawnThread(
                    hijo -> {
                        abueloVar.assign("fdsa");
                        hijo.spawnThread(
                                nieto -> {
                                    abueloVar.assign("foo");
                                },
                                "child-thread",
                                null);
                    },
                    "grandchild",
                    null);
        });

        PutWfSpecRequest expectedWfSpecRequest = wf.compileWorkflow();

        Assertions.assertThat(expectedWfSpecRequest).isNotNull();
    }
}
