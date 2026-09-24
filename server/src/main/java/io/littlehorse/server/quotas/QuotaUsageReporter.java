package io.littlehorse.server.quotas;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.QuotaIdModel;
import io.littlehorse.common.model.metadatacommand.subcommand.AggregateWindowMetricsModel;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuotaUsageReporter implements AutoCloseable {

    private final LHProducer producer;
    private final LHServerConfig config;
    private final Map<String, PartitionMetricWindowModel> windows = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public QuotaUsageReporter(LHProducer producer, LHServerConfig config) {
        this.producer = producer;
        this.config = config;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::reportClosedWindows, 1, 1, TimeUnit.SECONDS);
    }

    public void record(QuotaIdModel quotaId, boolean throttled, long throttleTimeMs) {
        Date windowStart = LHUtil.getCurrentWindowDate();
        MetricWindowIdModel id = new MetricWindowIdModel(quotaId.getTenant(), quotaId, windowStart);
        String key = id.getPartitionMetricStoreKey();
        windows.compute(key, (ignored, existing) -> {
            PartitionMetricWindowModel window = existing == null ? new PartitionMetricWindowModel(id) : existing;
            window.incrementQuotaUsage(throttled, throttleTimeMs);
            return window;
        });
    }

    void reportClosedWindows() {
        Date currentWindow = LHUtil.getCurrentWindowDate();
        for (Map.Entry<String, PartitionMetricWindowModel> entry : windows.entrySet()) {
            PartitionMetricWindowModel window = entry.getValue();
            if (window.getId().getWindowStart().before(currentWindow) && windows.remove(entry.getKey(), window)) {
                send(window);
            }
        }
    }

    private void send(PartitionMetricWindowModel window) {
        AggregateWindowMetricsModel aggregate = new AggregateWindowMetricsModel(window);
        CommandModel command = new CommandModel(aggregate);
        producer.send(
                        aggregate.getPartitionKey(),
                        command,
                        config.getRepartitionTopicName(),
                        HeadersUtil.metadataHeadersFor(
                                        window.getId().getTenantId(),
                                        new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL))
                                .toArray())
                .exceptionally(error -> {
                    log.warn("Unable to forward quota usage metrics for {}", window.getId(), error);
                    return null;
                });
    }

    @Override
    public void close() {
        reportClosedWindows();
        scheduler.shutdownNow();
    }
}
