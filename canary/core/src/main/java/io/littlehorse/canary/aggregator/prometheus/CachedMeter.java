package io.littlehorse.canary.aggregator.prometheus;

import com.google.common.util.concurrent.AtomicDouble;
import io.micrometer.core.instrument.Meter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
class CachedMeter {
    private Meter.Id id;

    @EqualsAndHashCode.Exclude
    private AtomicDouble meter;
}
