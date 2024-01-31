package io.littlehorse.server.streams.topology.core.processors;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.WfMetricUpdateModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecMetricsIdModel;
import io.littlehorse.common.model.getable.repartitioned.workflowmetrics.WfSpecMetricsModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.AggregateWfMetricsModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AggregateWfMetricsRepartitionCommandTest {

    private final String commandId = UUID.randomUUID().toString();

    @Mock
    private LHServerConfig config;

    @Mock
    private KafkaStreamsServerImpl server;

    @Mock
    private ExecutionContext executionContext;

    private final MetadataCache metadataCache = new MetadataCache();
    private final WfSpecIdModel wfSpecId = new WfSpecIdModel("my-wf", 0, 1);
    private final TenantIdModel tenantId = new TenantIdModel("my-tenant");

    private final KeyValueStore<String, Bytes> nativeInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.CORE_REPARTITION_STORE),
                    Serdes.String(),
                    Serdes.Bytes())
            .withLoggingDisabled()
            .build();
    private final KeyValueStore<String, Bytes> nativeInMemoryGlobalStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.GLOBAL_METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();
    private final MockProcessorContext<Void, Void> mockProcessorContext = new MockProcessorContext<>();

    private RepartitionCommandProcessor commandProcessor;
    private TenantScopedStore defaultStore = TenantScopedStore.newInstance(
            nativeInMemoryStore, new TenantIdModel(LHConstants.DEFAULT_TENANT), executionContext);

    @BeforeEach
    public void setup() {
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
        nativeInMemoryGlobalStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryGlobalStore);
        commandProcessor = new RepartitionCommandProcessor(config, metadataCache);
    }

    @Test
    public void shouldAggregateWfMetricsInTheSameWindow() {
        Date windowStart = LHUtil.getWindowStart(new Date(), MetricsWindowLength.HOURS_2);
        WfMetricUpdateModel firstUpdate = new WfMetricUpdateModel(windowStart, MetricsWindowLength.HOURS_2, wfSpecId);
        firstUpdate.totalCompleted = 1L;
        firstUpdate.totalErrored = 2L;
        firstUpdate.totalStarted = 3L;
        firstUpdate.startToCompleteTotal = 7;
        commandProcessor.init(mockProcessorContext);
        RepartitionCommand firstMetricUpdate = new RepartitionCommand(
                new AggregateWfMetricsModel(wfSpecId, List.of(firstUpdate), tenantId), new Date(), commandId);
        Headers metadata = HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL);
        commandProcessor.process(new Record<>(commandId, firstMetricUpdate, 0L, metadata));

        WfMetricUpdateModel secondUpdate = new WfMetricUpdateModel(windowStart, MetricsWindowLength.HOURS_2, wfSpecId);
        secondUpdate.totalCompleted = 1L;
        secondUpdate.totalErrored = 2L;
        secondUpdate.totalStarted = 3L;
        secondUpdate.startToCompleteTotal = 30;
        RepartitionCommand secondMetricUpdate = new RepartitionCommand(
                new AggregateWfMetricsModel(wfSpecId, List.of(secondUpdate), tenantId), new Date(), commandId);
        commandProcessor.process(new Record<>(commandId, secondMetricUpdate, 0L, metadata));

        StoredGetable<WfSpecMetrics, WfSpecMetricsModel> storedMetric = defaultStore.get(
                WfSpecMetricsIdModel.getObjectId(windowStart, MetricsWindowLength.HOURS_2, wfSpecId)
                        .getStoreableKey(),
                StoredGetable.class);
        assertThat(storedMetric).isNotNull();
        WfSpecMetricsModel wfSpecMetrics = storedMetric.getStoredObject();
        assertThat(wfSpecMetrics.totalCompleted).isEqualTo(2);
        assertThat(wfSpecMetrics.totalErrored).isEqualTo(4);
        assertThat(wfSpecMetrics.totalStarted).isEqualTo(6);
        assertThat(wfSpecMetrics.startToCompleteAvg).isEqualTo(15);
    }

    @Test
    public void shouldAggregateWfMetricsInDifferentWindow() {
        Date firstWindowStart = LHUtil.getWindowStart(new Date(), MetricsWindowLength.HOURS_2);
        Date secondWindowStart = LHUtil.getWindowStart(DateUtils.addHours(new Date(), 3), MetricsWindowLength.HOURS_2);
        WfMetricUpdateModel firstUpdate =
                new WfMetricUpdateModel(firstWindowStart, MetricsWindowLength.HOURS_2, wfSpecId);
        firstUpdate.totalCompleted = 1L;
        firstUpdate.totalErrored = 2L;
        firstUpdate.totalStarted = 3L;
        commandProcessor.init(mockProcessorContext);
        RepartitionCommand firstMetricUpdate = new RepartitionCommand(
                new AggregateWfMetricsModel(wfSpecId, List.of(firstUpdate), tenantId), new Date(), commandId);
        Headers metadata = HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL);
        commandProcessor.process(new Record<>(commandId, firstMetricUpdate, 0L, metadata));

        WfMetricUpdateModel secondUpdate =
                new WfMetricUpdateModel(secondWindowStart, MetricsWindowLength.HOURS_2, wfSpecId);
        secondUpdate.totalCompleted = 1L;
        secondUpdate.totalErrored = 2L;
        secondUpdate.totalStarted = 3L;
        RepartitionCommand secondMetricUpdate = new RepartitionCommand(
                new AggregateWfMetricsModel(wfSpecId, List.of(secondUpdate), tenantId), new Date(), commandId);
        commandProcessor.process(new Record<>(commandId, secondMetricUpdate, 0L, metadata));

        StoredGetable<WfSpecMetrics, WfSpecMetricsModel> firstStoredMetric = defaultStore.get(
                WfSpecMetricsIdModel.getObjectId(firstWindowStart, MetricsWindowLength.HOURS_2, wfSpecId)
                        .getStoreableKey(),
                StoredGetable.class);
        assertThat(firstStoredMetric).isNotNull();
        WfSpecMetricsModel firstMetric = firstStoredMetric.getStoredObject();
        assertThat(firstMetric.totalCompleted).isEqualTo(1);
        assertThat(firstMetric.totalErrored).isEqualTo(2);
        assertThat(firstMetric.totalStarted).isEqualTo(3);

        StoredGetable<WfSpecMetrics, WfSpecMetricsModel> secondStoredMetric = defaultStore.get(
                WfSpecMetricsIdModel.getObjectId(secondWindowStart, MetricsWindowLength.HOURS_2, wfSpecId)
                        .getStoreableKey(),
                StoredGetable.class);
        assertThat(secondStoredMetric).isNotNull();
        WfSpecMetricsModel secondMetric = secondStoredMetric.getStoredObject();
        assertThat(secondMetric.totalCompleted).isEqualTo(1);
        assertThat(secondMetric.totalErrored).isEqualTo(2);
        assertThat(secondMetric.totalStarted).isEqualTo(3);
    }
}
