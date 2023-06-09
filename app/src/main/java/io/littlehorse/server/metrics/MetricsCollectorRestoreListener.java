package io.littlehorse.server.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.processor.StateRestoreListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsCollectorRestoreListener implements StateRestoreListener {

    private static final Logger log = LoggerFactory.getLogger(
        MetricsCollectorRestoreListener.class
    );

    private MeterRegistry registry;
    private List<Tag> extraTags;
    private Map<TopicPartition, Instant> startTimeCache;

    public MetricsCollectorRestoreListener(
        MeterRegistry registry,
        Map<String, String> extraTags
    ) {
        this.registry = registry;
        this.extraTags =
            extraTags
                .entrySet()
                .stream()
                .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                .toList();
        this.startTimeCache = new HashMap<>();
    }

    @Override
    public void onRestoreStart(
        final TopicPartition topicPartition,
        final String storeName,
        final long startingOffset,
        final long endingOffset
    ) {
        log.debug(
            "Started restoration of " +
            storeName +
            " partition " +
            topicPartition.partition() +
            " total records to be restored " +
            (endingOffset - startingOffset)
        );
        Counter counter = registry.counter(
            "kafka_stream_state_restoration_started",
            createTags(topicPartition, storeName)
        );
        counter.increment();

        AtomicLong gauge = registry.gauge(
            "kafka_stream_state_restoration_planned_records",
            createTags(topicPartition, storeName),
            new AtomicLong(0)
        );
        gauge.set(endingOffset - startingOffset);

        startTimeCache.put(topicPartition, Instant.now());
    }

    private List<Tag> createTags(
        final TopicPartition topicPartition,
        final String storeName
    ) {
        ArrayList<Tag> tags = new ArrayList<>(
            List.of(
                Tag.of("store", storeName),
                Tag.of("partition", String.valueOf(topicPartition.partition())),
                Tag.of("topic", topicPartition.topic())
            )
        );
        tags.addAll(extraTags);
        return tags;
    }

    @Override
    public void onBatchRestored(
        final TopicPartition topicPartition,
        final String storeName,
        final long batchEndOffset,
        final long numRestored
    ) {
        log.debug(
            "Restored batch " +
            numRestored +
            " for " +
            storeName +
            " partition " +
            topicPartition.partition()
        );

        AtomicLong gauge = registry.gauge(
            "kafka_stream_state_restoration_restored_records",
            createTags(topicPartition, storeName),
            new AtomicLong(0)
        );
        gauge.set(numRestored);
    }

    @Override
    public void onRestoreEnd(
        final TopicPartition topicPartition,
        final String storeName,
        final long totalRestored
    ) {
        log.debug(
            "Restoration complete for " +
            storeName +
            " partition " +
            topicPartition.partition()
        );

        Counter counter = registry.counter(
            "kafka_stream_state_restoration_ended",
            createTags(topicPartition, storeName)
        );
        counter.increment();

        Timer timer = registry.timer(
            "kafka_stream_state_restoration_latency",
            createTags(topicPartition, storeName)
        );
        timer.record(
            Duration.between(startTimeCache.get(topicPartition), Instant.now())
        );
    }
}
