package io.littlehorse.server.metrics;


import io.littlehorse.TestUtil;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.MeasurableObject;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import io.littlehorse.storeinternals.InMemoryGetableManager;
import io.littlehorse.storeinternals.InMemoryMetadataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

class SensorTest {

    private final ProcessorExecutionContext executionContext = Mockito.mock(ProcessorExecutionContext.class);

    private final InMemoryGetableManager inMemoryGetableManager = new InMemoryGetableManager(executionContext);
    private final InMemoryMetadataManager inMemoryMetadataManager = new InMemoryMetadataManager();
    private final AuthorizationContext auth = Mockito.mock(AuthorizationContextImpl.class);

    @BeforeEach
    public void setup() {
        Mockito.when(executionContext.metadataManager()).thenReturn(inMemoryMetadataManager);
        Mockito.when(executionContext.getableManager()).thenReturn(inMemoryGetableManager);
        Mockito.when(executionContext.authorization()).thenReturn(auth);
    }

    @Test
    public void shouldRecordMetrics() {
        MetricSpecIdModel metricSpecId = new MetricSpecIdModel(MeasurableObject.WORKFLOW);
        Sensor sensor = new Sensor(Set.of(metricSpecId), executionContext);
        sensor.record(new WfRunStatusUpdate(TestUtil.wfSpecId(), null, LHStatus.RUNNING, LHStatus.COMPLETED));
    }

}