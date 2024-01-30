package io.littlehorse.canary.metronome;

import io.littlehorse.canary.app.Bootstrap;
import io.littlehorse.canary.app.InitializationException;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.kafka.MetricsEmitter;
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
    private MetricsEmitter emitter;

    private void initializeWorkflow(LHConfig lhConfig) {
        Workflow workflow = Workflow.newWorkflow(
                "canary-workflow",
                thread -> thread.execute(TASK_NAME, thread.addVariable(VARIABLE_NAME, VariableType.INT)));
        try {
            workflow.registerWfSpec(lhConfig.getBlockingStub());
        } catch (IOException e) {
            throw new InitializationException(e);
        }
    }

    private void initializeTaskWorker(LHConfig lhConfig, CanaryConfig config) {
        try {
            emitter = new MetricsEmitter(config);
            worker = new LHTaskWorker(new MetronomeTask(emitter), TASK_NAME, lhConfig);
            worker.registerTaskDef();
            worker.start();
        } catch (IOException e) {
            throw new InitializationException(e);
        }
    }

    @Override
    public void initialize(CanaryConfig config) {
        LHConfig lhConfig = new LHConfig(config.toLittleHorseConfig().toMap());

        initializeTaskWorker(lhConfig, config);
        initializeWorkflow(lhConfig);

        log.trace("Initialized");
    }

    @Override
    public void shutdown() {
        if (worker != null) {
            worker.close();
        }
        if (emitter != null) {
            emitter.close();
        }
        log.trace("Shutdown");
    }
}
