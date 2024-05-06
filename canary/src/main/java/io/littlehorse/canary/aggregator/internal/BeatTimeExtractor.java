package io.littlehorse.canary.aggregator.internal;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.proto.BeatValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

@Slf4j
public class BeatTimeExtractor implements TimestampExtractor {

    @Override
    public long extract(final ConsumerRecord<Object, Object> record, final long partitionTime) {
        if (!(record.value() instanceof BeatValue beat)) {
            log.warn("Invalid class {}, using default timestamp", record.value().getClass());
            return partitionTime;
        }

        if (!beat.hasTime()) {
            log.warn("Timestamp is missing for key {}, using default timestamp", record.key());
            return partitionTime;
        }

        return Timestamps.toMillis(beat.getTime());
    }
}
