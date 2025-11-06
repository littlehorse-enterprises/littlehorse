package io.littlehorse.server.monitoring;

import com.google.gson.Gson;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.health.InProgressRestoration;
import io.littlehorse.server.monitoring.health.ServerHealthState;
import io.littlehorse.server.monitoring.http.ContentType;
import io.littlehorse.server.monitoring.http.LHHttpException;
import io.littlehorse.server.monitoring.http.StatusServer;
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
    private final StatusServer statusServer;
    private final Gson gson = new Gson();
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
        this.statusServer = new StatusServer();

        this.numberOfPartitionPerTopic = config.partitionsByTopic();

        this.coreState = new InstanceState(coreStreams, internalComms);
        this.prom.bind(
                coreStreams,
                timerStreams,
                taskQueueManager,
                metadataCache,
                new StandbyMetrics(standbyStores, config.getLHInstanceName()),
                coreState);

        this.coreStreams = coreStreams;
        this.timerStreams = timerStreams;

        this.config = config;
        this.restorations = new ConcurrentHashMap<>();
        statusServer.handle(config.getPrometheusExporterPath(), ContentType.TEXT, () -> prom.handleRequest());
        statusServer.handle(config.getLivenessPath(), ContentType.TEXT, this::getLiveness);
        statusServer.handle(config.getReadinessPath(), ContentType.TEXT, this::getReadiness);
        statusServer.handle(config.getStatusPath(), ContentType.JSON, this::getStatus);
        statusServer.handle(config.getDiskUsagePath(), ContentType.JSON, this::getDiskUsage);
        statusServer.handle(config.getStandbyStatusPath(), ContentType.JSON, this::getStandbyStatus);

        coreStreams.setStandbyUpdateListener(this);
        coreStreams.setGlobalStateRestoreListener(this);
        timerStreams.setGlobalStateRestoreListener(this);
        timerStreams.setStandbyUpdateListener(this);

        coreStreams.setStateListener(coreState);
        timerStreams.setStateListener((newState, oldState) -> {
            log.debug("New state for timer topology: {}", newState);
            timerState = newState;
        });
    }

    public void start() {
        log.info("Starting health+metrics server");
        statusServer.start(config.getHealthServicePort());
    }

    public MeterRegistry getMeterRegistry() {
        return prom.getMeterRegistry();
    }

    @Override
    public void onRestoreStart(TopicPartition tp, String storeName, long startingOffset, long endingOffset) {
        log.debug("Starting restoration for store {} partition {}", storeName, tp.partition());
        restorations.put(tp, new InProgressRestoration(tp, storeName, startingOffset, endingOffset, config));
    }

    @Override
    public void onBatchRestored(TopicPartition tp, String storeName, long batchEndOffset, long numRestored) {
        restorations.get(tp).onBatchRestored(batchEndOffset, numRestored);
    }

    @Override
    public void onRestoreEnd(TopicPartition tp, String storeName, long totalRestored) {
        log.debug("Completed restoration for store {} partition {}", storeName, tp.partition());
        restorations.remove(tp);
    }

    @Override
    public void onRestoreSuspended(TopicPartition tp, String storeName, long totalRestored) {
        // This is harmless; it means the Task was migrated to another instance.
        log.debug("Suspending restoration for store {} partition {}", storeName, tp.partition());
        restorations.remove(tp);
    }

    private String getStandbyStatus() {
        try {
            return gson.toJson(standbyStores);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getReadiness() {
        // We can only answer requests when the state is RUNNING or REBALANCING (when REBALANCING,
        // it is possible we are restoring state, which means that some partitions might actually
        // be alive, and those partitions can answer requests).
        Predicate<State> isReady = state -> state == State.RUNNING || state == State.REBALANCING;

        if (isReady.test(coreState.getCurrentState())) {
            return "OK!";
        } else {
            throw new LHHttpException("Core topology is not ready to receive traffic");
        }
    }

    private String getLiveness() {
        Predicate<State> isAlive = (state) -> {
            switch (state) {
                case CREATED:
                case RUNNING:
                case REBALANCING:
                case PENDING_SHUTDOWN:
                    return true;
                case PENDING_ERROR:
                case ERROR:
                case NOT_RUNNING:
            }
            return false;
        };

        if (isAlive.test(coreState.getCurrentState()) && isAlive.test(timerState)) {
            return "OK!";
        } else {
            throw new LHHttpException("Core topology or Timer Topology has an error");
        }
    }

    private String getStatus() {
        try {
            ServerHealthState result =
                    new ServerHealthState(config, coreStreams, timerStreams, restorations, standbyStores);
            return gson.toJson(result);
        } catch (Exception exn) {
            throw new RuntimeException(exn);
        }
    }

    private String getDiskUsage() {
        return gson.toJson(Map.of("diskUsageBytes", FileUtils.sizeOfDirectory(new File(config.getStateDirectory()))));
    }

    @Override
    public void close() {
        this.prom.close();
        this.statusServer.close();
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
