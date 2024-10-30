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

    WorkflowIgniter(LHConfig config, String name, ThreadFunc entrypointThread) {
        this.config = config;
        this.wfImpl = new WorkflowImpl(name, entrypointThread);
    }

    public static WorkflowIgniter newIgniter(String name, ThreadFunc entrypointThread, LHConfig config) {
        return new WorkflowIgniter(config, name, entrypointThread);
    }

    public void registerAndStart() {
        // Collect task workers
        List<LHTaskWorker> workers = new ArrayList<>();
        for (Method workerMethod : wfImpl.getTaskMethodsToIgnite()) {
            Object executable = initializeExecutable(workerMethod);
            if (!workerMethod.isAnnotationPresent(LHTaskMethod.class)) {
                throw new IllegalArgumentException("Can only use @LHTaskMethod methods");
            }
            String taskName = workerMethod.getAnnotation(LHTaskMethod.class).value();
            workers.add(new LHTaskWorker(executable, taskName, config));
        }

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
            worker.start();
        }
    }

    private Object initializeExecutable(Method method) {
        try {
            return method.getDeclaringClass().getConstructor().newInstance();
        } catch(Exception exn) {
            throw new IllegalStateException(exn);
        }
    }
}
