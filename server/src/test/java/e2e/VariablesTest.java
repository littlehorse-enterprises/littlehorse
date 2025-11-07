package e2e;

import static org.junit.Assert.assertEquals;

import io.littlehorse.common.LHConstants;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.SearchVariableRequest;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.TaskNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class VariablesTest {

    @LHWorkflow("masked-variables-wf")
    private Workflow maskedVariablesWf;

    @LHWorkflow("mutation-wf")
    private Workflow mutationWf;

    @LHWorkflow("wf-run-id")
    private Workflow wfRunIdWf;

    @LHWorkflow("assign-null-wf")
    private Workflow assignNullWorkflow;

    private LittleHorseBlockingStub client;
    private WorkflowVerifier workflowVerifier;

    @Test
    public void shouldAllowAssigningNullToVariable() {
        workflowVerifier
                .prepareRun(assignNullWorkflow)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "some-json-obj", variableValue -> {
                    Assertions.assertThat(variableValue)
                            .isEqualTo(VariableValue.newBuilder().build());
                })
                .start();
    }

    @Test
    public void shouldMaskVariableValues() {
        final String inputText =
                """
                Vestibulum id mauris vel ex pharetra facilisis. Sed egestas metus mi,\s
                non interdum mauris blandit vitae. Aliquam odio tortor, sollicitudin\s
                nec aliquet mollis, mollis sit amet arcu. In hac habitasse platea dictumst.\s
                Vestibulum lobortis euismod elementum. Nam sed porttitor massa.
               \s""";
        workflowVerifier
                .prepareRun(maskedVariablesWf, Arg.of("text", inputText))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "text", variableValue -> {
                    Assertions.assertThat(variableValue.getStr())
                            .isNotEqualTo(inputText)
                            .isEqualTo(LHConstants.STRING_MASK);
                })
                .thenVerifyVariable(0, "length", variableValue -> {
                    Assertions.assertThat(variableValue.getInt()).isEqualTo(inputText.length());
                })
                .thenVerifyTaskRun(0, 1, taskRun -> {
                    List<VarNameAndVal> variables = taskRun.getInputVariablesList();
                    Assertions.assertThat(variables).hasSize(1);
                    VarNameAndVal taskArg = variables.get(0);
                    VariableValue inputTextArg = taskArg.getValue();
                    Assertions.assertThat(inputTextArg.getStr()).isEqualTo(LHConstants.STRING_MASK);
                })
                .start();
    }

    @Test
    public void shouldHandleVarSubErrors() {
        final String expectedMessage = "Cannot divide by zero";
        workflowVerifier
                .prepareRun(mutationWf, Arg.of("value-a", 9), Arg.of("value-b", 0))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyNodeRun(0, 0, nodeRun -> {
                    Assertions.assertThat(nodeRun.getFailuresList()).hasSize(1);
                    Failure failure = nodeRun.getFailuresList().get(0);
                    Assertions.assertThat(failure.getMessage()).contains(expectedMessage);
                })
                .start();
    }

    @Test
    public void shouldHandleDefaultValueForWfRunIdVariable() {
        workflowVerifier
                .prepareRun(wfRunIdWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRun(0, 1, taskRun -> {
                    VariableValue expectedVariableValue = VariableValue.newBuilder()
                            .setWfRunId(WfRunId.newBuilder().setId("default-id").build())
                            .build();
                    List<VarNameAndVal> inputVars = taskRun.getInputVariablesList();
                    Assertions.assertThat(inputVars).hasSize(1);
                    VarNameAndVal varNameAndVal = inputVars.get(0);
                    Assertions.assertThat(varNameAndVal.getValue()).isEqualTo(expectedVariableValue);
                })
                .start();
    }

    @Test
    public void shouldHandleInputWfRunVariable() {
        workflowVerifier
                .prepareRun(
                        wfRunIdWf,
                        Arg.of("wfrun-a", WfRunId.newBuilder().setId("input-id").build()))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRun(0, 1, taskRun -> {
                    VariableValue expectedVariableValue = VariableValue.newBuilder()
                            .setWfRunId(WfRunId.newBuilder().setId("input-id").build())
                            .build();
                    List<VarNameAndVal> inputVars = taskRun.getInputVariablesList();
                    Assertions.assertThat(inputVars).hasSize(1);
                    VarNameAndVal varNameAndVal = inputVars.get(0);
                    Assertions.assertThat(varNameAndVal.getValue()).isEqualTo(expectedVariableValue);
                })
                .start();
    }

    @Test
    public void shouldHandleTaskOutputWfRunVariable() {
        workflowVerifier
                .prepareRun(
                        wfRunIdWf,
                        Arg.of("wfrun-a", WfRunId.newBuilder().setId("input-id").build()))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRun(0, 2, taskRun -> {
                    List<VarNameAndVal> inputVars = taskRun.getInputVariablesList();
                    Assertions.assertThat(inputVars).hasSize(1);
                    VarNameAndVal varNameAndVal = inputVars.get(0);
                    Assertions.assertThat(varNameAndVal.getValue().getWfRunId().getId())
                            .isNotNull();
                })
                .start();
    }

    @Test
    void shouldFindWfRunSearchedOnJsonObjField() {
        WfRunId result = workflowVerifier
                .prepareRun(wfRunIdWf, Arg.of("my-json-blob", Map.of("someField", "asdf")))
                .waitForStatus(LHStatus.COMPLETED)
                .start();

        List<VariableId> results = client
                .searchVariable(SearchVariableRequest.newBuilder()
                        .setWfSpecName("wf-run-id")
                        .setValue(LHLibUtil.objToVarVal("asdf"))
                        .setVarName("my-json-blob_$.someField")
                        .build())
                .getResultsList()
                .stream()
                .filter(id -> id.getWfRunId().getId().equals(result.getId()))
                .toList();
        assertEquals(1, results.size());
    }

    @LHWorkflow("assign-null-wf")
    public Workflow assignNullVariable() {
        return new WorkflowImpl("assign-null-wf", thread -> {
            WfRunVariable myVar = thread.declareJsonObj("some-json-obj");
            myVar.assign(null);
        });
    }

    @LHWorkflow("masked-variables-wf")
    public Workflow buildChildWf() {
        return new WorkflowImpl("masked-variables-wf", thread -> {
            WfRunVariable textVariable =
                    thread.addVariable("text", VariableType.STR).masked().required();
            TaskNodeOutput lengthNodeOutput = thread.execute("get-text-length", textVariable);
            WfRunVariable length = thread.addVariable("length", VariableType.INT);
            thread.mutate(length, VariableMutationType.ASSIGN, lengthNodeOutput);
            thread.execute("print-number", length);
        });
    }

    @LHWorkflow("mutation-wf")
    public Workflow buildMutationWf() {
        return new WorkflowImpl("mutation-wf", thread -> {
            WfRunVariable valueAVariable =
                    thread.addVariable("value-a", VariableType.INT).required();
            WfRunVariable valueBVariable =
                    thread.addVariable("value-b", VariableType.INT).required();
            WfRunVariable resultVariable = thread.addVariable("result", VariableType.INT);
            thread.mutate(valueAVariable, VariableMutationType.DIVIDE, valueBVariable);
            thread.mutate(resultVariable, VariableMutationType.ASSIGN, valueAVariable);
            thread.execute("print-number", resultVariable);
        });
    }

    @LHWorkflow("wf-run-id")
    public Workflow buildWfRunIdWf() {
        return new WorkflowImpl("wf-run-id", thread -> {
            WfRunVariable valueAVariable = thread.addVariable("wfrun-a", VariableType.WF_RUN_ID)
                    .withDefault(WfRunId.newBuilder().setId("default-id").build());
            thread.declareJsonObj("my-json-blob").searchableOn("$.someField", VariableType.STR);
            TaskNodeOutput output = thread.execute("print-wf-run-id", valueAVariable);
            thread.execute("print-wf-run-id", output);
        });
    }

    @LHTaskMethod("get-text-length")
    @LHType(masked = false, name = "input-text-length")
    public Integer verifyOutput(@LHType(masked = true, name = "input-text") String variableValue) {
        Objects.requireNonNull(variableValue);
        return variableValue.length();
    }

    @LHTaskMethod("print-number")
    public void printLength(Integer length) {
        System.out.println("Text length is " + length);
    }

    @LHTaskMethod("print-wf-run-id")
    public WfRunId printWfRunId(WfRunId wfRunId) {
        System.out.println(wfRunId);
        return WfRunId.newBuilder().setId(UUID.randomUUID().toString()).build();
    }
}
