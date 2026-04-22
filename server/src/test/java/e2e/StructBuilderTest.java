package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import e2e.Struct.Address;
import e2e.Struct.Car;
import e2e.Struct.PersonWithAddress;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.LHStructBuilder;
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
@WithStructDefs({Car.class, PersonWithAddress.class, Address.class})
public class StructBuilderTest {

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("struct-builder-wf")
    private Workflow structBuilderWorkflow;

    @LHWorkflow("struct-builder-nested-wf")
    private Workflow nestedStructBuilderWorkflow;

    @Test
    void shouldBuildStructFromInputsAndAssign() {
        verifier.prepareRun(structBuilderWorkflow,
                        Arg.of("brand-input", "Toyota"),
                        Arg.of("model-input", "Camry"))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-car", variableValue -> {
                    var fields = variableValue.getStruct().getStruct().getFieldsMap();
                    assertThat(fields.get("brand").getValue().getStr()).isEqualTo("Toyota");
                    assertThat(fields.get("model").getValue().getStr()).isEqualTo("Camry");
                    assertThat(fields.get("mileage").getValue().getInt()).isEqualTo(0);
                })
                .start();
    }

    @Test
    void shouldBuildStructWithTaskOutput() {
        verifier.prepareRun(structBuilderWorkflow,
                        Arg.of("brand-input", "Honda"),
                        Arg.of("model-input", "Civic"))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-car", variableValue -> {
                    var fields = variableValue.getStruct().getStruct().getFieldsMap();
                    assertThat(fields.get("brand").getValue().getStr()).isEqualTo("Honda");
                    assertThat(fields.get("model").getValue().getStr()).isEqualTo("Civic");
                    assertThat(fields.get("mileage").getValue().getInt()).isEqualTo(42);
                })
                .start();
    }

    @Test
    void shouldBuildStructWithNestedInlineStruct() {
        verifier.prepareRun(nestedStructBuilderWorkflow,
                        Arg.of("name-input", "Obi-Wan"),
                        Arg.of("street-input", "123 Jedi Temple"),
                        Arg.of("state-input", "Coruscant"),
                        Arg.of("zip-input", 12345))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-person", variableValue -> {
                    var personFields = variableValue.getStruct().getStruct().getFieldsMap();
                    assertThat(personFields.get("name").getValue().getStr()).isEqualTo("Obi-Wan");

                    var addressFields = personFields.get("address")
                            .getValue()
                            .getStruct()
                            .getStruct()
                            .getFieldsMap();
                    assertThat(addressFields.get("street").getValue().getStr()).isEqualTo("123 Jedi Temple");
                    assertThat(addressFields.get("state").getValue().getStr()).isEqualTo("Coruscant");
                    assertThat(addressFields.get("zip").getValue().getInt()).isEqualTo(12345);
                })
                .start();
    }

    @LHWorkflow("struct-builder-wf")
    public Workflow structBuilderWf() {
        return new WorkflowImpl("struct-builder-wf", wf -> {
            WfRunVariable brandInput = wf.declareStr("brand-input").required();
            WfRunVariable modelInput = wf.declareStr("model-input").required();
            WfRunVariable carVar = wf.declareStruct("my-car", Car.class);

            var mileage = wf.execute("get-default-mileage");

            LHStructBuilder carBuilder = wf.buildStruct("struct-car")
                    .put("brand", brandInput)
                    .put("model", modelInput)
                    .put("mileage", mileage);

            carVar.assign(carBuilder);

            wf.execute("park-car", carVar);
        });
    }

    @LHWorkflow("struct-builder-nested-wf")
    public Workflow nestedStructBuilderWf() {
        return new WorkflowImpl("struct-builder-nested-wf", wf -> {
            WfRunVariable nameInput = wf.declareStr("name-input").required();
            WfRunVariable streetInput = wf.declareStr("street-input").required();
            WfRunVariable stateInput = wf.declareStr("state-input").required();
            WfRunVariable zipInput = wf.declareInt("zip-input").required();
            WfRunVariable personVar = wf.declareStruct("my-person", PersonWithAddress.class);

            LHStructBuilder personBuilder = wf.buildStruct("struct-person-with-address")
                    .put("name", nameInput)
                    .put("address", wf.buildInlineStruct()
                            .put("street", streetInput)
                            .put("state", stateInput)
                            .put("zip", zipInput));

            personVar.assign(personBuilder);

            wf.execute("greet-person", personVar.get("name"));
        });
    }

    @LHTaskMethod("get-default-mileage")
    public int getDefaultMileage() {
        return 42;
    }

    @LHTaskMethod("park-car")
    public String parkCar(Car car) {
        return "Parked: " + car;
    }

    @LHTaskMethod("greet-person")
    public String greetPerson(String name) {
        return "Hello, " + name;
    }
}
