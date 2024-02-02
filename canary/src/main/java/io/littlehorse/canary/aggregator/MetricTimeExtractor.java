package io.littlehorse.canary.aggregator;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.proto.Metric;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

@Slf4j
public class MetricTimeExtractor implements TimestampExtractor {
    @Override
    public long extract(final ConsumerRecord<Object, Object> record, final long partitionTime) {
        if (!Metric.class.isInstance(record.value())) {
            log.warn(
                    "It's not possible to extract timestamp for class {}, using default timestamp",
                    record.value().getClass());
            return partitionTime;
        }

        final Metric metric = (Metric) record.value();
        return Timestamps.toMillis(metric.getMetadata().getTime());
    }
}
