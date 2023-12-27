package io.littlehorse.server.monitoring;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.health.ServerHealthState;
import io.littlehorse.server.monitoring.metrics.PrometheusMetricExporter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.Closeable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.processor.StateRestoreListener;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Slf4j
public class HealthService implements Closeable, StateRestoreListener {

    private PrometheusMetricExporter prom;
    private Javalin server;
    private LHServerConfig config;

    private Map<TopicPartition, InProgressRestoration> restorations;
    private State coreState;
    private State timerState;

    private KafkaStreams coreStreams;
    private KafkaStreams timerStreams;

    public HealthService(LHServerConfig config, KafkaStreams coreStreams, KafkaStreams timerStreams) {
        this.prom = new PrometheusMetricExporter(config);
        this.prom.bind(coreStreams, timerStreams);
        this.server = Javalin.create();

        this.coreStreams = coreStreams;
        this.timerStreams = timerStreams;

        this.config = config;
        this.restorations = new HashMap<>();

        this.server.get(config.getPrometheusExporterPath(), prom.handleRequest());
        this.server.get(config.getLivenessPath(), this::getLiveness);
        this.server.get(config.getStatusPath(), this::getStatus);
        this.server.get(config.getDiskUsagePath(), this::getDiskUsage);

        this.server.get("/dumpstore", ctx -> {
            ReadOnlyKeyValueStore<String, Bytes> store = coreStreams.store(
                StoreQueryParameters.fromNameAndType("global-metadata-store", QueryableStoreTypes.keyValueStore())
            );
            try (KeyValueIterator<String, Bytes> iter = store.all()) {
                while (iter.hasNext()) {
                    System.out.println(iter.next().key);
                }
            }
        });

        coreStreams.setGlobalStateRestoreListener(this);
        timerStreams.setGlobalStateRestoreListener(this);

        coreStreams.setStateListener((newState, oldState) -> {
            log.info("New state for core topology: {}", newState);
            coreState = newState;
        });
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

    private void getLiveness(Context ctx) {
        Predicate<State> isAlive = state -> state == State.RUNNING || state == State.REBALANCING;

        if (isAlive.test(timerState) && isAlive.test(coreState)) {
            ctx.result("OK!");
        } else {
            ctx.status(500);
            ctx.result("Core state is " + coreState + " and timer is " + timerState);
        }
    }

    private void getStatus(Context ctx) {
        try {
            ServerHealthState result = new ServerHealthState(config, coreStreams, timerStreams, restorations);
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
}
