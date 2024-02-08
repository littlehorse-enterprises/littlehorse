package io.littlehorse.canary.prometheus;

import com.google.common.base.Strings;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import java.util.List;

public class PrometheusMetricFilter implements MeterFilter {
    private final List<String> rules;

    public PrometheusMetricFilter(final List<String> rules) {
        if (rules == null) {
            this.rules = List.of();
        } else {
            this.rules = rules;
        }
    }

    @Override
    public MeterFilterReply accept(final Meter.Id id) {
        if (id == null) {
            return MeterFilterReply.DENY;
        }

        final String metricName = id.getName().replace(".", "_")
                + (Strings.isNullOrEmpty(id.getBaseUnit()) ? "" : "_" + id.getBaseUnit());

        for (String rule : rules) {
            if (rule.equals(metricName)) {
                return MeterFilterReply.ACCEPT;
            }
        }

        return MeterFilterReply.DENY;
    }
}
