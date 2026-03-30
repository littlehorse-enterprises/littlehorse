package e2e;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.Array;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.TaskNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class ArraysTest {

    @LHWorkflow("empty-array-assign-wf")
    private Workflow emptyArrayWf;

    @LHWorkflow("mixed-array-assign-wf")
    private Workflow mixedArrayWf;

    @LHWorkflow("filled-array-assign-wf")
    private Workflow filledArrayWf;

    @LHWorkflow("array-get-wf")
    private Workflow arrayGetWf;

    private LittleHorseBlockingStub client;
    private WorkflowVerifier workflowVerifier;

    @Test
    public void shouldAllowAssigningEmptyNativeArrayToTypedArrayVariable() {
        workflowVerifier
                .prepareRun(emptyArrayWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-array", variableValue -> {
                    // Expect an ARRAY VariableValue with zero items
                    Assertions.assertThat(variableValue.getValueCase().toString())
                            .isEqualTo("ARRAY");
                    Assertions.assertThat(variableValue.getArray().getItemsList())
                            .hasSize(0);
                })
                .start();
    }

    @Test
    public void shouldRejectMixedTypedArrayInputOnRunWf() {
        // Ensure WfSpec is registered
        workflowVerifier
                .prepareRun(emptyArrayWf)
                .waitForStatus(LHStatus.COMPLETED)
                .start();

        String wfRunId = UUID.randomUUID().toString();

        VariableValue mixedArr = VariableValue.newBuilder()
                .setArray(Array.newBuilder()
                        .addItems(VariableValue.newBuilder().setInt(1).build())
                        .addItems(VariableValue.newBuilder().setStr("two").build())
                        .build())
                .build();

        assertThatThrownBy(() -> client.runWf(RunWfRequest.newBuilder()
                        .setWfSpecName("empty-array-assign-wf")
                        .setId(wfRunId)
                        .putVariables("my-array", mixedArr)
                        .build()))
                .matches(exn -> {
                    if (!(exn instanceof StatusRuntimeException)) return false;
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Status.Code.INVALID_ARGUMENT
                            && sre.getMessage().toLowerCase().contains("my-array");
                });
    }

    @LHTaskMethod("produce-empty-array")
    @LHType(isLHArray = true)
    public Long[] produceEmptyArray() {
        return new Long[0];
    }

    @Test
    public void shouldAssignNativeArrayWithContents() {
        workflowVerifier
                .prepareRun(filledArrayWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-array", variableValue -> {
                    Assertions.assertThat(variableValue.getValueCase().toString())
                            .isEqualTo("ARRAY");
                    Assertions.assertThat(variableValue.getArray().getItemsList())
                            .hasSize(3);
                    Assertions.assertThat(variableValue.getArray().getItems(0).getInt())
                            .isEqualTo(1L);
                    Assertions.assertThat(variableValue.getArray().getItems(1).getInt())
                            .isEqualTo(2L);
                    Assertions.assertThat(variableValue.getArray().getItems(2).getInt())
                            .isEqualTo(3L);
                })
                .start();
    }

    @Test
    public void shouldAllowGettingArrayElementByIndex() {
        workflowVerifier
                .prepareRun(arrayGetWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "picked", variableValue -> {
                    Assertions.assertThat(variableValue.getValueCase().toString())
                            .isEqualTo("INT");
                    Assertions.assertThat(variableValue.getInt()).isEqualTo(2L);
                })
                .start();
    }

    @LHTaskMethod("produce-array")
    @LHType(isLHArray = true)
    public Long[] produceArray() {
        return new Long[] {1L, 2L, 3L};
    }

    @LHWorkflow("array-get-wf")
    public Workflow buildArrayGetWf() {
        return new WorkflowImpl("array-get-wf", thread -> {
            WfRunVariable arrVar = thread.declareArray("my-array", Long.class);
            WfRunVariable picked = thread.declareInt("picked");
            TaskNodeOutput produced = thread.execute("produce-array");
            arrVar.assign(produced);
            picked.assign(arrVar.get(1));
        });
    }

    @LHWorkflow("empty-array-assign-wf")
    public Workflow buildEmptyArrayAssignWf() {
        return new WorkflowImpl("empty-array-assign-wf", thread -> {
            WfRunVariable arrVar = thread.declareArray("my-array", Long.class);
            TaskNodeOutput produced = thread.execute("produce-empty-array");
            arrVar.assign(produced);
        });
    }

    @LHWorkflow("mixed-array-assign-wf")
    public Workflow buildMixedArrayAssignWf() {
        return new WorkflowImpl("mixed-array-assign-wf", thread -> {
            WfRunVariable arrVar = thread.declareArray("my-array", Long.class);
            TaskNodeOutput produced = thread.execute("produce-mixed-array");
            arrVar.assign(produced);
        });
    }

    @LHWorkflow("filled-array-assign-wf")
    public Workflow buildFilledArrayAssignWf() {
        return new WorkflowImpl("filled-array-assign-wf", thread -> {
            WfRunVariable arrVar = thread.declareArray("my-array", Long.class);
            TaskNodeOutput produced = thread.execute("produce-array");
            arrVar.assign(produced);
        });
    }
}
