package io.littlehorse.server.monitoring.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;

public class ServerMetricFilter {

    private final MeterRegistry meterRegistry;
    private final List<ServerFilterRule> rules;

    public ServerMetricFilter(MeterRegistry meterRegistry, List<ServerFilterRule> rules) {
        this.meterRegistry = meterRegistry;
        this.rules = rules == null ? List.of() : rules;
    }

    public void initialize() {
        if (this.meterRegistry != null) {
            this.rules.forEach(rule -> meterRegistry.config().meterFilter(rule.getFilter()));
        }
    }
}
