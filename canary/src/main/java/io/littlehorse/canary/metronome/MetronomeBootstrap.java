package io.littlehorse.canary.metronome;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.canary.util.Shutdown;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeBootstrap implements Bootstrap {
    public static final String TASK_NAME = "canary-worker-task";
    public static final String VARIABLE_NAME = "start-time";

    public MetronomeBootstrap(
            final String metricsTopicName,
            final Map<String, Object> kafkaProducerConfigMap,
            final Map<String, Object> littleHorseConfigMap) {

        final LHConfig lhConfig = new LHConfig(littleHorseConfigMap);
        final MetricsEmitter emitter = new MetricsEmitter(metricsTopicName, kafkaProducerConfigMap);
        Shutdown.addShutdownHook(emitter);

        initializeWorker(emitter, lhConfig);
        initializeWorkflow(lhConfig);

        final Metronome metronome = new Metronome(emitter);
        Shutdown.addShutdownHook(metronome);

        log.trace("Initialized");
    }

    private static void initializeWorkflow(final LHConfig lhConfig) {
        final Workflow workflow = Workflow.newWorkflow(
                "canary-workflow",
                thread -> thread.execute(TASK_NAME, thread.addVariable(VARIABLE_NAME, VariableType.INT)));
        try {
            workflow.registerWfSpec(lhConfig.getBlockingStub());
        } catch (IOException e) {
            throw new CanaryException(e);
        }
    }

    private static void initializeWorker(final MetricsEmitter emitter, final LHConfig lhConfig) {
        try {
            final LHTaskWorker worker = new LHTaskWorker(new MetronomeTask(emitter), TASK_NAME, lhConfig);
            Shutdown.addShutdownHook(worker);
            worker.registerTaskDef();
            worker.start();
        } catch (IOException e) {
            throw new CanaryException(e);
        }
    }
}
