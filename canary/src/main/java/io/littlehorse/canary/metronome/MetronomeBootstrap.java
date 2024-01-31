package io.littlehorse.canary.metronome;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.kafka.MetricsEmitter;
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
    private final LHTaskWorker worker;
    private final MetricsEmitter emitter;
    private final Metronome metronome;

    public MetronomeBootstrap(
            String topicName, Map<String, Object> kafkaProducerConfigMap, Map<String, Object> littleHorseConfigMap) {
        // Initialize kafka producer
        emitter = new MetricsEmitter(topicName, kafkaProducerConfigMap);
        metronome = new Metronome(emitter);
        metronome.start();

        // Initialize task worker
        LHConfig lhConfig = new LHConfig(littleHorseConfigMap);
        try {
            worker = new LHTaskWorker(new MetronomeTask(emitter), TASK_NAME, lhConfig);
            worker.registerTaskDef();
            worker.start();
        } catch (IOException e) {
            throw new CanaryException(e);
        }

        // Initialize workflow
        Workflow workflow = Workflow.newWorkflow(
                "canary-workflow",
                thread -> thread.execute(TASK_NAME, thread.addVariable(VARIABLE_NAME, VariableType.INT)));
        try {
            workflow.registerWfSpec(lhConfig.getBlockingStub());
        } catch (IOException e) {
            throw new CanaryException(e);
        }

        log.trace("Initialized");
    }

    @Override
    public void shutdown() {
        worker.close();
        emitter.close();
        metronome.close();
        log.trace("Shutdown");
    }
}
