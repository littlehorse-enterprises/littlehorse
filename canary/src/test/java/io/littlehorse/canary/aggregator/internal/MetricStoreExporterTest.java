package io.littlehorse.canary.aggregator.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import io.littlehorse.canary.proto.MetricKey;
import io.littlehorse.canary.proto.MetricValue;
import io.littlehorse.canary.proto.Tag;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.time.Duration;
import java.util.List;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricStoreExporterTest {

    public static final String TEST_STORAGE = "testStorage";
    public static final String HOST = "localhost";

    @Mock
    KafkaStreams kafkaStreams;

    @Mock
    ReadOnlyKeyValueStore<MetricKey, MetricValue> store;

    @Mock
    KeyValueIterator<MetricKey, MetricValue> records;

    PrometheusMeterRegistry prometheusRegistry;
    MetricStoreExporter metricExporter;

    @BeforeEach
    void setUp() {
        metricExporter = new MetricStoreExporter(kafkaStreams, TEST_STORAGE, Duration.ofSeconds(10));
        prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        metricExporter.close();
        prometheusRegistry.close();
    }

    @Test
    public void shouldScrapeSimpleMetric() throws InterruptedException {
        // metrics
        List<Tag> tags = List.of(
                Tag.newBuilder().setKey("custom_tag").setValue("custom_value").build());
        MetricKey key = MetricKey.newBuilder()
                .setServerHost(HOST)
                .setServerPort(2023)
                .setServerVersion("test")
                .setId("my_metric")
                .addAllTags(tags)
                .build();
        MetricValue value = MetricValue.newBuilder().setValue(1.0).build();

        // records
        when(records.hasNext()).thenReturn(true, false);
        when(records.next()).thenReturn(KeyValue.pair(key, value));
        doNothing().when(records).close();

        // store
        when(store.all()).thenReturn(records);

        // kafka streams
        when(kafkaStreams.state()).thenReturn(KafkaStreams.State.RUNNING);
        when(kafkaStreams.store(any())).thenReturn(store);

        metricExporter.bindTo(prometheusRegistry);

        Thread.sleep(500);

        assertThat(prometheusRegistry.scrape())
                .contains(
                        "my_metric{custom_tag=\"custom_value\",server=\"localhost:2023\",server_version=\"test\",} 1.0");
    }
}
