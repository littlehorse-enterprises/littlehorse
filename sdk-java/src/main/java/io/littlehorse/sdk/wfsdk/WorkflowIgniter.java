package io.littlehorse.sdk.wfsdk;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;

public class WorkflowIgniter {

    private WorkflowImpl wfImpl;
    private LHConfig config;
    private List<Object> executables;

    WorkflowIgniter(LHConfig config, String name, ThreadFunc entrypointThread, Object... workerExecutables) {
        this.config = config;
        this.wfImpl = new WorkflowImpl(name, entrypointThread);
        this.executables = new ArrayList<>();
        for (Object executable : workerExecutables) {
            executables.add(executable);
        }
    }

    public static WorkflowIgniter newIgniter(String name, ThreadFunc entrypointThread, LHConfig config, Object... workerExecutables) {
        return new WorkflowIgniter(config, name, entrypointThread, workerExecutables);
    }

    public void registerAndStart() {
        // Collect task workers
        List<LHTaskWorker> workers = collectWorkers();

        // Register tasks
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // TODO: Register UserTaskDefs
        // TODO: Register ExternalEventDefs
        // TODO: Register WorkflowEventDefs

        // Register workflows
        wfImpl.registerWfSpec(config.getBlockingStub());

        // Start Task Workers
        for (LHTaskWorker worker : workers) {
            Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
            System.out.println("Starting worker!");
            worker.start();
        }
    }

    private List<LHTaskWorker> collectWorkers() {
        List<LHTaskWorker> workers = new ArrayList<>();
        for (Object executable : executables) {
            for (Method workerMethod : executable.getClass().getMethods()) {
                if (!workerMethod.isAnnotationPresent(LHTaskMethod.class)) {
                    continue;
                }
                String taskName = workerMethod.getAnnotation(LHTaskMethod.class).value();
                workers.add(new LHTaskWorker(executable, taskName, config));
            }
        }
        System.out.println("workers: " + workers);
        return workers;
    }
}
