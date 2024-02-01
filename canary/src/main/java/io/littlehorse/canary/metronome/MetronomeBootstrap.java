package io.littlehorse.canary.metronome;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.canary.util.Shutdown;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeBootstrap implements Bootstrap {

    public MetronomeBootstrap(
            final String metricsTopicName,
            final Map<String, Object> kafkaProducerConfigMap,
            final Map<String, Object> littleHorseConfigMap,
            final long frequency,
            final int threads,
            final int runs) {

        final LHConfig lhConfig = new LHConfig(littleHorseConfigMap);
        final LittleHorseBlockingStub lhClient = lhConfig.getBlockingStub();

        final MetricsEmitter emitter = new MetricsEmitter(metricsTopicName, kafkaProducerConfigMap);
        Shutdown.addShutdownHook(emitter);

        initializeWorker(emitter, lhConfig);
        initializeWorkflow(lhClient);
        initializeMetronome(emitter, lhClient, frequency, threads, runs);

        log.trace("Initialized");
    }

    private static void initializeMetronome(
            final MetricsEmitter emitter,
            final LittleHorseBlockingStub lhClient,
            final long frequency,
            final int threads,
            final int runs) {
        final Metronome metronome = new Metronome(emitter, lhClient, frequency, threads, runs);
        Shutdown.addShutdownHook(metronome);
    }

    private static void initializeWorkflow(final LittleHorseBlockingStub lhClient) {
        final MetronomeWorkflow workflow = new MetronomeWorkflow(lhClient);
        workflow.register();
    }

    private static void initializeWorker(final MetricsEmitter emitter, final LHConfig lhConfig) {
        final LHTaskWorker worker = new LHTaskWorker(new MetronomeTask(emitter), MetronomeWorkflow.TASK_NAME, lhConfig);
        Shutdown.addShutdownHook(worker);
        worker.registerTaskDef();
        worker.start();
    }
}
