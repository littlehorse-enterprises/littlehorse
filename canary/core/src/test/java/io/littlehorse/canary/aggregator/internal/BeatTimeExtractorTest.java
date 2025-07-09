package io.littlehorse.canary.aggregator.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.proto.BeatValue;
import net.datafaker.Faker;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

class BeatTimeExtractorTest {

    Faker faker = new Faker();

    private static ConsumerRecord<Object, Object> newRecord(Object value) {
        return new ConsumerRecord<>("my-topic", 0, 0, "my-key", value);
    }

    @Test
    void shouldReturnDefaultTimeIfMetadataIsNotPresent() {
        BeatTimeExtractor extractor = new BeatTimeExtractor();

        long expectedTime = faker.number().randomNumber();
        ConsumerRecord<Object, Object> record = newRecord(BeatValue.newBuilder().build());
        long result = extractor.extract(record, expectedTime);

        assertThat(result).isEqualTo(expectedTime);
    }

    @Test
    void shouldExtractTimestampFromValue() {
        BeatTimeExtractor extractor = new BeatTimeExtractor();

        long expectedTime = faker.number().randomNumber();
        long notExpectedTime = faker.number().randomNumber();
        BeatValue metric = BeatValue.newBuilder()
                .setTime(Timestamps.fromMillis(expectedTime))
                .build();
        ConsumerRecord<Object, Object> record = newRecord(metric);
        long result = extractor.extract(record, notExpectedTime);

        assertThat(result).isEqualTo(expectedTime);
    }

    @Test
    void shouldReturnDefaultTimeIfWrongClassWasPassed() {
        BeatTimeExtractor extractor = new BeatTimeExtractor();

        long expectedTime = faker.number().randomNumber();
        ConsumerRecord<Object, Object> record = newRecord("Another class");
        long result = extractor.extract(record, expectedTime);

        assertThat(result).isEqualTo(expectedTime);
    }
}
