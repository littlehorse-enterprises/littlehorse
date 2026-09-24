package e2e;

import static org.junit.jupiter.api.Assertions.assertThrows;

import e2e.Struct.Car;
import e2e.Struct.CarWithMissingField;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithStructDefs;
import io.littlehorse.test.WorkflowVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
@WithStructDefs({Car.class, StructValidationsTest.RecordCar.class})
public class StructValidationsTest {

    @LHStructDef("struct-record-car")
    public record RecordCar(String brand, String model, int mileage) {}

    private WorkflowVerifier verifier;

    @LHWorkflow("structs-wf")
    private Workflow structWorkflow;

    @LHWorkflow("record-structs-wf")
    private Workflow recordStructWorkflow;

    @Test
    void shouldPassWithValidStruct() {
        Arg structArg = Arg.of("my-car", new Car("Obi-Wan", "Kenobi", 5000));

        verifier.prepareRun(structWorkflow, structArg)
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    void shouldUseRecordStructAsTaskInputAndOutput() {
        verifier.prepareRun(recordStructWorkflow, Arg.of("my-car", new RecordCar("Honda", "Civic", 42)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "parked-car", variableValue -> {
                    var fields = variableValue.getStruct().getStruct().getFieldsMap();
                    Assertions.assertThat(fields.get("brand").getValue().getStr())
                            .isEqualTo("Honda");
                    Assertions.assertThat(fields.get("model").getValue().getStr())
                            .isEqualTo("Civic");
                    Assertions.assertThat(fields.get("mileage").getValue().getInt())
                            .isEqualTo(42);
                })
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

    @LHWorkflow("structs-wf")
    public Workflow structsWf() {
        return new WorkflowImpl("structs-wf", wf -> {
            WfRunVariable carVar = wf.declareStruct("my-car", Car.class).required();

            wf.execute("park-car", carVar);
        });
    }

    @LHWorkflow("record-structs-wf")
    public Workflow recordStructsWf() {
        return new WorkflowImpl("record-structs-wf", wf -> {
            WfRunVariable carVar = wf.declareStruct("my-car", RecordCar.class).required();
            WfRunVariable parkedCar = wf.declareStruct("parked-car", RecordCar.class);

            parkedCar.assign(wf.execute("park-record-car", carVar));
        });
    }

    @LHTaskMethod("park-car")
    public String parkCar(Car car) {
        return "asdf";
    }

    @LHTaskMethod("park-record-car")
    public RecordCar parkRecordCar(RecordCar car) {
        return car;
    }
}
