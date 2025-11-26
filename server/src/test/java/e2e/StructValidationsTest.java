package e2e;

import static org.junit.jupiter.api.Assertions.assertThrows;

import e2e.Struct.Car;
import e2e.Struct.CarWithExtraField;
import e2e.Struct.CarWithMissingField;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@LHTest
public class StructValidationsTest {
    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("structs-wf")
    private Workflow structWorkflow;

    @BeforeEach
    public void setup() {
        LHStructDefType lhStructDefType = new LHStructDefType(Car.class);
        client.putStructDef(lhStructDefType.toPutStructDefRequest());
    }

    @Test
    void shouldPassWithValidStruct() {
        Arg structArg = Arg.of("my-car", new Car("Obi-Wan", "Kenobi", 5000));

        verifier.prepareRun(structWorkflow, structArg)
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    void shouldFailWithNonStructInput() {
        StatusRuntimeException caught = assertThrows(StatusRuntimeException.class, () -> {
            verifier.prepareRun(structWorkflow, Arg.of("my-car", "hello")).start();
        });

        Assertions.assertThat(caught.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
    }

    @Test
    void shouldFailWithStructWithMissingField() {
        StatusRuntimeException caught = assertThrows(StatusRuntimeException.class, () -> {
            verifier.prepareRun(structWorkflow, Arg.of("my-car", new CarWithMissingField("Obi-Wan", "Kenobi")))
                    .start();
        });

        Assertions.assertThat(caught.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
    }

    @Test
    void shouldFailWithStructWithExtraField() {
        StatusRuntimeException caught = assertThrows(StatusRuntimeException.class, () -> {
            verifier.prepareRun(
                            structWorkflow, Arg.of("my-car", new CarWithExtraField("Obi-Wan", "Kenobi", 5000, "red")))
                    .start();
        });

        Assertions.assertThat(caught.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
    }

    @LHWorkflow("structs-wf")
    public Workflow structsWf() {
        return new WorkflowImpl("structs-wf", wf -> {
            WfRunVariable carVar = wf.declareStruct("my-car", Car.class).required();

            wf.execute("park-car", carVar);
        });
    }

    @LHTaskMethod("park-car")
    public String parkCar(Car car) {
        return "asdf";
    }
}
