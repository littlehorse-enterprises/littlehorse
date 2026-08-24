package io.littlehorse.server.streams.topology.core.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.subcommand.job.BulkJobShardCursorModel;
import io.littlehorse.common.model.corecommand.subcommand.job.BulkJobShardReportModel;
import io.littlehorse.common.model.getable.global.bulkjob.ActiveBulkJobModel;
import io.littlehorse.common.model.getable.global.bulkjob.BulkDeleteWfRunModel;
import io.littlehorse.common.model.getable.global.bulkjob.BulkJobModel;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.BulkDeleteWfRun;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.background.PartitionActionApplier;
import io.littlehorse.server.streams.topology.core.background.PartitionActionScheduler;
import io.littlehorse.server.streams.topology.core.background.PartitionJobContext;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.TaskId;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Port of the old {@code BulkJobPunctuatorTest} onto the background-job mechanism.
 *
 * <p>The job's {@code run()} is driven synchronously on the test thread, which keeps every scenario
 * deterministic, and {@code scheduler.drain()} then plays the role of the punctuator. That split is
 * also what these tests are really about: the job only ever <i>schedules</i> effects, and nothing is
 * visible in the store or downstream until the Streams thread applies them.
 */
@ExtendWith(MockitoExtension.class)
public class BulkJobScanJobTest {

    private static final String METADATA_CMD_TOPIC = "metadata-cmd-topic";
    private static final String WF_SPEC_NAME = "target-wf";
    private static final int NUMBER_OF_PARTITIONS = 12;
    private static final TaskId TASK_ID = new TaskId(0, 0);

    /**
     * A command budget large enough that it never interferes with the time-budget focused tests:
     * they assert on wall-clock deadlines, not on the number of forwarded commands.
     */
    private static final long UNLIMITED_COMMAND_BUDGET = 1_000L;

    private static final Duration UNLIMITED_TIME_BUDGET = Duration.ofMinutes(1);

    @Mock
    private LHServerConfig config;

    @Mock
    private CoreStoreProvider storeProvider;

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    private final BackgroundContext context = new BackgroundContext();
    private final TenantIdModel tenantId = new TenantIdModel(LHConstants.DEFAULT_TENANT);

    private final KeyValueStore<String, Bytes> globalStore = TestUtil.testStore(ServerTopology.GLOBAL_METADATA_STORE);
    private final KeyValueStore<String, Bytes> coreStore = TestUtil.testStore(ServerTopology.CORE_STORE);

    private ClusterScopedStore clusterStore;
    private TenantScopedStore tenantGlobalStore;
    private TenantScopedStore tenantCoreStore;

    private PartitionActionApplier applier;
    private PartitionActionScheduler scheduler;
    private BulkJobScanJob job;

    @BeforeEach
    void setup() {
        globalStore.init(mockProcessorContext.getStateStoreContext(), globalStore);
        coreStore.init(mockProcessorContext.getStateStoreContext(), coreStore);

        when(config.getMetadataCmdTopicName()).thenReturn(METADATA_CMD_TOPIC);
        when(storeProvider.getNativeGlobalStore()).thenReturn(globalStore);
        when(storeProvider.nativeCoreStore(TASK_ID.partition())).thenReturn(coreStore);

        clusterStore = ClusterScopedStore.newInstance(globalStore, context);
        tenantGlobalStore = TenantScopedStore.newInstance(globalStore, tenantId, context);
        tenantCoreStore = TenantScopedStore.newInstance(coreStore, tenantId, context);

        applier = new PartitionActionApplier(mockProcessorContext, coreStore);
        job = newJob(UNLIMITED_TIME_BUDGET, UNLIMITED_COMMAND_BUDGET, Instant::now);
    }

    @Test
    void shouldForwardExactlyOneReportForRunnableJob() throws Exception {
        final String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, emptyMatchDelete());

        runAndApply(job);

        BulkJobShardReportModel report = onlyForwardedShardReport();
        assertThat(report.getBulkJobId().getId()).isEqualTo(jobId);
        assertThat(report.getPartition()).isEqualTo(TASK_ID.partition());
        assertThat(report.isCompleted()).isTrue();
    }

    @Test
    void shouldForwardDeleteCommandsForMatchingWfRuns() throws Exception {
        final String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, deleteForWfSpec(WF_SPEC_NAME));

        String wfRunIdA = LHUtil.generateGuid();
        String wfRunIdB = LHUtil.generateGuid();
        Date now = new Date();
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdA, now);
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdB, now);

        runAndApply(job);

        // One delete command per matching WfRun Tag, plus the shard report.
        assertThat(forwardedDeletedWfRunIds()).containsExactlyInAnyOrder(wfRunIdA, wfRunIdB);
        assertThat(onlyForwardedShardReport().isCompleted()).isTrue();
        assertThat(onlyForwardedShardReport().getLastSeenTimestamp()).isEqualTo(now);
    }

    @Test
    void shouldForwardOneReportPerActiveJob() throws Exception {
        String jobIdA = LHUtil.generateGuid();
        String jobIdB = LHUtil.generateGuid();
        seedRunningJob(jobIdA, emptyMatchDelete());
        seedRunningJob(jobIdB, emptyMatchDelete());

        runAndApply(job);

        // No Tags seeded, so each job contributes exactly one report and nothing else.
        List<BulkJobShardReportModel> reports = forwardedShardReports();
        assertThat(reports)
                .extracting(report -> report.getBulkJobId().getId())
                .containsExactlyInAnyOrder(jobIdA, jobIdB);
        assertThat(reports).allSatisfy(report -> {
            assertThat(report.isCompleted()).isTrue();
            assertThat(report.getPartition()).isEqualTo(TASK_ID.partition());
        });
    }

    @Test
    void shouldScheduleNothingUntilThePunctuatorDrains() throws Exception {
        String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, emptyMatchDelete());
        String cursorKey = new BulkJobShardCursorModel(new BulkJobIdModel(jobId)).getStoreKey();

        job.run(jobContext());

        // The job has produced its effects, but the Streams thread has not applied them yet.
        assertThat(scheduler.pendingCount()).isEqualTo(2);
        assertThat(mockProcessorContext.forwarded()).isEmpty();
        assertThat(tenantCoreStore.get(cursorKey, BulkJobShardCursorModel.class))
                .isNull();

        drain();

        assertThat(onlyForwardedShardReport().getBulkJobId().getId()).isEqualTo(jobId);
        assertThat(tenantCoreStore.get(cursorKey, BulkJobShardCursorModel.class).isScanCompleted())
                .isTrue();
    }

    @Test
    void shouldDeferPendingWorkWhenTimeBudgetIsExhausted() throws Exception {
        String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, emptyMatchDelete());
        String cursorKey = new BulkJobShardCursorModel(new BulkJobIdModel(jobId)).getStoreKey();

        // An already-elapsed budget makes the deadline expire before the first job is processed, so
        // the job schedules nothing: all work is deferred.
        runAndApply(newJob(Duration.ofSeconds(-1), UNLIMITED_COMMAND_BUDGET, Instant::now));

        assertThat(mockProcessorContext.forwarded()).isEmpty();
        assertThat(tenantCoreStore.get(cursorKey, BulkJobShardCursorModel.class))
                .isNull();

        // A later pass with budget available resumes the still-pending job to completion.
        runAndApply(job);

        BulkJobShardReportModel report = onlyForwardedShardReport();
        assertThat(report.getBulkJobId().getId()).isEqualTo(jobId);
        assertThat(report.isCompleted()).isTrue();
        assertThat(tenantCoreStore.get(cursorKey, BulkJobShardCursorModel.class).isScanCompleted())
                .isTrue();
    }

    @Test
    void shouldResumeFromTheSameKeyOnTheNextPassWhenTimeBudgetIsExhausted() throws Exception {
        String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, deleteForWfSpec(WF_SPEC_NAME));

        // Distinct, increasing createdAt timestamps make the Tag scan order deterministic: a -> b -> c.
        String wfRunIdA = LHUtil.generateGuid();
        String wfRunIdB = LHUtil.generateGuid();
        String wfRunIdC = LHUtil.generateGuid();
        long now = System.currentTimeMillis();
        Date wfRunCreatedAtA = new Date(now - 3000);
        Date wfRunCreatedAtB = new Date(now - 2000);
        Date wfRunCreatedAtC = new Date(now - 1000);
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdA, wfRunCreatedAtA);
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdB, wfRunCreatedAtB);
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdC, wfRunCreatedAtC);

        // First pass: the budget is exhausted as soon as two deletes have been queued, so the scan
        // yields mid-way and persists its position. Note this observes the ACTION QUEUE rather than
        // the forwards, because nothing reaches the ProcessorContext until the drain.
        Instant base = Instant.now();
        Supplier<Instant> budgetExhaustedAfterTwoDeletes =
                () -> scheduler.pendingCount() >= 2 ? base.plus(Duration.ofHours(1)) : base;
        runAndApply(newJob(Duration.ofMinutes(1), UNLIMITED_COMMAND_BUDGET, budgetExhaustedAfterTwoDeletes));

        // Exactly the first two WfRuns are deleted and the shard is reported as not yet complete.
        assertThat(forwardedDeletedWfRunIds()).containsExactly(wfRunIdA, wfRunIdB);
        assertThat(onlyForwardedShardReport().isCompleted()).isFalse();
        assertThat(onlyForwardedShardReport().getLastSeenTimestamp()).isEqualTo(wfRunCreatedAtB);

        mockProcessorContext.resetForwards();

        // Second pass resumes exactly where the first left off: only the remaining WfRun is deleted
        // (no re-processing of the first two), and the shard completes.
        runAndApply(job);

        assertThat(forwardedDeletedWfRunIds()).containsExactly(wfRunIdC);
        assertThat(onlyForwardedShardReport().isCompleted()).isTrue();
        assertThat(onlyForwardedShardReport().getLastSeenTimestamp()).isEqualTo(wfRunCreatedAtC);
    }

    @Test
    void shouldDeferAllWorkWhenCommandBudgetIsExhaustedBeforeFirstJob() throws Exception {
        String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, emptyMatchDelete());
        String cursorKey = new BulkJobShardCursorModel(new BulkJobIdModel(jobId)).getStoreKey();

        // A command budget of 0 is already spent at the very first (inter-job) outOfBudget check, so
        // the job yields before touching anything. The generous time budget proves the yield is
        // caused by the command budget, not the clock.
        runAndApply(newJob(Duration.ofMinutes(1), 0L, Instant::now));

        assertThat(mockProcessorContext.forwarded()).isEmpty();
        assertThat(tenantCoreStore.get(cursorKey, BulkJobShardCursorModel.class))
                .isNull();

        // A later pass with an ample command budget resumes the still-pending job to completion.
        runAndApply(job);

        BulkJobShardReportModel report = onlyForwardedShardReport();
        assertThat(report.getBulkJobId().getId()).isEqualTo(jobId);
        assertThat(report.isCompleted()).isTrue();
        assertThat(tenantCoreStore.get(cursorKey, BulkJobShardCursorModel.class).isScanCompleted())
                .isTrue();
    }

    @Test
    void shouldStopForwardingDeletesWhenCommandBudgetIsExhausted() throws Exception {
        String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, deleteForWfSpec(WF_SPEC_NAME));

        // Distinct, increasing createdAt timestamps make the Tag scan order deterministic: a -> b -> c.
        String wfRunIdA = LHUtil.generateGuid();
        String wfRunIdB = LHUtil.generateGuid();
        String wfRunIdC = LHUtil.generateGuid();
        long now = System.currentTimeMillis();
        Date wfRunCreatedAtA = new Date(now - 3000);
        Date wfRunCreatedAtB = new Date(now - 2000);
        Date wfRunCreatedAtC = new Date(now - 1000);
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdA, wfRunCreatedAtA);
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdB, wfRunCreatedAtB);
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdC, wfRunCreatedAtC);

        // The shared budget is decremented once per emitted delete. With a budget of 2 that leaves
        // room for exactly two deletes (a, b) before the third scan iteration exhausts it, so the
        // scan yields mid-way and persists its position. A minutes-long time budget guarantees the
        // yield is driven by the command budget, not the clock.
        runAndApply(newJob(Duration.ofMinutes(1), 2L, Instant::now));

        assertThat(forwardedDeletedWfRunIds()).containsExactly(wfRunIdA, wfRunIdB);
        assertThat(onlyForwardedShardReport().isCompleted()).isFalse();
        assertThat(onlyForwardedShardReport().getLastSeenTimestamp()).isEqualTo(wfRunCreatedAtB);

        mockProcessorContext.resetForwards();

        // Second pass with an ample budget resumes exactly where the first left off.
        runAndApply(job);

        assertThat(forwardedDeletedWfRunIds()).containsExactly(wfRunIdC);
        assertThat(onlyForwardedShardReport().isCompleted()).isTrue();
        assertThat(onlyForwardedShardReport().getLastSeenTimestamp()).isEqualTo(wfRunCreatedAtC);
    }

    @Test
    void shouldBackOffWithoutCompletingWhenBoundaryWfRunIsNotDeletedYet() throws Exception {
        String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, deleteForWfSpec(WF_SPEC_NAME));

        // The boundary WfRun (A) is where a previous pass left off. Its delete command has NOT been
        // processed yet, so both its Tag and its WfRun getable still exist in the store. A later
        // WfRun (B) also matches and would be deleted if the scan proceeded past the boundary.
        //
        // This guard matters MORE now than it did inline: reading through IQv1 widens the window
        // between emitting a delete and observing that it has been applied.
        String wfRunIdA = LHUtil.generateGuid();
        String wfRunIdB = LHUtil.generateGuid();
        long now = System.currentTimeMillis();
        Tag boundaryTag = seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdA, new Date(now - 2000));
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdB, new Date(now - 1000));
        tenantCoreStore.put(TestUtil.storedWfRun(wfRunIdA));

        // Resume cursor pointing at the boundary Tag, with the scan not yet complete.
        BulkJobShardCursorModel cursor = new BulkJobShardCursorModel(new BulkJobIdModel(jobId));
        cursor.setLastKey(boundaryTag.getStoreKey());
        cursor.setScanCompleted(false);
        tenantCoreStore.put(cursor);

        runAndApply(job);

        // The job backs off: it emits no new deletes (not even for the still-pending boundary run,
        // nor for the later WfRun B) and reports the shard as NOT complete.
        assertThat(forwardedDeletedWfRunIds()).isEmpty();
        assertThat(onlyForwardedShardReport().isCompleted()).isFalse();

        // The persisted cursor is unchanged: still incomplete and still pointing at the boundary key,
        // so the next pass retries from exactly the same position once the delete lands.
        BulkJobShardCursorModel persisted = tenantCoreStore.get(cursor.getStoreKey(), BulkJobShardCursorModel.class);
        assertThat(persisted.isScanCompleted()).isFalse();
        assertThat(persisted.getLastKey()).isEqualTo(boundaryTag.getStoreKey());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private BulkJobScanJob newJob(Duration scanBudget, long maxCommands, Supplier<Instant> clock) {
        return new BulkJobScanJob(config, scanBudget, maxCommands, clock);
    }

    /**
     * Runs one pass of the job on the test thread, then plays the punctuator and applies everything
     * it queued.
     */
    private void runAndApply(BulkJobScanJob toRun) throws InterruptedException {
        toRun.run(jobContext());
        drain();
    }

    private void drain() {
        scheduler.drain(applier, UNLIMITED_TIME_BUDGET, Integer.MAX_VALUE);
    }

    /**
     * Lazily built so that {@link #scheduler} is shared by every call within a test, which is what
     * lets the budget clock observe the action queue.
     */
    private PartitionJobContext jobContext() {
        if (scheduler == null) {
            scheduler = new PartitionActionScheduler(TASK_ID, config, storeProvider, List.of());
        }
        return new PartitionJobContext(TASK_ID.partition(), config, storeProvider, scheduler);
    }

    private void seedRunningJob(String jobId, BulkDeleteWfRunModel deleteWfRun) {
        BulkJobIdModel bulkJobId = new BulkJobIdModel(jobId);
        // Cluster-scoped marker that the discovery range-scan iterates over.
        ActiveBulkJobModel active = new ActiveBulkJobModel(bulkJobId, tenantId);
        clusterStore.put(new StoredGetable<>(active));

        // Tenant-scoped BulkJob that the job loads by id (must be RUNNING).
        BulkJobModel bulkJob = new BulkJobModel(bulkJobId, deleteWfRun, NUMBER_OF_PARTITIONS);
        tenantGlobalStore.put(new StoredGetable<>(bulkJob));
    }

    private BulkDeleteWfRunModel emptyMatchDelete() {
        return deleteForWfSpec("no-such-wf-spec");
    }

    private BulkDeleteWfRunModel deleteForWfSpec(String wfSpecName) {
        BulkDeleteWfRun proto = BulkDeleteWfRun.newBuilder()
                .setWfSpecName(wfSpecName)
                // A real past instant: LHUtil.fromProtoTs treats epoch(0) as "now", which would
                // otherwise place the window start at "now" and exclude back-dated WfRun Tags.
                .setEarliestStart(LHUtil.fromDate(new Date(System.currentTimeMillis() - 3_600_000)))
                // Slightly in the future so Tags created "now" fall strictly inside the scan window.
                .setLatestStart(LHUtil.fromDate(new Date(System.currentTimeMillis() + 60_000)))
                .build();
        return LHSerializable.fromProto(proto, BulkDeleteWfRunModel.class, context);
    }

    private Tag seedMatchingWfRunTag(String wfSpecName, String wfRunId, Date createdAt) {
        WfRunIdModel id = new WfRunIdModel(wfRunId);
        Tag tag = new Tag(
                TagStorageType.LOCAL, GetableClassEnum.WF_RUN, List.of(new Attribute("wfSpecName", wfSpecName)));
        tag.setCreatedAt(createdAt);
        tag.setDescribedObjectId(id.toString());
        // Tags are co-partitioned with the WfRun in the tenant-scoped CORE store.
        tenantCoreStore.put(tag);
        return tag;
    }

    private List<String> forwardedDeletedWfRunIds() {
        return mockProcessorContext.forwarded().stream()
                .map(forward -> forward.record().value().getPayload())
                .filter(LHTimer.class::isInstance)
                .map(payload -> {
                    try {
                        Command command = Command.parseFrom(((LHTimer) payload).getPayload());
                        return command.getDeleteWfRun().getId().getId();
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    private BulkJobShardReportModel onlyForwardedShardReport() {
        List<BulkJobShardReportModel> reports = forwardedShardReports();
        assertThat(reports).hasSize(1);
        return reports.get(0);
    }

    private List<BulkJobShardReportModel> forwardedShardReports() {
        return mockProcessorContext.forwarded().stream()
                .map(forward -> forward.record().value())
                .filter(output -> METADATA_CMD_TOPIC.equals(output.getTopic()))
                .map(output -> (BulkJobShardReportModel) ((MetadataCommandModel) output.getPayload()).getSubCommand())
                .toList();
    }
}
