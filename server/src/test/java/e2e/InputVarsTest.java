package e2e;

import e2e.Struct.Car;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class InputVarsTest {

    private WorkflowVerifier workflowVerifier;
    private LittleHorseBlockingStub client;

    @LHWorkflow("input-vars-wf")
    private Workflow workflow;

    @LHWorkflow("json-input-vars-wf")
    private Workflow jsonWorkflow;

    @LHWorkflow("struct-var-wf")
    private Workflow structWorkflow;

    @Test
    public void simpleIntegerVarInput() {
        workflowVerifier
                .prepareRun(workflow, Arg.of("my-var", 10))
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 1, TaskStatus.TASK_SUCCESS)
                .waitForTaskStatus(0, 2, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(0, 1, variableValue -> Assertions.assertEquals(20, variableValue.getInt()))
                .thenVerifyTaskRunResult(0, 2, variableValue -> Assertions.assertEquals(2, variableValue.getInt()))
                .start();
    }

    @Test
    public void jsonVarMutation() {
        TestSubJsonObject subObj = new TestSubJsonObject();
        subObj.bar = 5;
        subObj.foo = "Hello there";

        TestJsonObject inputVar = new TestJsonObject();
        inputVar.subObject = subObj;
        inputVar.baz = 137;

        Consumer<VariableValue> verifyProcessBigObjectOutput = variableValue -> {
            Assertions.assertEquals("Greeting: Hello there", variableValue.getStr());
        };

        Consumer<VariableValue> verifyProcessSubObjectOutput = variableValue -> {
            Assertions.assertEquals(11, variableValue.getInt());
        };

        workflowVerifier
                .prepareRun(jsonWorkflow, Arg.of("my-json-var", inputVar))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRunResult(0, 1, verifyProcessSubObjectOutput)
                .thenVerifyTaskRunResult(0, 2, verifyProcessBigObjectOutput)
                .start();
    }

    @Test
    public void structVarInput() {
        LHStructDefType lhStructDefType = new LHStructDefType(Car.class);

        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName("car")
                .setStructDef(lhStructDefType.getInlineStructDef())
                .build());

        VariableValue originalStruct = LHLibUtil.objToVarVal(new Car("Obi-Wan", "Kenobi", 123));
        VariableValue expectedStructFromTask1 = LHLibUtil.objToVarVal(new Car("Obi-Wan", "Kenobi", 124));
        VariableValue expectedStructFromTask2 = LHLibUtil.objToVarVal(new Car("Colt", "McNealy", 123));

        workflowVerifier
                .prepareRun(structWorkflow, Arg.of("struct-input", originalStruct))
                .waitForTaskStatus(0, 1, TaskStatus.TASK_SUCCESS)
                .waitForTaskStatus(0, 2, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(
                        0, 1, variableValue -> Assertions.assertEquals(expectedStructFromTask1, variableValue))
                .thenVerifyTaskRunResult(
                        0, 2, variableValue -> Assertions.assertEquals(expectedStructFromTask2, variableValue))
                .start();
    }

    @LHTaskMethod("count-length")
    public int countLength(String toCount) {
        return toCount.length();
    }

    @LHTaskMethod("process-big-obj")
    public String processBigObject(TestJsonObject input) {
        return "Greeting: " + input.subObject.foo;
    }

    @LHTaskMethod("process-sub-obj")
    public int processSubObj(TestSubJsonObject input) {
        return 2 * input.bar;
    }

    @LHWorkflow("input-vars-wf")
    public Workflow buildWorkflow() {
        return new WorkflowImpl("input-vars-wf", thread -> {
            WfRunVariable myVar = thread.addVariable("my-var", VariableType.INT);
            thread.execute("ab-double-it", myVar);
            thread.execute("ab-subtract", 10, 8);
        });
    }

    @LHWorkflow("struct-var-wf")
    public Workflow structWorkflow() {
        return new WorkflowImpl("struct-var-wf", thread -> {
            WfRunVariable structVar = thread.declareStruct("struct-input", Car.class);
            thread.execute("increase-mileage", structVar);
            thread.execute("change-owner", structVar, "Colt", "McNealy");
        });
    }

    @LHWorkflow("json-input-vars-wf")
    public Workflow jsonInputVarsWf() {
        return new WorkflowImpl("json-input-vars-wf", thread -> {
            WfRunVariable myVar = thread.addVariable("my-json-var", VariableType.JSON_OBJ);

            // Use JsonPath to pass in a String from a nested subobject.
            NodeOutput taskOutput = thread.execute("count-length", myVar.jsonPath("$.subObject.foo"));

            // Use jsonpath to edit a nested field in a big object
            thread.mutate(myVar.jsonPath("$.subObject.bar"), VariableMutationType.ADD, taskOutput);

            // Pass in a JSON_OBJ to a Java task that takes in a POJO.
            // Behold the magic of the Java LH SDK!
            thread.execute("process-big-obj", myVar);

            // Can also pass in a whole sub object rather than just a
            // string
            thread.execute("process-sub-obj", myVar.jsonPath("$.subObject"));
        });
    }

    @LHTaskMethod("ab-double-it")
    public int doubleIt(int toDouble) {
        System.out.println("");
        return toDouble * 2;
    }

    @LHTaskMethod("ab-subtract")
    public Long subtract(long first, Integer second) {
        return first - second;
    }

    @LHTaskMethod("increase-mileage")
    public Car increaseMileage(Car car) {
        return new Car(car.getFirstName(), car.getLastName(), car.getMileage() + 1);
    }

    @LHTaskMethod("change-owner")
    public Car changeOwner(Car car, String firstName, String lastName) {
        return new Car(firstName, lastName, car.getMileage());
    }
}

class TestJsonObject {
    public int baz;
    public TestSubJsonObject subObject;
}

class TestSubJsonObject {

    public String foo;
    public Integer bar;
}
