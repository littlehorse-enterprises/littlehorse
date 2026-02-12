package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Objects;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class SharedVariablesTest {

    private WorkflowVerifier workflowVerifier;
    private LittleHorseBlockingStub client;

    @LHWorkflow("shared-variables-parent-wf")
    private Workflow parentWf;

    @LHWorkflow("shared-variables-child-wf")
    private Workflow childWf;

    @Test
    public void shouldResolvePublicVariablesFromParentWf() {
        String parentWfRunId = UUID.randomUUID().toString();
        String childWfRunId = UUID.randomUUID().toString();

        WfRunId parentWfRun = workflowVerifier
                .prepareRun(parentWf, Arg.of("input-number", 3))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "public-variable", variableValue -> {
                    assertThat(variableValue.getInt()).isEqualTo(6);
                })
                .start(WfRunId.newBuilder().setId(parentWfRunId).build());

        workflowVerifier
                .prepareRun(childWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "calculated-value", variableValue -> {
                    Assertions.assertThat(variableValue.getInt()).isEqualTo(12);
                })
                .start(WfRunId.newBuilder()
                        .setId(childWfRunId)
                        .setParentWfRunId(parentWfRun)
                        .build());

        assertThat(client.getVariable(VariableId.newBuilder()
                                .setWfRunId(parentWfRun)
                                .setName("public-variable")
                                .build())
                        .getValue()
                        .getInt())
                .isEqualTo(12);
    }

    @Test
    void shouldNotFreezeSearchableVariables() {
        // Adding UUID makes it possible to run repeatedly with ExternalBootstrapper
        String wfSpecName = "dont-freeze-searchable-" + UUID.randomUUID();
        Workflow workflow = Workflow.newWorkflow(wfSpecName, wf -> {
            wf.addVariable("some-var", VariableType.STR).searchable();
        });
        WfSpec result = client.putWfSpec(workflow.compileWorkflow());
        assertThat(result.getFrozenVariablesCount()).isEqualTo(0);

        // Additionally, private searchable variables do not increment the major version,
        // and I can change the type of them if they're not public.
        Workflow secondWorkflow = Workflow.newWorkflow(wfSpecName, wf -> {
            wf.addVariable("some-var", VariableType.INT);
        });
        WfSpec secondVersion = client.putWfSpec(secondWorkflow.compileWorkflow());
        assertThat(secondVersion.getId().getMajorVersion()).isEqualTo(0);
        assertThat(secondVersion.getId().getRevision()).isEqualTo(1);
    }

    @Test
    void publicVariablesShouldStayFrozenThroughVersions() {
        String wfSpecName = "public-var-frozen-" + UUID.randomUUID();
        Workflow workflow = Workflow.newWorkflow(wfSpecName, wf -> {
            wf.addVariable("some-var", VariableType.STR).asPublic();
        });
        WfSpec firstVersion = client.putWfSpec(workflow.compileWorkflow());
        assertThat(firstVersion.getFrozenVariablesCount()).isEqualTo(1);

        Workflow secondWorkflow = Workflow.newWorkflow(wfSpecName, wf -> {
            wf.addVariable("some-other-var", "some string value").searchable();
        });
        WfSpec secondVersion = client.putWfSpec(secondWorkflow.compileWorkflow());

        // Breaking change
        assertThat(secondVersion.getId().getMajorVersion()).isEqualTo(1);
        assertThat(secondVersion.getId().getRevision()).isEqualTo(0);

        // The first variable should still be frozen
        assertThat(secondVersion.getFrozenVariablesCount()).isEqualTo(1);
        assertThat(secondVersion.getFrozenVariables(0).getVarDef().getName()).isEqualTo("some-var");
    }

    @Test
    void requiredVariablesIncrementMajorVersion() {
        String wfSpecName = "required-major-version-" + UUID.randomUUID();
        Workflow workflow = Workflow.newWorkflow(wfSpecName, wf -> {
            wf.addVariable("some-var", VariableType.STR).required();
        });
        WfSpec firstVersion = client.putWfSpec(workflow.compileWorkflow());
        assertThat(firstVersion.getFrozenVariablesCount()).isEqualTo(1);

        Workflow secondWorkflow = Workflow.newWorkflow(wfSpecName, wf -> {
            wf.addVariable("some-other-var", "not required").searchable();
        });
        WfSpec secondVersion = client.putWfSpec(secondWorkflow.compileWorkflow());

        assertThat(secondVersion.getId().getMajorVersion()).isEqualTo(1);
        assertThat(secondVersion.getId().getRevision()).isEqualTo(0);
    }

    @Test
    void publicVariablesIncrementMajorVersion() {
        String wfSpecName = "public-major-version-" + UUID.randomUUID();
        Workflow workflow = Workflow.newWorkflow(wfSpecName, wf -> {
            wf.addVariable("some-var", VariableType.STR).asPublic();
            wf.addVariable("another-var", "will-be-removed").asPublic();
        });
        WfSpec firstVersion = client.putWfSpec(workflow.compileWorkflow());
        assertThat(firstVersion.getFrozenVariablesCount()).isEqualTo(2);

        Workflow secondWorkflow = Workflow.newWorkflow(wfSpecName, wf -> {
            wf.addVariable("some-var", "not public anymore").searchable();
        });
        WfSpec thirdVersion = client.putWfSpec(secondWorkflow.compileWorkflow());

        assertThat(thirdVersion.getId().getMajorVersion()).isEqualTo(1);
        assertThat(thirdVersion.getId().getRevision()).isEqualTo(0);
    }

    @LHWorkflow("shared-variables-parent-wf")
    public Workflow buildParentWf() {
        return new WorkflowImpl("shared-variables-parent-wf", thread -> {
            WfRunVariable inputNumber =
                    thread.addVariable("input-number", VariableType.INT).required();
            WfRunVariable publicVariable = thread.addVariable("public-variable", VariableType.INT)
                    .withAccessLevel(WfRunVariableAccessLevel.PUBLIC_VAR);
            thread.mutate(inputNumber, Operation.MULTIPLY, 2);
            thread.mutate(publicVariable, Operation.ASSIGN, inputNumber);
        });
    }

    @LHWorkflow("shared-variables-child-wf")
    public Workflow buildChildWf() {
        Workflow out = new WorkflowImpl("shared-variables-child-wf", thread -> {
            WfRunVariable publicVariable =
                    thread.addVariable("public-variable", VariableType.INT).asInherited();

            WfRunVariable calculatedValue =
                    thread.addVariable("calculated-value", VariableType.INT).searchable();
            thread.mutate(publicVariable, Operation.MULTIPLY, 2);
            thread.execute("print-output", publicVariable);
            thread.mutate(calculatedValue, Operation.ASSIGN, publicVariable);
        });
        out.setParent("shared-variables-parent-wf");
        return out;
    }

    @LHTaskMethod("print-output")
    public void verifyOutput(Integer output) {
        Objects.requireNonNull(output);
        System.out.println("Calculated value: " + output);
    }
}
