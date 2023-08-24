package io.littlehorse.unit.server.metrics;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.littlehorse.server.metrics.ServerFilterRule;
import io.littlehorse.server.metrics.ServerMetricFilter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ServerMetricFilterTest {

    @Test
    void itAddAllTheProvidedFilterRules() {
        MeterRegistry registry = mock(RETURNS_DEEP_STUBS);
        ServerFilterRule mockRule1 = mock(RETURNS_DEEP_STUBS);
        ServerFilterRule mockRule2 = mock(RETURNS_DEEP_STUBS);
        List<ServerFilterRule> rules = List.of(mockRule1, mockRule2);

        new ServerMetricFilter(registry, rules).initialize();

        verify(registry.config()).meterFilter(mockRule1.getFilter());
        verify(registry.config()).meterFilter(mockRule2.getFilter());
    }
}
