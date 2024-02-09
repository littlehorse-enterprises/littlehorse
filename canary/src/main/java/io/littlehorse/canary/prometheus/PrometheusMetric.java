package io.littlehorse.canary.prometheus;

import com.google.common.util.concurrent.AtomicDouble;
import io.micrometer.core.instrument.Meter;

class PrometheusMetric {
    final Meter.Id id;
    final AtomicDouble meter;

    PrometheusMetric(final Meter.Id id, final AtomicDouble meter) {
        this.id = id;
        this.meter = meter;
    }
}
