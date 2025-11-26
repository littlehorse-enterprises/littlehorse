package e2e;

import com.google.protobuf.Timestamp;
import e2e.Struct.Car;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
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

    @LHWorkflow("input-vars-wf")
    public Workflow buildWorkflow() {
        return new WorkflowImpl("input-vars-wf", thread -> {
            WfRunVariable myVar = thread.addVariable("my-var", VariableType.INT);
            WfRunVariable tsVar = thread.addVariable("ts-var", VariableType.TIMESTAMP);

            thread.execute("ab-double-it", myVar);
            thread.execute("ab-subtract", 10, 8);
            thread.execute("print-timestamps", tsVar, tsVar, tsVar, tsVar, tsVar);
        });
    }

    @LHWorkflow("struct-var-wf")
    public Workflow structWorkflow() {
        return new WorkflowImpl("struct-var-wf", thread -> {
            WfRunVariable structVar = thread.declareStruct("struct-input", Car.class);
            thread.execute("increment-mileage", structVar);
            thread.execute("change-details", structVar, "Mustang", "Mach-E");
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
        Instant instant = Instant.ofEpochMilli(1690000000000L);
        inputVar.creationDate = new Date(1690000000000L);
        inputVar.creationInstant = instant;
        inputVar.creationLocalDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        inputVar.creationSqlTimestamp = java.sql.Timestamp.from(instant);
        inputVar.creationTimestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

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

        client.putStructDef(lhStructDefType.toPutStructDefRequest());

        VariableValue originalStruct = LHLibUtil.objToVarVal(new Car("Ford", "Bronco", 123));
        VariableValue expectedStructFromTask1 = LHLibUtil.objToVarVal(new Car("Ford", "Bronco", 124));
        VariableValue expectedStructFromTask2 = LHLibUtil.objToVarVal(new Car("Mustang", "Mach-E", 123));

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

    @Test
    public void timestampVarInput() {
        long epochMs = 1690000000000L;
        Date dt = new Date(epochMs);

        workflowVerifier
                .prepareRun(workflow, Arg.of("ts-var", dt), Arg.of("my-var", 10))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRun(0, 3, taskRun -> {
                    List<VarNameAndVal> inputVars = taskRun.getInputVariablesList();
                    Assertions.assertEquals(5, inputVars.size());
                    for (VarNameAndVal v : inputVars) {
                        VariableValue varValue = v.getValue();
                        Assertions.assertEquals(VariableValue.ValueCase.UTC_TIMESTAMP, varValue.getValueCase());
                        Assertions.assertEquals(
                                epochMs / 1000, varValue.getUtcTimestamp().getSeconds());
                    }
                })
                .start();
    }

    @LHTaskMethod("count-length")
    public int countLength(String toCount) {
        return toCount.length();
    }

    @LHTaskMethod("process-big-obj")
    public String processBigObject(TestJsonObject input) {
        System.out.println("Instant: " + input.creationInstant);
        System.out.println("Date: " + input.creationDate);
        System.out.println("LocalDateTime: " + input.creationLocalDateTime);
        System.out.println("Java SQL Timestamp: " + input.creationSqlTimestamp);
        System.out.println("Protobuf Timestamp: " + input.creationTimestamp);
        return "Greeting: " + input.subObject.foo;
    }

    @LHTaskMethod("process-sub-obj")
    public int processSubObj(TestSubJsonObject input) {
        return 2 * input.bar;
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

    @LHTaskMethod("increment-mileage")
    public Car increaseMileage(Car car) {
        return new Car(car.getBrand(), car.getModel(), car.getMileage() + 1);
    }

    @LHTaskMethod("change-details")
    public Car changeOwner(Car car, String firstName, String lastName) {
        return new Car(firstName, lastName, car.getMileage());
    }

    @LHTaskMethod("print-timestamps")
    public void printTimestamps(
            Instant instant,
            Date date,
            LocalDateTime localDateTime,
            java.sql.Timestamp sqlTimestamp,
            Timestamp timestamp) {
        System.out.println("Instant: " + instant);
        System.out.println("Date: " + date);
        System.out.println("LocalDateTime: " + localDateTime);
        System.out.println("Java SQL Timestamp: " + sqlTimestamp);
        System.out.println("Protobuf Timestamp: " + timestamp);
    }
}

class TestJsonObject {
    public int baz;
    public TestSubJsonObject subObject;
    public Instant creationInstant;
    public Date creationDate;
    public LocalDateTime creationLocalDateTime;
    public java.sql.Timestamp creationSqlTimestamp;
    public Timestamp creationTimestamp;
}

class TestSubJsonObject {

    public String foo;
    public Integer bar;
}
