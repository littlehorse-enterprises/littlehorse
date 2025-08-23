package e2e;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
public class ThreadRunReturnTest {

    private WorkflowVerifier verifier;
    private LittleHorseBlockingStub client;

    @LHWorkflow("threadrun-output")
    private Workflow threadRunOutputWf;

    @LHWorkflow("threadrun-output")
    public Workflow getThreadRunOutputWorkflow() {
        return Workflow.newWorkflow("threadrun-output", wf -> {
            WfRunVariable theName = wf.declareStr("name").required();
            wf.complete(theName);
        });
    }

    @Test
    void dontAllowReturningDifferentTypes() {
        Workflow invalidWf = Workflow.newWorkflow("asdf", wf -> {
            WfRunVariable mySwitch = wf.declareBool("switch");
            WfRunVariable myInt = wf.declareInt("my-int");
            WfRunVariable myDouble = wf.declareDouble("my-double");

            wf.doIf(mySwitch.isEqualTo(true), handler -> {
                handler.complete(myInt);
            });

            wf.complete(myDouble);
        });

        assertThatThrownBy(() -> {
                    invalidWf.registerWfSpec(client);
                })
                .matches(exn -> {
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                            && sre.getMessage().toLowerCase().contains("returned different output types");
                });
    }

    @Test
    void dontAllowReturningNullAndSomethingInSameThread() {
        Workflow invalidWf = Workflow.newWorkflow("asdf", wf -> {
            WfRunVariable mySwitch = wf.declareBool("switch");
            wf.doIf(mySwitch.isEqualTo(true), handler -> {
                handler.complete();
            });

            wf.complete("asdf");
        });

        assertThatThrownBy(() -> {
                    invalidWf.registerWfSpec(client);
                })
                .matches(exn -> {
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                            && sre.getMessage().toLowerCase().contains("returns void")
                            && sre.getMessage().toLowerCase().contains("returns non-void");
                });
    }

    @Test
    void shouldBeAbleToReturnSomething() {
        verifier.prepareRun(threadRunOutputWf, Arg.of("name", LHLibUtil.objToVarVal("asdf")))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    assertEquals("asdf", wfRun.getThreadRuns(0).getOutput().getStr());
                })
                .start();
    }
}
