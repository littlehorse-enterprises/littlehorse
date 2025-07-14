package io.littlehorse.server.monitoring;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.health.InProgressRestoration;
import io.littlehorse.server.monitoring.health.ServerHealthState;
import io.littlehorse.server.monitoring.metrics.InstanceState;
import io.littlehorse.server.monitoring.metrics.PrometheusMetricExporter;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.util.MetadataCache;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.Closeable;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.processor.StandbyUpdateListener;
import org.apache.kafka.streams.processor.StateRestoreListener;
import org.apache.kafka.streams.processor.TaskId;

@Slf4j
public class HealthService implements Closeable, StateRestoreListener, StandbyUpdateListener {

    private PrometheusMetricExporter prom;
    private Javalin server;
    private LHServerConfig config;

    private Map<TopicPartition, InProgressRestoration> restorations;
    private final Map<String, Integer> numberOfPartitionPerTopic;
    private InstanceState coreState;
    private final Map<String, StandbyStoresOnInstance> standbyStores = new ConcurrentHashMap<>();
    private State timerState;

    private KafkaStreams coreStreams;
    private KafkaStreams timerStreams;

    public HealthService(
            LHServerConfig config,
            KafkaStreams coreStreams,
            KafkaStreams timerStreams,
            TaskQueueManager taskQueueManager,
            MetadataCache metadataCache,
            BackendInternalComms internalComms) {
        this.prom = new PrometheusMetricExporter(config);
        this.numberOfPartitionPerTopic = config.partitionsByTopic();

        this.coreState = new InstanceState(coreStreams, internalComms);
        this.prom.bind(
                coreStreams,
                timerStreams,
                taskQueueManager,
                metadataCache,
                new StandbyMetrics(standbyStores, config.getLHInstanceName()),
                coreState);
        this.server = Javalin.create(c -> {
            c.useVirtualThreads = true;
        });

        this.coreStreams = coreStreams;
        this.timerStreams = timerStreams;

        this.config = config;
        this.restorations = new ConcurrentHashMap<>();

        this.server.get(config.getPrometheusExporterPath(), prom.handleRequest());
        this.server.get(config.getLivenessPath(), this::getLiveness);
        this.server.get(config.getStatusPath(), this::getStatus);
        this.server.get(config.getDiskUsagePath(), this::getDiskUsage);
        this.server.get(config.getStandbyStatusPath(), this::getStandbyStatus);

        coreStreams.setStandbyUpdateListener(this);
        coreStreams.setGlobalStateRestoreListener(this);
        timerStreams.setGlobalStateRestoreListener(this);
        timerStreams.setStandbyUpdateListener(this);

        coreStreams.setStateListener(coreState);
        timerStreams.setStateListener((newState, oldState) -> {
            log.info("New state for timer topology: {}", newState);
            timerState = newState;
        });
    }

    public void start() {
        log.info("Starting health+metrics server");
        server.start(config.getHealthServicePort());
    }

    public MeterRegistry getMeterRegistry() {
        return prom.getMeterRegistry();
    }

    @Override
    public void onRestoreStart(TopicPartition tp, String storeName, long startingOffset, long endingOffset) {
        log.info("Starting restoration for store {} partition {}", storeName, tp.partition());
        restorations.put(tp, new InProgressRestoration(tp, storeName, startingOffset, endingOffset, config));
    }

    @Override
    public void onBatchRestored(TopicPartition tp, String storeName, long batchEndOffset, long numRestored) {
        restorations.get(tp).onBatchRestored(batchEndOffset, numRestored);
    }

    @Override
    public void onRestoreEnd(TopicPartition tp, String storeName, long totalRestored) {
        log.info("Completed restoration for store {} partition {}", storeName, tp.partition());
        restorations.remove(tp);
    }

    @Override
    public void onRestoreSuspended(TopicPartition tp, String storeName, long totalRestored) {
        // This is harmless; it means the Task was migrated to another instance.
        log.info("Suspending restoration for store {} partition {}", storeName, tp.partition());
        restorations.remove(tp);
    }

    private void getStandbyStatus(Context ctx) {
        try {
            ctx.json(standbyStores);
        } catch (Exception e) {
            ctx.status(500);
            log.error(e.getMessage());
        }
    }

    private void getLiveness(Context ctx) {
        Predicate<State> isAlive = state -> state == State.RUNNING || state == State.REBALANCING;

        if (isAlive.test(timerState) && isAlive.test(coreState.getCurrentState())) {
            ctx.result("OK!");
        } else {
            ctx.status(500);
            ctx.result("Core state is " + coreState + " and timer is " + timerState);
        }
    }

    private void getStatus(Context ctx) {
        try {
            ServerHealthState result =
                    new ServerHealthState(config, coreStreams, timerStreams, restorations, standbyStores);
            ctx.json(result);
        } catch (Exception exn) {
            exn.printStackTrace();
        }
    }

    private void getDiskUsage(Context ctx) {
        ctx.json(Map.of("diskUsageBytes", FileUtils.sizeOfDirectory(new File(config.getStateDirectory()))));
    }

    @Override
    public void close() {
        this.prom.close();
    }

    @Override
    public void onUpdateStart(TopicPartition topicPartition, String storeName, long startingOffset) {
        StandbyStoresOnInstance instanceStore = standbyStores.getOrDefault(
                storeName,
                new StandbyStoresOnInstance(storeName, numberOfPartitionPerTopic.get(topicPartition.topic())));
        instanceStore.recordOffsets(topicPartition, startingOffset, -1);
        standbyStores.put(storeName, instanceStore);
    }

    @Override
    public void onBatchLoaded(
            TopicPartition topicPartition,
            String storeName,
            TaskId taskId,
            long batchEndOffset,
            long batchSize,
            long currentEndOffset) {
        StandbyStoresOnInstance instanceStore = standbyStores.getOrDefault(
                storeName,
                new StandbyStoresOnInstance(storeName, numberOfPartitionPerTopic.get(topicPartition.topic())));
        instanceStore.recordOffsets(topicPartition, batchEndOffset, currentEndOffset);
        standbyStores.put(storeName, instanceStore);
    }

    @Override
    public void onUpdateSuspended(
            TopicPartition topicPartition,
            String storeName,
            long storeOffset,
            long currentEndOffset,
            SuspendReason reason) {
        StandbyStoresOnInstance instanceStore = standbyStores.getOrDefault(
                storeName,
                new StandbyStoresOnInstance(storeName, numberOfPartitionPerTopic.get(topicPartition.topic())));
        instanceStore.suspendPartition(topicPartition, storeOffset, currentEndOffset, reason);
        standbyStores.put(storeName, instanceStore);
    }
}
