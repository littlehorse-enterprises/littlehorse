package e2e;

import static org.junit.jupiter.api.Assertions.assertThrows;

import e2e.Struct.Address;
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
@WithStructDefs({
    Car.class,
    Address.class,
    StructValidationsTest.RecordCar.class,
    StructValidationsTest.RecordAddress.class,
    StructValidationsTest.PersonWithRecordAddress.class,
    StructValidationsTest.RecordPersonWithAddress.class
})
public class StructValidationsTest {

    @LHStructDef("struct-record-car")
    public record RecordCar(String brand, String model, int mileage) {}

    @LHStructDef("struct-record-address")
    public record RecordAddress(String street, String state, int zip) {}

    @LHStructDef("struct-person-with-record-address")
    public static class PersonWithRecordAddress {
        private String name;
        private RecordAddress address;

        public PersonWithRecordAddress() {}

        public PersonWithRecordAddress(String name, RecordAddress address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public RecordAddress getAddress() {
            return address;
        }

        public void setAddress(RecordAddress address) {
            this.address = address;
        }
    }

    @LHStructDef("struct-record-person-with-address")
    public record RecordPersonWithAddress(String name, Address address) {}

    private WorkflowVerifier verifier;

    @LHWorkflow("structs-wf")
    private Workflow structWorkflow;

    @LHWorkflow("record-structs-wf")
    private Workflow recordStructWorkflow;

    @LHWorkflow("nested-record-structs-wf")
    private Workflow nestedRecordStructWorkflow;

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
    void shouldRoundTripNestedRecordsAndStructDefs() {
        PersonWithRecordAddress classWithRecord =
                new PersonWithRecordAddress("Leia", new RecordAddress("1 Royal Way", "Alderaan", 10001));
        RecordPersonWithAddress recordWithClass =
                new RecordPersonWithAddress("Luke", new Address("1 Farm Road", "Tatooine", 20002));

        verifier.prepareRun(
                        nestedRecordStructWorkflow,
                        Arg.of("class-with-record", classWithRecord),
                        Arg.of("record-with-class", recordWithClass))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "class-with-record-output", variableValue -> {
                    var fields = variableValue.getStruct().getStruct().getFieldsMap();
                    var addressFields = fields.get("address")
                            .getValue()
                            .getStruct()
                            .getStruct()
                            .getFieldsMap();
                    Assertions.assertThat(fields.get("name").getValue().getStr())
                            .isEqualTo("Leia");
                    Assertions.assertThat(addressFields.get("state").getValue().getStr())
                            .isEqualTo("Alderaan");
                })
                .thenVerifyVariable(0, "record-with-class-output", variableValue -> {
                    var fields = variableValue.getStruct().getStruct().getFieldsMap();
                    var addressFields = fields.get("address")
                            .getValue()
                            .getStruct()
                            .getStruct()
                            .getFieldsMap();
                    Assertions.assertThat(fields.get("name").getValue().getStr())
                            .isEqualTo("Luke");
                    Assertions.assertThat(addressFields.get("state").getValue().getStr())
                            .isEqualTo("Tatooine");
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

    @LHWorkflow("nested-record-structs-wf")
    public Workflow nestedRecordStructsWf() {
        return new WorkflowImpl("nested-record-structs-wf", wf -> {
            WfRunVariable classWithRecord = wf.declareStruct("class-with-record", PersonWithRecordAddress.class)
                    .required();
            WfRunVariable recordWithClass = wf.declareStruct("record-with-class", RecordPersonWithAddress.class)
                    .required();
            WfRunVariable classWithRecordOutput =
                    wf.declareStruct("class-with-record-output", PersonWithRecordAddress.class);
            WfRunVariable recordWithClassOutput =
                    wf.declareStruct("record-with-class-output", RecordPersonWithAddress.class);

            classWithRecordOutput.assign(wf.execute("echo-class-with-record", classWithRecord));
            recordWithClassOutput.assign(wf.execute("echo-record-with-class", recordWithClass));
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

    @LHTaskMethod("echo-class-with-record")
    public PersonWithRecordAddress echoClassWithRecord(PersonWithRecordAddress person) {
        return person;
    }

    @LHTaskMethod("echo-record-with-class")
    public RecordPersonWithAddress echoRecordWithClass(RecordPersonWithAddress person) {
        return person;
    }
}
