package io.littlehorse.canary.aggregator.prometheus;

import io.micrometer.core.instrument.Tag;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
class PrometheusMetric {
    private String id;
    private List<Tag> tags;

    @EqualsAndHashCode.Exclude
    private Double value;
}
