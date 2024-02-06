package io.littlehorse.canary.prometheus;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import java.util.List;

public class PrometheusMetricFilter implements MeterFilter {
    public static final List<String> RULES = List.of("jvm_memory_used_bytes");

    @Override
    public MeterFilterReply accept(final Meter.Id id) {
        if (id == null) {
            return MeterFilterReply.DENY;
        }

        final String metricName = "%s_%s".formatted(id.getName().replace(".", "_"), id.getBaseUnit());

        for (String rule : RULES) {
            if (rule.equals(metricName)) {
                return MeterFilterReply.ACCEPT;
            }
        }

        return MeterFilterReply.DENY;
    }
}
