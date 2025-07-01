package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.worker.LHTaskMethod;
import org.junit.jupiter.api.Test;

class LHTaskSignatureTest {

    @Test
    public void shouldCreateTaskSignatureForWfRunIdArgsAndReturn() {
        LHTaskSignature signature = new LHTaskSignature("wf", new MyWfRunIdWorker(), "wf");
        assertThat(signature.getParamTypes().get(0)).isEqualTo(VariableType.WF_RUN_ID);
        assertThat(signature.getReturnType().getReturnType().getType()).isEqualTo(VariableType.WF_RUN_ID);
    }

    private static class MyWfRunIdWorker {

        @LHTaskMethod("wf")
        public WfRunId run(WfRunId wfId) {
            return null;
        }
    }
}
