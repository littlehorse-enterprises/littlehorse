package io.littlehorse.common.model.corecommand.subcommand.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.global.bulkjob.BulkDeleteWfRunModel;
import io.littlehorse.common.model.getable.global.bulkjob.BulkJobModel;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.BulkDeleteWfRun;
import io.littlehorse.sdk.common.proto.BulkJobStatus;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.monitoring.metrics.CommandProcessorMetrics;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BulkJobShardReportModelTest {

    @Mock
    private LHServerConfig config;

    @Mock
    private LHServer server;

    private final CommandProcessorMetrics metrics = mock();
    private final AsyncWaiters asyncWaiters = new AsyncWaiters();
    private final MetadataCache metadataCache = new MetadataCache();

    private final ExecutionContext executionContext = Mockito.mock(Answers.RETURNS_DEEP_STUBS);

    // The MetadataProcessorContext reads from ServerTopology.METADATA_STORE.
    private final KeyValueStore<String, Bytes> nativeMetadataStore = TestUtil.testStore(ServerTopology.METADATA_STORE);

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    private final TenantIdModel tenantId = new TenantIdModel(LHConstants.DEFAULT_TENANT);

    private final TenantScopedStore defaultStore =
            TenantScopedStore.newInstance(nativeMetadataStore, tenantId, executionContext);

    private MetadataProcessor metadataProcessor;

    @BeforeEach
    void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache, asyncWaiters, metrics);
        // init() bootstraps the default Tenant + anonymous Principal, which the output-topic
        // callback in MetadataManager.put() relies on.
        metadataProcessor.init(mockProcessorContext);

        defaultStore.enableCache(metadataCache);
    }

    @Test
    void shouldMarkSingleShardComplete() {
        String jobId = "test";
        seedBulkJob(jobId, 3);
        sendReport(jobId, 0, true);

        BulkJobModel updated = storedBulkJob(jobId);

        assertThat(updated.getSubprocesses())
                .filteredOn(sp -> sp.getId() == 0)
                .allMatch(sp -> sp.getStatus() == BulkJobStatus.BULK_JOB_COMPLETED);
        // Other shards remain running.
        assertThat(updated.getSubprocesses())
                .filteredOn(sp -> sp.getId() != 0)
                .allMatch(sp -> sp.getStatus() == BulkJobStatus.BULK_JOB_RUNNING);
        assertThat(updated.getStatus()).isEqualTo(BulkJobStatus.BULK_JOB_RUNNING);
    }

    @Test
    void shouldNotLoseUpdatesAcrossMultipleReports() {
        String jobId = "test";
        seedBulkJob(jobId, 100);
        for (int i = 0; i < 100; i++) {
            sendReport(jobId, i, true);
        }

        BulkJobModel updated = storedBulkJob(jobId);
        assertThat(updated.getSubprocesses()).allMatch(sp -> sp.getStatus() == BulkJobStatus.BULK_JOB_COMPLETED);
        assertThat(updated.getStatus()).isEqualTo(BulkJobStatus.BULK_JOB_COMPLETED);
    }

    @Test
    void shouldMergeIntoExistingPartialProgress() {
        String jobId = "test";
        seedBulkJob(jobId, 3);
        // First two shards complete across two commands.
        sendReport(jobId, 0, true);
        sendReport(jobId, 1, true);

        BulkJobModel afterTwo = storedBulkJob(jobId);
        assertThat(afterTwo.getStatus()).isEqualTo(BulkJobStatus.BULK_JOB_RUNNING);
        assertThat(afterTwo.getSubprocesses())
                .filteredOn(sp -> sp.getId() == 2)
                .allMatch(sp -> sp.getStatus() == BulkJobStatus.BULK_JOB_RUNNING);

        // The final shard completes and flips the whole job to COMPLETED, without wiping the
        // progress recorded by the earlier reports.
        sendReport(jobId, 2, true);

        BulkJobModel afterThree = storedBulkJob(jobId);
        assertThat(afterThree.getSubprocesses()).allMatch(sp -> sp.getStatus() == BulkJobStatus.BULK_JOB_COMPLETED);
        assertThat(afterThree.getStatus()).isEqualTo(BulkJobStatus.BULK_JOB_COMPLETED);
    }

    /**
     * Regression test for the lost-update bug fixed by {@code BulkJobShardReportModel#process}
     * calling {@code metadataManager().disableCache()}.
     *
     * <p>The shared {@link MetadataCache} is populated asynchronously by the
     * {@code MetadataGlobalStoreProcessor} as it replays the metadata changelog. That replay can
     * land <em>after</em> a shard report has already committed a newer value, overwriting the cache
     * with an older snapshot. Because {@code getMetadataObject} uses {@code computeIfAbsent}, the
     * next read-modify-write starts from that stale snapshot and silently drops previously-recorded
     * shard progress.
     *
     * <p>This test reproduces that race deterministically by poisoning the cache with a pre-report
     * snapshot in between commands. With the cache disabled (the fix) each command reads the fresh
     * native-store state and no update is lost; without it, shard 0's completion is wiped out.
     */
    @Test
    void shouldNotLoseUpdatesWhenStaleSnapshotPoisonsCache() {
        String jobId = "test";
        seedBulkJob(jobId, 3);

        // Snapshot the pre-report state (all shards RUNNING), independent of the shared cache.
        StoredGetable<?, BulkJobModel> staleSnapshot = uncachedSnapshot(jobId);

        // Shard 0 completes and is persisted to the native store.
        sendReport(jobId, 0, true);

        // Simulate the MetadataGlobalStoreProcessor asynchronously replaying the *pre-report*
        // changelog record, overwriting the cache with a snapshot in which shard 0 is still RUNNING.
        poisonCache(jobId, staleSnapshot);

        // Remaining shards complete. Without disabling the cache, shard 1's read-modify-write starts
        // from the stale snapshot and drops shard 0's completion.
        sendReport(jobId, 1, true);
        sendReport(jobId, 2, true);

        BulkJobModel updated = storedBulkJob(jobId);
        assertThat(updated.getSubprocesses()).allMatch(sp -> sp.getStatus() == BulkJobStatus.BULK_JOB_COMPLETED);
        assertThat(updated.getStatus()).isEqualTo(BulkJobStatus.BULK_JOB_COMPLETED);
    }

    // --- Helpers ---

    private void seedBulkJob(String jobId, int totalSubprocesses) {
        BulkJobModel job = new BulkJobModel(new BulkJobIdModel(jobId), realBulkDelete(), totalSubprocesses);
        defaultStore.put(new StoredGetable<>(job));
    }

    private void sendReport(String jobId, int partition, boolean completed) {
        BulkJobShardReportModel report = new BulkJobShardReportModel(
                new BulkJobIdModel(jobId), partition, completed, "last-seen-key", new Date());
        MetadataCommandModel command = new MetadataCommandModel(report);
        Headers metadata = HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL);
        metadataProcessor.process(new Record<>(jobId, command.toProto().build(), 0L, metadata));
    }

    private BulkJobModel storedBulkJob(String jobId) {
        // Clear the cache so we assert against the committed native-store state rather than a
        // possibly stale cache entry.
        metadataCache.clear();
        StoredGetable<?, BulkJobModel> stored =
                defaultStore.get(new BulkJobIdModel(jobId).getStoreableKey(), StoredGetable.class);
        assertThat(stored).isNotNull();
        return stored.getStoredObject();
    }

    /**
     * Reads the current BulkJob straight from the native store, bypassing the shared cache, so the
     * returned snapshot is an independent deep copy that later mutations won't affect.
     */
    private StoredGetable<?, BulkJobModel> uncachedSnapshot(String jobId) {
        TenantScopedStore uncached = TenantScopedStore.newInstance(nativeMetadataStore, tenantId, executionContext);
        return uncached.get(new BulkJobIdModel(jobId).getStoreableKey(), StoredGetable.class);
    }

    /**
     * Injects a stale StoredGetable into the shared cache under the exact key the tenant-scoped store
     * uses, mimicking the async cache write performed by MetadataGlobalStoreProcessor.
     */
    private void poisonCache(String jobId, StoredGetable<?, BulkJobModel> stale) {
        String storeKey = new BulkJobIdModel(jobId).getStoreableKey();
        String cacheKey = tenantId + "/" + Storeable.getFullStoreKey(StoreableType.STORED_GETABLE, storeKey);
        metadataCache.update(cacheKey, stale);
    }

    private BulkDeleteWfRunModel realBulkDelete() {
        // A real (non-mock) BulkDeleteWfRunModel is required because seeding the BulkJob goes
        // through toProto() serialization, which dereferences these fields.
        BulkDeleteWfRun proto = BulkDeleteWfRun.newBuilder()
                .setWfSpecName("target-wf")
                .setEarliestStart(LHUtil.fromDate(new Date(System.currentTimeMillis() - 3_600_000)))
                .setLatestStart(LHUtil.fromDate(new Date(System.currentTimeMillis() + 60_000)))
                .build();
        return LHSerializable.fromProto(proto, BulkDeleteWfRunModel.class, executionContext);
    }
}
