package io.littlehorse.canary.proto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import io.littlehorse.canary.util.MetricFactory;
import org.junit.jupiter.api.Test;

class MetricFactoryTest {

    public static final String ID = "my_metric";
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 2023;
    public static final String SERVER_VERSION = "1.0.0";
    public static final double VALUE = 10.;

    @Test
    public void buildMetricBeat() {
        MetricKey key = MetricFactory.buildKey(ID, SERVER_HOST, SERVER_PORT, SERVER_VERSION);
        MetricValue value = MetricFactory.buildValue(VALUE);

        assertThat(key)
                .isEqualTo(MetricKey.newBuilder()
                        .setId("canary_" + ID)
                        .addTags(Tag.newBuilder().setKey("server").setValue(SERVER_HOST + ":" + SERVER_PORT))
                        .addTags(Tag.newBuilder().setKey("server_version").setValue(SERVER_VERSION))
                        .build());
        assertThat(value).isEqualTo(MetricValue.newBuilder().setValue(VALUE).build());
    }

    @Test
    public void buildMetricBeatWithExtraTags() {
        String myKey = "MyKey";
        String myValue = "MyValue";
        MetricKey key = MetricFactory.buildKey(
                ID,
                SERVER_HOST,
                SERVER_PORT,
                SERVER_VERSION,
                List.of(Tag.newBuilder().setKey(myKey).setValue(myValue).build()));

        assertThat(key)
                .isEqualTo(MetricKey.newBuilder()
                        .setId("canary_" + ID)
                        .addTags(Tag.newBuilder().setKey("server").setValue(SERVER_HOST + ":" + SERVER_PORT))
                        .addTags(Tag.newBuilder().setKey("server_version").setValue(SERVER_VERSION))
                        .addTags(Tag.newBuilder().setKey(myKey).setValue(myValue))
                        .build());
    }
}
