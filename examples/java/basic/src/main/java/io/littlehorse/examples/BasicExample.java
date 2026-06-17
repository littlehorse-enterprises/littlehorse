package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/*
 * Dashboard conditional gallery: exercises every Comparator, else branches,
 * BOOL truthiness checks, and task-output conditions in one WfSpec.
 */
public class BasicExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-basic", wf -> {
            WfRunVariable theName = wf.declareStr("input-name").searchable();
            WfRunVariable score = wf.declareInt("score").withDefault(42);
            WfRunVariable status = wf.declareStr("status").withDefault("alpha");
            WfRunVariable enabled = wf.declareBool("enabled").withDefault(true);

            NodeOutput greeting = wf.execute("greet", theName);

            wf.doIf(wf.condition(score, Comparator.LESS_THAN, 10), body -> body.execute("greet", "branch-lt"))
                    .doElseIf(wf.condition(score, Comparator.GREATER_THAN, 100), body -> body.execute("greet", "branch-gt"))
                    .doElseIf(
                            wf.condition(score, Comparator.LESS_THAN_EQ, 50), body -> body.execute("greet", "branch-lte"))
                    .doElseIf(
                            wf.condition(score, Comparator.GREATER_THAN_EQ, 90),
                            body -> body.execute("greet", "branch-gte"))
                    .doElseIf(
                            wf.condition(status, Comparator.EQUALS, "alpha"), body -> body.execute("greet", "branch-eq"))
                    .doElseIf(
                            wf.condition(status, Comparator.NOT_EQUALS, "denied"),
                            body -> body.execute("greet", "branch-neq"))
                    .doElseIf(
                            status.isIn(new ArrayList<>(List.of("alpha", "beta", "gamma"))),
                            body -> body.execute("greet", "branch-in"))
                    .doElseIf(
                            status.isNotIn(new ArrayList<>(List.of("denied", "blocked"))),
                            body -> body.execute("greet", "branch-not-in"))
                    .doElse(body -> body.execute("greet", "branch-else"));

            wf.doIf(enabled, body -> body.execute("greet", "truthy-yes"))
                    .doElse(body -> body.execute("greet", "truthy-no"));

            wf.doIf(greeting.isEqualTo("hello there, Obi-Wan"), body -> body.execute("greet", "output-match"))
                    .doElse(body -> body.execute("greet", "output-mismatch"));
        });
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config")
                .toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static LHTaskWorker getTaskWorker(LHConfig config) {
        MyWorker executable = new MyWorker();
        LHTaskWorker worker = new LHTaskWorker(executable, "greet", config);

        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException {
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        Workflow workflow = getWorkflow();
        LHTaskWorker worker = getTaskWorker(config);

        worker.registerTaskDef();
        workflow.registerWfSpec(config.getBlockingStub());
        worker.start();
    }
}
