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

public class CastingExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl("casting-workflow", wf -> {
            WfRunVariable stringInput = wf.declareStr("string-input").withDefault("true");
            WfRunVariable intVar = wf.declareInt("int-var");

            var intResult = wf.execute("int-method", intVar); // Returns an INT
            var doubleResult = wf.execute("double-method", intResult); // Auto cast from INT to DOUBLE
            var strResult= wf.execute("string-method", doubleResult); // Auto cast from DOUBLE to STR

            wf.execute("int-method", doubleResult.castToInt()); // Manual cast from DOUBLE to INT
            wf.execute("bool-method", stringInput.cast(VariableType.BOOL)); // Manual cast from STR to BOOL

            stringInput.assign(strResult);
            wf.execute("double-method", stringInput.castToDouble()); // Manual cast from STR to DOUBLE
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

        // Register the workflow
        workflow.registerWfSpec(config.getBlockingStub());

        for (LHTaskWorker worker : workers) {
            worker.start();
        }

        System.out.println("Casting example workers started!");
        System.out.println();
        System.out.println("=== CASTING WORKFLOW ===");
        System.out.println("Demonstrates both automatic and manual type casting:");
        System.out.println();
        System.out.println("✅ Automatic Casting (no .cast() needed):");
        System.out.println("   - INT → STR (automatic)");
        System.out.println("   - INT → DOUBLE (automatic)");
        System.out.println("   - DOUBLE → STR (automatic)");
        System.out.println("   - BOOL → STR (automatic)");
        System.out.println();
        System.out.println("✅ Manual Casting (requires .cast() calls):");
        System.out.println("   - STR → INT/DOUBLE/BOOL (manual)");
        System.out.println("   - DOUBLE → INT (manual)");
        System.out.println("   - Convenience methods: .castToInt(), .castToDouble(), .castToBool(), etc.");
        System.out.println();
        System.out.println("✅ Variable Assignment Validation:");
        System.out.println("   - Automatic assignments work without .cast()");
        System.out.println("   - Manual assignments require .cast() calls");
        System.out.println();
        System.out.println("Run with: lhctl run casting-workflow");
        System.out.println("Inspect with: lhctl get wfSpec casting-workflow");
    }
}
