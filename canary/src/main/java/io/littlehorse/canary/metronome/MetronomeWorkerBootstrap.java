package io.littlehorse.canary.metronome;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.canary.util.LHClientUtil;
import io.littlehorse.canary.util.Shutdown;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeWorkerBootstrap extends Bootstrap implements MeterBinder {

    private final MetricsEmitter emitter;

    public MetronomeWorkerBootstrap(final CanaryConfig config) {
        super(config);

        final LHConfig lhConfig = new LHConfig(config.toLittleHorseConfig().toMap());

        emitter = new MetricsEmitter(
                config.getTopicName(), config.toKafkaProducerConfig().toMap());

        final MetronomeTask executable = new MetronomeTask(
                emitter,
                lhConfig.getApiBootstrapHost(),
                lhConfig.getApiBootstrapPort(),
                LHClientUtil.getServerVersion(lhConfig.getBlockingStub()));

        final LHTaskWorker worker = new LHTaskWorker(executable, MetronomeWorkflow.TASK_NAME, lhConfig);
        Shutdown.addShutdownHook("Metronome: LH Task Worker", worker);
        worker.registerTaskDef();
        worker.start();

        log.info("Initialized");
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        emitter.bindTo(registry);
    }
}
