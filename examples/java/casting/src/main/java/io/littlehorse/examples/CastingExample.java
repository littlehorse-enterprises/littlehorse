package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CastingExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl("casting-workflow", wf -> {
            WfRunVariable stringInput = wf.declareStr("string-number").withDefault("3.14");
            WfRunVariable stringBool = wf.declareStr("string-bool").withDefault("false");
            WfRunVariable jsonInput = wf.declareJsonObj("json-input").withDefault(Map.of("int", "1", "string", "hello"));

            NodeOutput doubleResult = wf.execute("double-method", stringInput.cast(VariableType.DOUBLE)); // Manual cast from STR variable to DOUBLE
            NodeOutput intResult = wf.execute("int-method", doubleResult.castToInt()); // Manual cast from DOUBLE output to INT

            wf.execute("bool-method", stringBool.castToBool()); // Manual cast from STR to BOOL
            wf.execute("int-method", doubleResult.castToInt()); // Manual cast from DOUBLE to INT
            wf.execute("double-method", intResult); // Auto cast from INT to DOUBLE
            wf.execute("int-method", jsonInput.jsonPath("$.int").castToInt());// We don't know the type of json path, but here we are forcing it to be INT
            wf.execute("string-method", stringInput); // Print the original string

        });
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath =
                Path.of(System.getProperty("user.home"), ".config/littlehorse.config").toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        CastingWorker executable = new CastingWorker();
        LHTaskWorker stringWorker = new LHTaskWorker(executable, "string-method", config);
        LHTaskWorker intWorker = new LHTaskWorker(executable, "int-method", config);
        LHTaskWorker doubleWorker = new LHTaskWorker(executable, "double-method", config);
        LHTaskWorker boolWorker = new LHTaskWorker(executable, "bool-method", config);

        Runtime.getRuntime().addShutdownHook(new Thread(stringWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(intWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(doubleWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(boolWorker::close));
        return List.of(stringWorker, intWorker, doubleWorker, boolWorker);
    }

    public static void main(String[] args) throws IOException {
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        Workflow workflow = getWorkflow();
        List<LHTaskWorker> workers = getTaskWorkers(config);

        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        workflow.registerWfSpec(config.getBlockingStub());

        for (LHTaskWorker worker : workers) {
            worker.start();
        }
    }
}
