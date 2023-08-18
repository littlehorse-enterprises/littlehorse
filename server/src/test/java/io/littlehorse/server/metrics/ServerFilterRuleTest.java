package io.littlehorse.server.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.config.MeterFilterReply;
import org.junit.jupiter.api.Test;

public class ServerFilterRuleTest {

    @Test
    void shouldBeNeutralWhenIsAnotherMetric() {
        ServerFilterRule rule = new ServerFilterRule("random.metric", MeterFilterReply.DENY);

        assertThat(rule.getFilter().accept(new Id("random.not.my.metric", null, null, null, null)))
                .isEqualTo(MeterFilterReply.NEUTRAL);
    }

    @Test
    void denyMetric() {
        ServerFilterRule rule = new ServerFilterRule("random.metric", MeterFilterReply.DENY);

        assertThat(
                        rule.getFilter()
                                .accept(
                                        new Id(
                                                "random.metric.other.values",
                                                null,
                                                null,
                                                null,
                                                null)))
                .isEqualTo(MeterFilterReply.DENY);
    }

    @Test
    void acceptMetricWithUnderscore() {
        ServerFilterRule rule = new ServerFilterRule("random_metric", MeterFilterReply.ACCEPT);

        assertThat(
                        rule.getFilter()
                                .accept(
                                        new Id(
                                                "random.metric.other.values",
                                                null,
                                                null,
                                                null,
                                                null)))
                .isEqualTo(MeterFilterReply.ACCEPT);
    }

    @Test
    void acceptMetric() {
        ServerFilterRule rule = new ServerFilterRule("random.metric", MeterFilterReply.ACCEPT);

        assertThat(
                        rule.getFilter()
                                .accept(
                                        new Id(
                                                "random.metric.other.values",
                                                null,
                                                null,
                                                null,
                                                null)))
                .isEqualTo(MeterFilterReply.ACCEPT);
    }

    @Test
    void testGetMetricPrefix() {
        ServerFilterRule rule = new ServerFilterRule("random.metric", MeterFilterReply.ACCEPT);

        assertThat(rule.getPrefix()).isEqualTo("random.metric");
    }

    @Test
    void testGetMetricPrefixWithUnderscore() {
        ServerFilterRule rule = new ServerFilterRule("random_metric", MeterFilterReply.ACCEPT);

        assertThat(rule.getPrefix()).isEqualTo("random.metric");
    }
}
