package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import e2e.Struct.UserCredentials;
import io.littlehorse.common.LHConstants;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithStructDefs;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
@WithStructDefs({UserCredentials.class})
public class StructMaskedFieldsTest {
    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("struct-masked-fields-wf")
    private Workflow maskedFieldsWorkflow;

    @Test
    void shouldMaskStructFieldValues() {
        UserCredentials creds = new UserCredentials("admin", "s3cret!");
        Arg structArg = Arg.of("credentials", creds);

        verifier.prepareRun(maskedFieldsWorkflow, structArg)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "credentials", variableValue -> {
                    // The username field should NOT be masked
                    assertThat(variableValue
                                    .getStruct()
                                    .getStruct()
                                    .getFieldsMap()
                                    .get("username")
                                    .getValue()
                                    .getStr())
                            .isEqualTo("admin");

                    // The password field SHOULD be masked
                    assertThat(variableValue
                                    .getStruct()
                                    .getStruct()
                                    .getFieldsMap()
                                    .get("password")
                                    .getValue()
                                    .getStr())
                            .isEqualTo(LHConstants.STRING_MASK);
                })
                .start();
    }

    @LHWorkflow("struct-masked-fields-wf")
    public Workflow structMaskedFieldsWf() {
        return new WorkflowImpl("struct-masked-fields-wf", wf -> {
            WfRunVariable credsVar =
                    wf.declareStruct("credentials", UserCredentials.class).required();

            wf.execute("process-credentials", credsVar.get("username"));
        });
    }

    @LHTaskMethod("process-credentials")
    public String processCredentials(String username) {
        return "Processed user: " + username;
    }
}
