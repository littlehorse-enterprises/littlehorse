package io.littlehorse.server.streams.topology.core;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.topology.core.LHEventBus.LHEvent;
import io.littlehorse.server.streams.topology.core.LHEventBus.LHWfRunEvent;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class MetricsAggregation implements LHEventBus.Subscriber {
    private final Map<Object, MetricCounter> metrics = new HashMap<>();
    @Override
    public void listen(LHEvent event) {
        Object newMetric;
        if (event instanceof LHWfRunEvent wfRunEvent) {
            newMetric = new WorkflowMetricCounter(
                    event.getWfSpecName(), event.getWfSpecVersion(), wfRunEvent.getNewStatus());
        } else {
            throw new IllegalArgumentException("");
        }
        MetricCounter counter = metrics.getOrDefault(newMetric, new MetricCounter());
        counter.increment(event.getCreationDate());
    }

    private static final class MetricCounter {
        private final AtomicInteger counter = new AtomicInteger(0);
        // Every counter increment creates a record in this sorted field.
        private final TreeSet<Date> eventDates = new TreeSet<>();

        private void increment(Date eventDate) {
            eventDates.add(eventDate);
            counter.incrementAndGet();
        }
    }

    private class WorkflowMetricCounter {
        private final String wfSpecName;
        private final int wfSpecVersion;
        private final LHStatus status;

        private WorkflowMetricCounter(String wfSpecName, int wfSpecVersion, LHStatus status) {
            this.wfSpecName = wfSpecName;
            this.wfSpecVersion = wfSpecVersion;
            this.status = status;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WorkflowMetricCounter that)) return false;
            return wfSpecVersion == that.wfSpecVersion
                    && Objects.equals(wfSpecName, that.wfSpecName)
                    && status == that.status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(wfSpecName, wfSpecVersion, status);
        }
    }
}
