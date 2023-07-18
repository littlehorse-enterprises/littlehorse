package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.util.Arrays;
import java.util.List;

public class ACVarMutationsJsonObj extends WorkflowLogicTest {

    public ACVarMutationsJsonObj(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return (
            "Tests JSONPath for both task inputs (variable assignment) and " +
            "Variable Mutations; i.e. mutating a sub-field of a JSON Object."
        );
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                WfRunVariable myVar = thread.addVariable(
                    "my-var",
                    VariableTypePb.JSON_OBJ
                );

                // Use JsonPath to pass in a String from a nested subobject.
                NodeOutput taskOutput = thread.execute(
                    "count-length",
                    myVar.jsonPath("$.subObject.foo")
                );

                // Use jsonpath to edit a nested field in a big object
                thread.mutate(
                    myVar.jsonPath("$.subObject.bar"),
                    VariableMutationTypePb.ADD,
                    taskOutput
                );

                // Pass in a JSON_OBJ to a Java task that takes in a POJO.
                // Behold the magic of the Java LH SDK!
                thread.execute("process-big-obj", myVar);

                // Can also pass in a whole sub object rather than just a
                // string
                thread.execute("process-sub-obj", myVar.jsonPath("$.subObject"));
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ACJsonPathThing());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws TestFailure, InterruptedException, LHApiError {
        ACJsonPathThing worker = new ACJsonPathThing();
        /* Create a POJO which will be a json like:
        {
            "baz": 137,
            "subObject": {
                "bar": 5,
                "foo": "Hello there"
            }
        }
         */
        MyJsonSubObj subObj = new MyJsonSubObj();
        subObj.bar = 5;
        subObj.foo = "Hello there";

        MyJsonObj inputVar = new MyJsonObj();
        inputVar.subObject = subObj;
        inputVar.baz = 137;

        // Now pass it in as a variable
        String wfRunId = runWf(client, Arg.of("my-var", inputVar));
        Thread.sleep(500);

        assertStatus(client, wfRunId, LHStatusPb.COMPLETED);

        // Check first output
        assertTaskOutput(client, wfRunId, 0, 1, inputVar.subObject.foo.length());

        // Check that the first task properly mutated the variable
        MyJsonObj result = getVarAsObj(client, wfRunId, 0, "my-var", MyJsonObj.class);
        if (
            !result.subObject.bar.equals(
                inputVar.subObject.foo.length() + inputVar.subObject.bar
            )
        ) {
            throw new TestFailure(
                this,
                "Got wrong value for variable my-var on jsonpath $.subObject.bar"
            );
        }

        // Check second task output
        assertTaskOutput(client, wfRunId, 0, 2, worker.processBigObject(inputVar));

        // Check third task output
        assertTaskOutput(
            client,
            wfRunId,
            0,
            3,
            worker.processSubObj(result.subObject)
        );

        return Arrays.asList(wfRunId);
    }
}

class ACJsonPathThing {

    @LHTaskMethod("count-length")
    public int countLength(String toCount) {
        return toCount.length();
    }

    @LHTaskMethod("process-big-obj")
    public String processBigObject(MyJsonObj input) {
        return "Greeting: " + input.subObject.foo;
    }

    @LHTaskMethod("process-sub-obj")
    public int processSubObj(MyJsonSubObj input) {
        return 2 * input.bar;
    }
}

class MyJsonObj {

    public int baz;
    public MyJsonSubObj subObject;
}

class MyJsonSubObj {

    public String foo;
    public Integer bar;
}
