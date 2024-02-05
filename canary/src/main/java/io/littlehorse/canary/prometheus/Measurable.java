package io.littlehorse.canary.prometheus;

import io.micrometer.core.instrument.MeterRegistry;

public interface Measurable {

    void bindTo(final MeterRegistry registry);
}
