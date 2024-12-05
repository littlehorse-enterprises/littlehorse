package io.littlehorse.common;

import io.littlehorse.common.util.LHProducer;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;

public class MockLHProducer extends LHProducer {
    private final MockProducer<String, Bytes> mockProducer;

    private MockLHProducer(MockProducer<String, Bytes> mockProducer) {
        super(mockProducer);
        this.mockProducer = mockProducer;
    }

    public MockProducer<String, Bytes> getKafkaProducer() {
        return mockProducer;
    }

    public static MockLHProducer create() {
        return create(true);
    }

    public static MockLHProducer create(boolean autoComplete) {
        return new MockLHProducer(new MockProducer<>(
                autoComplete, Serdes.String().serializer(), Serdes.Bytes().serializer()));
    }
}
