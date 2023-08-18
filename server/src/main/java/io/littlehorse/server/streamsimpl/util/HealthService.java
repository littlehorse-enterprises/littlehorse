package io.littlehorse.server.streamsimpl.util;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.littlehorse.common.LHConfig;
import io.littlehorse.server.metrics.PrometheusMetricExporter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.Closeable;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;

@Slf4j
public class HealthService implements Closeable {

    private PrometheusMetricExporter prom;
    private Javalin server;
    private LHConfig config;

    private State coreState;
    private State timerState;

    public HealthService(LHConfig config, KafkaStreams coreStreams, KafkaStreams timerStreams) {
        this.prom = new PrometheusMetricExporter(config);
        this.prom.bind(coreStreams, timerStreams);
        this.server = Javalin.create();

        this.config = config;

        this.server.get(config.getPrometheusExporterPath(), prom.handleRequest());
        this.server.get(config.getLivenessPath(), this::getLiveness);
        this.server.get(config.getReadinessPath(), this::getReadiness);

        coreStreams.setStateListener(this::onCoreStateChange);
        timerStreams.setStateListener(this::onTimerStateChange);
    }

    public void start() {
        log.info("Starting health+metrics server");
        server.start(config.getHealthServicePort());
    }

    public MeterRegistry getMeterRegistry() {
        return prom.getMeterRegistry();
    }

    public void onTimerStateChange(State newState, State oldState) {
        log.info("New state for timer topology: {}", newState);
        this.timerState = newState;
    }

    public void onCoreStateChange(State newState, State oldState) {
        log.info("New state for core topology: {}", newState);
        this.coreState = newState;
    }

    private boolean isAlive(State state) {
        return state == State.RUNNING || state == State.REBALANCING;
    }

    private boolean canServeRequests(State state) {
        return state == State.RUNNING;
    }

    private void getLiveness(Context ctx) {
        if (isAlive(timerState) && isAlive(coreState)) {
            ctx.result("OK!");
        } else {
            ctx.status(500);
            ctx.result("Core state is " + coreState + " and timer is " + timerState);
        }
    }

    private void getReadiness(Context ctx) {
        if (canServeRequests(coreState) && isAlive(timerState)) {
            ctx.result("OK!");
        } else {
            ctx.status(500);
            ctx.result("Core state is " + coreState + " and timer is " + timerState);
        }
    }

    @Override
    public void close() {
        this.prom.close();
    }
}
