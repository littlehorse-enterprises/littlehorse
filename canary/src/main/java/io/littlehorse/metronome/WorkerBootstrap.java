package io.littlehorse.metronome;

import io.littlehorse.common.app.BoostrapInitializationException;
import io.littlehorse.common.app.Bootstrap;
import io.littlehorse.common.config.CanaryConfig;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkerBootstrap implements Bootstrap {
    public static final String TASK_NAME = "canary-worker-task";
    public static final String VARIABLE_NAME = "start-time";
    private LHTaskWorker worker;

    private void initializeWorkflow(LHConfig lhConfig) throws BoostrapInitializationException {
        Workflow workflow = Workflow.newWorkflow(
                "canary-workflow",
                thread -> thread.execute(TASK_NAME, thread.addVariable(VARIABLE_NAME, VariableType.INT)));
        try {
            workflow.registerWfSpec(lhConfig.getBlockingStub());
        } catch (IOException e) {
            throw new BoostrapInitializationException(e);
        }
    }

    private void initializeTask(LHConfig lhConfig) throws BoostrapInitializationException {
        WorkerTask workerTask = new WorkerTask();
        try {
            worker = new LHTaskWorker(workerTask, TASK_NAME, lhConfig);
            worker.registerTaskDef();
            worker.start();
        } catch (IOException e) {
            throw new BoostrapInitializationException(e);
        }
    }

    @Override
    public void initialize(CanaryConfig config) throws BoostrapInitializationException {
        LHConfig lhConfig = new LHConfig(config.toLittleHorseConfig().toMap());
        initializeTask(lhConfig);
        initializeWorkflow(lhConfig);
        log.info("Initialized");
    }

    @Override
    public void shutdown() {
        if (worker != null) {
            worker.close();
        }
        log.info("Shutdown");
    }
}
