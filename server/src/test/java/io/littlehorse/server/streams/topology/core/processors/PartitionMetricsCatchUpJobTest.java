package io.littlehorse.server.streams.topology.core.processors;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricWindowType;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.MetricsHintModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.background.PartitionActionApplier;
import io.littlehorse.server.streams.topology.core.background.PartitionActionScheduler;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.TaskId;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Exercises the historical metric-window replay end to end: the job runs on a real background
 * worker, reads through a store handed out by a stubbed {@link CoreStoreProvider}, and the resulting
 * actions are applied by a real {@link PartitionActionApplier} on the test thread.
 */
@ExtendWith(MockitoExtension.class)
public class PartitionMetricsCatchUpJobTest {

    private static final String CORE_CMD_TOPIC = "core-cmd-topic";
    private static final String TENANT_ID = "my-tenant";
    private static final TaskId TASK_ID = new TaskId(0, 0);
    private static final long ONE_MINUTE = 60_000L;

    @Mock
    private LHServerConfig config;

    @Mock
    private CoreStoreProvider storeProvider;

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();
    private final BackgroundContext context = new BackgroundContext();

    private KeyValueStore<String, Bytes> nativeCoreStore;
    private ClusterScopedStore coreStore;
    private PartitionActionApplier applier;
    private PartitionActionScheduler scheduler;
    private PartitionMetricsCatchUpJob job;

    @BeforeEach
    void setup() {
        nativeCoreStore = TestUtil.testStore(ServerTopology.CORE_STORE);
        nativeCoreStore.init(mockProcessorContext.getStateStoreContext(), nativeCoreStore);
        coreStore = ClusterScopedStore.newInstance(nativeCoreStore, context);
        applier = new PartitionActionApplier(mockProcessorContext, nativeCoreStore);
        when(storeProvider.nativeCoreStore(TASK_ID.partition())).thenReturn(nativeCoreStore);
        job = new PartitionMetricsCatchUpJob(config);
    }

    @AfterEach
    void tearDown() {
        if (scheduler != null) {
            scheduler.close();
        }
    }

    @Test
    void shouldCompleteImmediatelyWhenThereIsNoHint() {
        start();

        await().until(job::isComplete);
        assertThat(mockProcessorContext.forwarded()).isEmpty();
    }

    @Test
    void shouldReplayPersistedWindowsAndThenComplete() {
        when(config.getCoreCmdTopicName()).thenReturn(CORE_CMD_TOPIC);
        seedHint(minutesAgo(10));
        seedWindow("wf-a", minutesAgo(5));
        seedWindow("wf-b", minutesAgo(4));

        start();
        drainUntil(() -> mockProcessorContext.forwarded().size() == 4);

        await().until(job::isComplete);
        // Two windows, each producing a spec-level and a tenant-level aggregate.
        assertThat(mockProcessorContext.forwarded()).hasSize(4);
    }

    @Test
    void shouldEmitOneSpecLevelAndOneTenantLevelAggregatePerWindow() {
        when(config.getCoreCmdTopicName()).thenReturn(CORE_CMD_TOPIC);
        seedHint(minutesAgo(10));
        seedWindow("wf-a", minutesAgo(5));

        start();
        drainUntil(() -> mockProcessorContext.forwarded().size() == 2);

        List<MetricWindowIdModel> ids = forwardedWindowIds();
        assertThat(ids).hasSize(2);

        // This is the aliasing regression: the tenant-level aggregate is produced by clearing the
        // wfSpecId, and because actions are applied lazily the two records must not share a window.
        assertThat(ids.get(0).getWfSpecId()).isNotNull();
        assertThat(ids.get(0).getMetricType()).isEqualTo(MetricWindowType.WORKFLOW_METRIC);
        assertThat(ids.get(1).getWfSpecId()).isNull();

        // ...and the partition keys must differ accordingly.
        assertThat(mockProcessorContext.forwarded().get(0).record().key())
                .isNotEqualTo(mockProcessorContext.forwarded().get(1).record().key());
    }

    @Test
    void shouldDeleteReplayedWindowsFromTheStore() {
        when(config.getCoreCmdTopicName()).thenReturn(CORE_CMD_TOPIC);
        seedHint(minutesAgo(10));
        PartitionMetricWindowModel window = seedWindow("wf-a", minutesAgo(5));
        assertThat(coreStore.get(window.getStoreKey(), PartitionMetricWindowModel.class))
                .isNotNull();

        start();
        drainUntil(() -> mockProcessorContext.forwarded().size() == 2);

        assertThat(coreStore.get(window.getStoreKey(), PartitionMetricWindowModel.class))
                .isNull();
    }

    @Test
    void shouldAdvanceTheHintOnceHistoryIsReplayed() {
        when(config.getCoreCmdTopicName()).thenReturn(CORE_CMD_TOPIC);
        long originalHint = minutesAgo(10);
        seedHint(originalHint);
        seedWindow("wf-a", minutesAgo(5));

        start();
        await().until(job::isComplete);
        drainUntil(() -> hint() > originalHint);

        assertThat(hint()).isGreaterThan(originalHint);
    }

    @Test
    void shouldIgnoreWindowsNewerThanTheServerStartBoundary() {
        // No getCoreCmdTopicName() stub on purpose: if anything were forwarded, building the record
        // would need it, so strict stubbing doubles as an assertion that nothing was replayed.
        seedHint(minutesAgo(10));
        // Belongs to the window that is open right now, so it is owned by the Streams thread and
        // must be left alone by the background replay.
        PartitionMetricWindowModel current =
                seedWindow("wf-current", LHUtil.getCurrentWindowDate().getTime());

        start();
        await().until(job::isComplete);
        scheduler.drain(applier, Duration.ofMinutes(1), 1000);

        assertThat(mockProcessorContext.forwarded()).isEmpty();
        assertThat(coreStore.get(current.getStoreKey(), PartitionMetricWindowModel.class))
                .isNotNull();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void start() {
        scheduler = new PartitionActionScheduler(TASK_ID, config, storeProvider, List.of(job));
        scheduler.start();
    }

    /**
     * Plays the role of the punctuator: keeps draining until the expected state is reached.
     */
    private void drainUntil(java.util.function.BooleanSupplier done) {
        await().untilAsserted(() -> {
            scheduler.drain(applier, Duration.ofMinutes(1), 1000);
            assertThat(done.getAsBoolean()).isTrue();
        });
    }

    private long minutesAgo(int minutes) {
        return LHUtil.getCurrentWindowDate().getTime() - (minutes * ONE_MINUTE);
    }

    private void seedHint(long timestampMillis) {
        coreStore.put(new MetricsHintModel(fromMillis(timestampMillis)));
    }

    private long hint() {
        MetricsHintModel stored = coreStore.get(MetricsHintModel.METRICS_HINT_KEY, MetricsHintModel.class);
        return stored == null
                ? Long.MIN_VALUE
                : stored.getLastProcessedTimestamp().getSeconds() * 1000;
    }

    private PartitionMetricWindowModel seedWindow(String wfSpecName, long windowStartMillis) {
        MetricWindowIdModel id = new MetricWindowIdModel(
                new TenantIdModel(TENANT_ID), new WfSpecIdModel(wfSpecName, 0, 0), new Date(windowStartMillis));
        PartitionMetricWindowModel window = new PartitionMetricWindowModel(id);
        window.incrementCount("started");
        coreStore.put(window);
        return window;
    }

    private List<MetricWindowIdModel> forwardedWindowIds() {
        List<MetricWindowIdModel> ids = new ArrayList<>();
        mockProcessorContext.forwarded().forEach(forward -> {
            Record<? extends String, ? extends CommandProcessorOutput> record = forward.record();
            LHTimer timer = (LHTimer) record.value().payload;
            try {
                Command command = Command.parseFrom(timer.getPayload());
                ids.add(io.littlehorse.common.LHSerializable.fromProto(
                        command.getAggregateWindowMetrics().getMetricWindow().getId(),
                        MetricWindowIdModel.class,
                        context));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return ids;
    }

    private static org.awaitility.core.ConditionFactory await() {
        return Awaitility.await().atMost(Duration.ofSeconds(5)).pollInterval(Duration.ofMillis(10));
    }

    static {
        Awaitility.setDefaultPollDelay(0, TimeUnit.MILLISECONDS);
    }

    /**
     * Guards against the headers being dropped when a record is built off-thread.
     */
    @Test
    void shouldStampTenantMetadataHeadersOnForwardedRecords() {
        when(config.getCoreCmdTopicName()).thenReturn(CORE_CMD_TOPIC);
        seedHint(minutesAgo(10));
        seedWindow("wf-a", minutesAgo(5));

        start();
        drainUntil(() -> mockProcessorContext.forwarded().size() == 2);

        Record<? extends String, ? extends CommandProcessorOutput> record =
                mockProcessorContext.forwarded().get(0).record();
        assertThat(HeadersUtil.tenantIdFromMetadata(record.headers()).getId()).isEqualTo(TENANT_ID);
        assertThat(record.value().topic).isEqualTo(CORE_CMD_TOPIC);
        LHTimer timer = (LHTimer) record.value().payload;
        assertThat(timer.isRepartition()).isTrue();
        assertThat(timer.getTenantId().getId()).isEqualTo(TENANT_ID);
        assertThat(LHConstants.PARTITION_METRICS_KEY).isNotBlank();
    }
}
