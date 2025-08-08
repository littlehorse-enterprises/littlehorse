package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
/*
 * This example demonstrates automatic type casting in LittleHorse.
 * It shows how primitive types are automatically converted when passed 
 * to tasks expecting different types:
 * - INT → STR (automatic)
 * - INT → DOUBLE (automatic)  
 * - DOUBLE → STR (automatic)
 * - BOOL → STR (automatic)
 */
public class CastingExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "auto-casting-workflow",
            wf -> {
                WfRunVariable intVar = wf.addVariable("int-var", VariableType.INT).withDefault(3);
                WfRunVariable doubleVar = wf.addVariable("double-var", VariableType.DOUBLE).withDefault(3.14);
                WfRunVariable stringVar = wf.addVariable("string-var", VariableType.STR).withDefault("some string");
                WfRunVariable boolVar = wf.addVariable("bool-var", VariableType.BOOL).withDefault(true);

                var intResult = wf.execute("int-method", intVar);
                var doubleResult = wf.execute("double-method", doubleVar);
                var stringResult = wf.execute("string-method", stringVar);
                var boolResult =  wf.execute("bool-method", boolVar);

                wf.execute("string-method", 3);     // INT → STR
                wf.execute("string-method", 3.14);     // INT → DOUBLE
                wf.execute("string-method", "some-string");  // DOUBLE → STR
                wf.execute("string-method", true);
                wf.execute("double-method", 1);

                wf.execute("string-method", intVar);     // INT → STR
                wf.execute("string-method", doubleVar);     // INT → DOUBLE
                wf.execute("string-method", stringVar);  // DOUBLE → STR
                wf.execute("string-method", boolVar);
                wf.execute("double-method", intVar);

                wf.execute("string-method", intResult);     // INT → STR
                wf.execute("string-method", doubleResult);     // INT → DOUBLE
                wf.execute("string-method", stringResult);  // DOUBLE → STR
                wf.execute("string-method", boolResult);
                wf.execute("double-method", intResult);// BOOL → STR
            }
        );
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        ).toFile();
        if(configPath.exists()){
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
        
        System.out.println("Casting example workers started. The workflow demonstrates automatic type casting:");
        System.out.println("- INT → STR (automatic)");
        System.out.println("- INT → DOUBLE (automatic)"); 
        System.out.println("- DOUBLE → STR (automatic)");
        System.out.println("- BOOL → STR (automatic)");
    }
}
