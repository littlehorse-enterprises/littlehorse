package io.littlehorse.canary.aggregator.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.proto.Metadata;
import io.littlehorse.canary.proto.Metric;
import net.datafaker.Faker;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

class MetricTimeExtractorTest {

    Faker faker = new Faker();

    private static ConsumerRecord<Object, Object> newRecord(Object value) {
        return new ConsumerRecord<>("my-topic", 0, 0, "my-key", value);
    }

    @Test
    void returnDefaultTimeIfMetadataIsNotPresent() {
        MetricTimeExtractor extractor = new MetricTimeExtractor();

        long expectedTime = faker.number().randomNumber();
        ConsumerRecord<Object, Object> record = newRecord(Metric.newBuilder().build());
        long result = extractor.extract(record, expectedTime);

        assertThat(result).isEqualTo(expectedTime);
    }

    @Test
    void returnTheRightTimestamp() {
        MetricTimeExtractor extractor = new MetricTimeExtractor();

        long expectedTime = faker.number().randomNumber();
        long notExpectedTime = faker.number().randomNumber();
        Metric metric = Metric.newBuilder()
                .setMetadata(Metadata.newBuilder()
                        .setTime(Timestamps.fromMillis(expectedTime))
                        .build())
                .build();
        ConsumerRecord<Object, Object> record = newRecord(metric);
        long result = extractor.extract(record, notExpectedTime);

        assertThat(result).isEqualTo(expectedTime);
    }

    @Test
    void returnDefaultTimeIfWrongClassWassPassed() {
        MetricTimeExtractor extractor = new MetricTimeExtractor();

        long expectedTime = faker.number().randomNumber();
        ConsumerRecord<Object, Object> record = newRecord("Another class");
        long result = extractor.extract(record, expectedTime);

        assertThat(result).isEqualTo(expectedTime);
    }
}
