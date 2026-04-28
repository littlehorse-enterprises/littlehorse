package io.littlehorse.server.monitoring.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;

public class ServerFilterRule {

    private String prefix;
    private final MeterFilterReply condition;
    private final String tag;

    public ServerFilterRule(String prefix, MeterFilterReply condition) {
        this(prefix, null, condition);
    }

    public ServerFilterRule(String prefix, String tag, MeterFilterReply condition) {
        this.prefix = prefix == null ? "" : prefix;
        this.condition = condition == null ? MeterFilterReply.NEUTRAL : condition;
        this.tag = tag;
    }

    public String getPrefix() {
        return prefix.replace("_", ".");
    }

    public MeterFilter getFilter() {
        return tag != null ? new TagMeterFilter() : new PrefixBasedMeterFilter();
    }

    private class PrefixBasedMeterFilter implements MeterFilter {
        @Override
        public MeterFilterReply accept(Meter.Id id) {
            if (id.getName().startsWith(getPrefix())) {
                return condition;
            }
            return MeterFilterReply.NEUTRAL;
        }
    }

    private class TagMeterFilter extends PrefixBasedMeterFilter {

        @Override
        public MeterFilterReply accept(Meter.Id id) {
            MeterFilterReply prefixResult = super.accept(id);
            if (id.getTag(tag) != null) {
                return prefixResult;
            }
            return MeterFilterReply.NEUTRAL;
        }
    }
}
