package io.littlehorse.server.monitoring.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;

public class ServerFilterRule {

    private String prefix;
    private MeterFilterReply condition;

    public ServerFilterRule(String prefix, MeterFilterReply condition) {
        this.prefix = prefix == null ? "" : prefix;
        this.condition = condition == null ? MeterFilterReply.NEUTRAL : condition;
    }

    public String getPrefix() {
        return prefix.replace("_", ".");
    }

    public MeterFilterReply getCondition() {
        return condition;
    }

    public MeterFilter getFilter() {
        return new MeterFilter() {
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                if (id.getName().startsWith(getPrefix())) {
                    return getCondition();
                }
                return MeterFilterReply.NEUTRAL;
            }
        };
    }
}
