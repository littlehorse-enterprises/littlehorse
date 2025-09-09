package e2e;

import com.google.protobuf.Timestamp;
import io.littlehorse.sdk.common.proto.LHStatus;
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
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class InputVarsTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("input-vars-wf")
    private Workflow workflow;

    @LHWorkflow("json-input-vars-wf")
    private Workflow jsonWorkflow;

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
        inputVar.creationDate = new Date(1690000000000L);

        Consumer<VariableValue> verifyProcessBigObjectOutput = variableValue -> {
            Assertions.assertEquals("Greeting: Hello there", variableValue.getStr());
        };

        Consumer<VariableValue> verifyProcessSubObjectOutput = variableValue -> {
            Assertions.assertEquals(11, variableValue.getInt());
        };

        Consumer<VariableValue> verifyPrintDateOutput = variableValue -> {
            Assertions.assertEquals(1690000000L, variableValue.getUtcTimestamp().getSeconds());
        };

        workflowVerifier
                .prepareRun(jsonWorkflow, Arg.of("my-json-var", inputVar))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRunResult(0, 1, verifyProcessSubObjectOutput)
                .thenVerifyTaskRunResult(0, 2, verifyProcessBigObjectOutput)
                .thenVerifyTaskRunResult(0, 4, verifyPrintDateOutput)
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
                    Assertions.assertEquals(1, inputVars.size());
                    VariableValue varValue = inputVars.getFirst().getValue();
                    Assertions.assertEquals(VariableValue.ValueCase.UTC_TIMESTAMP, varValue.getValueCase());
                    Assertions.assertEquals(
                            epochMs / 1000, varValue.getUtcTimestamp().getSeconds());
                })
                .thenVerifyTaskRun(0, 4, taskRun -> {
                    List<VarNameAndVal> inputVars = taskRun.getInputVariablesList();
                    Assertions.assertEquals(1, inputVars.size());
                    VariableValue varValue = inputVars.getFirst().getValue();
                    Assertions.assertEquals(VariableValue.ValueCase.UTC_TIMESTAMP, varValue.getValueCase());
                    Assertions.assertEquals(
                            epochMs / 1000, varValue.getUtcTimestamp().getSeconds());
                })
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
            WfRunVariable tsVar = thread.addVariable("ts-var", VariableType.TIMESTAMP);

            thread.execute("ab-double-it", myVar);
            thread.execute("ab-subtract", 10, 8);
            thread.execute("print-date", tsVar);
            thread.execute("print-proto-ts", tsVar);
            thread.execute("print-instant", tsVar);
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

            // Can also extract a date field and pass it into a date task
            thread.execute("print-date", myVar.jsonPath("$.creationDate"));
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

    @LHTaskMethod("print-date")
    public Date printDate(Date input) {
        System.out.println("print-date: " + input);
        return input;
    }

    @LHTaskMethod("print-proto-ts")
    public Timestamp printProtoTs(Timestamp input) {
        System.out.println("print-proto-ts: " + input);
        return input;
    }

    @LHTaskMethod("print-instant")
    public Instant printInstant(Instant input) {
        System.out.println("print-instant: " + input);
        return input;
    }
}

class TestJsonObject {

    public int baz;
    public TestSubJsonObject subObject;
    public Date creationDate;
}

class TestSubJsonObject {

    public String foo;
    public Integer bar;
}
