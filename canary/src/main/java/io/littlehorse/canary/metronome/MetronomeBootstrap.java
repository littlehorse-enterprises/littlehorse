package io.littlehorse.canary.metronome;

import com.google.protobuf.Empty;
import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.canary.util.Shutdown;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.ServerVersionResponse;
import io.littlehorse.sdk.worker.LHTaskWorker;
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

        emitter = new MetricsEmitter(
                config.getTopicName(), config.toKafkaProducerConfig().toMap());

        final String serverVersion = getServerVersion(lhConfig);

        final MetronomeTask executable = new MetronomeTask(
                emitter, lhConfig.getApiBootstrapHost(), lhConfig.getApiBootstrapPort(), serverVersion);
        final LHTaskWorker worker = new LHTaskWorker(executable, MetronomeWorkflow.TASK_NAME, lhConfig);
        Shutdown.addShutdownHook("Metronome: LH Task Worker", worker);
        worker.registerTaskDef();
        worker.start();

        final MetronomeWorkflow workflow = new MetronomeWorkflow(lhClient);
        workflow.register();

        if (config.isMetronomeActiveModeEnabled()) {
            final Metronome metronome = new Metronome(
                    emitter,
                    lhClient,
                    config.getMetronomeFrequencyMs(),
                    config.getMetronomeThreads(),
                    config.getMetronomeRuns(),
                    lhConfig.getApiBootstrapHost(),
                    lhConfig.getApiBootstrapPort(),
                    serverVersion);
        }

        log.trace("Initialized");
    }

    private static String getServerVersion(final LHConfig lhConfig) {
        final ServerVersionResponse response = lhConfig.getBlockingStub().getServerVersion(Empty.getDefaultInstance());
        return "%s.%s.%s%s"
                .formatted(
                        response.getMajorVersion(),
                        response.getMinorVersion(),
                        response.getPatchVersion(),
                        response.hasPreReleaseIdentifier() ? "-" + response.getPreReleaseIdentifier() : "");
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        emitter.bindTo(registry);
    }
}
