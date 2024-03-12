package io.littlehorse.canary.metronome;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.canary.util.LHClientUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeBootstrap extends Bootstrap implements MeterBinder {

    private final MetricsEmitter emitter;

    public MetronomeBootstrap(final CanaryConfig config) {
        super(config);

        final LHConfig lhConfig = new LHConfig(config.toLittleHorseConfig().toMap());
        final LittleHorseBlockingStub lhClient = lhConfig.getBlockingStub();

        final MetronomeWorkflow workflow = new MetronomeWorkflow(lhClient);
        workflow.register();

        emitter = new MetricsEmitter(
                config.getTopicName(), config.toKafkaProducerConfig().toMap());

        new Metronome(
                emitter,
                lhClient,
                config.getMetronomeFrequencyMs(),
                config.getMetronomeThreads(),
                config.getMetronomeRuns(),
                lhConfig.getApiBootstrapHost(),
                lhConfig.getApiBootstrapPort(),
                LHClientUtil.getServerVersion(lhClient));

        log.info("Initialized");
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        emitter.bindTo(registry);
    }
}
