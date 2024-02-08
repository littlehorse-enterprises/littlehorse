package io.littlehorse.canary.aggregator.internal;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.proto.Beat;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

@Slf4j
public class BeatTimeExtractor implements TimestampExtractor {
    @Override
    public long extract(final ConsumerRecord<Object, Object> record, final long partitionTime) {
        if (!Beat.class.isInstance(record.value())) {
            log.warn("Invalid class {}, using default timestamp", record.value().getClass());
            return partitionTime;
        }

        final Beat beat = (Beat) record.value();

        if (!beat.hasTime()) {
            log.warn("Timestamp is missing for key {}, using default timestamp", record.key());
            return partitionTime;
        }

        return Timestamps.toMillis(beat.getTime());
    }
}
