package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.PartitionMetricsModel;
import io.littlehorse.common.model.LHStatusChangedModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.store.TenantModelStore;
import io.littlehorse.server.streams.topology.core.LHEventBus.LHEvent;
import io.littlehorse.server.streams.topology.core.LHEventBus.LHWfRunEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class MetricsAggregation implements LHEventBus.Subscriber {
    private final Map<Object, MetricCounter> metrics = new HashMap<>();
    private boolean dirtyState = false;
    private final TenantModelStore modelStore;
    private PartitionMetricsModel aggregateModel;

    public MetricsAggregation(TenantModelStore modelStore) {
        this.modelStore = modelStore;
    }

    @Override
    public void listen(LHEvent event) {
        if (event instanceof LHWfRunEvent wfRunEvent) {
            LHStatusChangedModel statusChanged = new LHStatusChangedModel(wfRunEvent.getPreviousStatus(), wfRunEvent.getNewStatus());
            currentAggregateCommand().addMetric(wfRunEvent.getWfSPecId(), wfRunEvent.getTenantId(), statusChanged, wfRunEvent.getCreationDate());
        } else {
            throw new IllegalArgumentException("");
        }
        dirtyState = true;
    }

    private PartitionMetricsModel currentAggregateCommand() {
        if(aggregateModel == null) {
            aggregateModel = Optional.ofNullable(modelStore.get("PEDRO", PartitionMetricsModel.class))
                    .orElse(new PartitionMetricsModel());
        }
        return aggregateModel;
    }
    public void maybePersistState() {
        if(dirtyState) {
            this.modelStore.put(currentAggregateCommand());
        }
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

    private static class WorkflowMetricCounter {
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
