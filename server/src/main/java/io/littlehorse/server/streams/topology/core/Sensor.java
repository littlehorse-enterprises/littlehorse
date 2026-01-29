package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.sdk.common.proto.LHStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sensor {
    private static final Map<WfSpecIdModel, Map<LHStatus, CountAndTiming.Builder>> metrics = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static {
        scheduler.scheduleAtFixedRate(Sensor::printMetricsSummary, 5, 10, TimeUnit.SECONDS);
    }

    private static void printMetricsSummary() {
        System.out.println("=== WORKFLOW METRICS SUMMARY ===");
        printCurrentMetrics();
    }

    public static void printCurrentMetrics() {
        if (metrics.isEmpty()) {
            return;
        }
        System.out.println("Current Metrics:" + metrics);
        metrics.clear();
    }

    public static void track(WfRunModel wfRun) {
        WfSpecIdModel wfSpecId = wfRun.getWfSpecId();
        var status = wfRun.getStatus();
        long latencyMs = 0;
        if (status == LHStatus.COMPLETED) {
            latencyMs = wfRun.getEndTime().getTime() - wfRun.getStartTime().getTime();
        }
        Map<LHStatus, CountAndTiming.Builder> wfSpecMetrics = metrics.computeIfAbsent(wfSpecId, k -> new HashMap<>());
        CountAndTiming.Builder timing = wfSpecMetrics.computeIfAbsent(status, k -> CountAndTiming.newBuilder());
        timing.setCount(timing.getCount() + 1);
        if (status == LHStatus.COMPLETED) {
            timing.setTotalLatencyMs(timing.getTotalLatencyMs() + latencyMs);
        }
    }

    public Map<LHStatus, CountAndTiming> getMetrics(WfSpecIdModel wfSpecId) {
        Map<LHStatus, CountAndTiming.Builder> wfSpecMetrics = metrics.get(wfSpecId);
        if (wfSpecMetrics == null) {
            return new HashMap<>();
        }

        Map<LHStatus, CountAndTiming> result = new HashMap<>();
        for (Map.Entry<LHStatus, CountAndTiming.Builder> entry : wfSpecMetrics.entrySet()) {
            result.put(entry.getKey(), entry.getValue().build());
        }
        return result;
    }

    public static Map<WfSpecIdModel, Map<LHStatus, CountAndTiming>> getAllMetrics() {
        Map<WfSpecIdModel, Map<LHStatus, CountAndTiming>> result = new HashMap<>();
        for (Map.Entry<WfSpecIdModel, Map<LHStatus, CountAndTiming.Builder>> wfSpecEntry : metrics.entrySet()) {
            Map<LHStatus, CountAndTiming> transitionMetrics = new HashMap<>();
            for (Map.Entry<LHStatus, CountAndTiming.Builder> transitionEntry :
                    wfSpecEntry.getValue().entrySet()) {
                transitionMetrics.put(
                        transitionEntry.getKey(), transitionEntry.getValue().build());
            }
            result.put(wfSpecEntry.getKey(), transitionMetrics);
        }
        return result;
    }

    public void reset() {
        metrics.clear();
    }

    @Override
    public String toString() {
        return "Sensor{" + "metrics=" + getAllMetrics() + '}';
    }
}
