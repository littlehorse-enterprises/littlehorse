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
import io.littlehorse.server.streams.util.MetadataCache;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BulkJobPunctuatorTest {

    private static final String METADATA_CMD_TOPIC = "metadata-cmd-topic";
    private static final String WF_SPEC_NAME = "target-wf";
    private static final int NUMBER_OF_PARTITIONS = 12;

    /**
     * A command budget large enough that it never interferes with the time-budget focused tests:
     * they assert on wall-clock deadlines, not on the number of forwarded commands.
     */
    private static final long UNLIMITED_COMMAND_BUDGET = 1_000L;

    @Mock
    private LHServerConfig config;

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    private final MetadataCache metadataCache = new MetadataCache();
    private final BackgroundContext context = new BackgroundContext();

    private final TenantIdModel tenantId = new TenantIdModel(LHConstants.DEFAULT_TENANT);

    private final KeyValueStore<String, Bytes> globalStore = TestUtil.testStore(ServerTopology.GLOBAL_METADATA_STORE);
    private final KeyValueStore<String, Bytes> coreStore = TestUtil.testStore(ServerTopology.CORE_STORE);
    private ClusterScopedStore clusterStore;
    private TenantScopedStore tenantGlobalStore;
    private TenantScopedStore tenantCoreStore;
    private Duration punctuationBudget = Duration.ofSeconds(1);

    private BulkJobPunctuator punctuator;

    @BeforeEach
    void setup() {
        // Registering the native stores against the mock's StateStoreContext is what makes
        // ctx.getStateStore(name) resolve them inside the punctuator.
        globalStore.init(mockProcessorContext.getStateStoreContext(), globalStore);
        coreStore.init(mockProcessorContext.getStateStoreContext(), coreStore);
        punctuator = new BulkJobPunctuator(
                mockProcessorContext, config, metadataCache, punctuationBudget, UNLIMITED_COMMAND_BUDGET);
        when(config.getMetadataCmdTopicName()).thenReturn(METADATA_CMD_TOPIC);
        clusterStore = ClusterScopedStore.newInstance(globalStore, context);
        tenantGlobalStore = TenantScopedStore.newInstance(globalStore, tenantId, context);
        tenantCoreStore = TenantScopedStore.newInstance(coreStore, tenantId, context);
    }

    @Test
    void shouldForwardExactlyOneReportForRunnableJob() {
        final String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, emptyMatchDelete());

        punctuator.punctuate(System.currentTimeMillis());

        BulkJobShardReportModel report = getForwardedShardReport(0);
        assertThat(report.getBulkJobId().getId()).isEqualTo(jobId);
        assertThat(report.getPartition())
                .isEqualTo(mockProcessorContext.taskId().partition());
        assertThat(report.isCompleted()).isTrue();
    }

    @Test
    void shouldForwardDeleteCommandsForMatchingWfRuns() {
        final String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, deleteForWfSpec(WF_SPEC_NAME));

        String wfRunIdA = LHUtil.generateGuid();
        String wfRunIdB = LHUtil.generateGuid();
        Date now = new Date();
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdA, now);
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdB, now);

        punctuator.punctuate(System.currentTimeMillis());

        // One delete command per matching WfRun Tag, plus the shard report.
        assertThat(forwardedDeletedWfRunIds()).containsExactlyInAnyOrder(wfRunIdA, wfRunIdB);
        assertThat(onlyForwardedShardReport().isCompleted()).isTrue();
        assertThat(onlyForwardedShardReport().getLastSeenTimestamp()).isEqualTo(now);
    }

    @Test
    void shouldForwardOneReportPerActiveJob() {
        String jobIdA = LHUtil.generateGuid();
        String jobIdB = LHUtil.generateGuid();
        seedRunningJob(jobIdA, emptyMatchDelete());
        seedRunningJob(jobIdB, emptyMatchDelete());

        punctuator.punctuate(System.currentTimeMillis());

        // No Tags seeded, so each job contributes exactly one report and nothing else.
        List<BulkJobShardReportModel> reports = forwardedShardReports();
        assertThat(reports)
                .extracting(report -> report.getBulkJobId().getId())
                .containsExactlyInAnyOrder(jobIdA, jobIdB);
        assertThat(reports).allSatisfy(report -> {
            assertThat(report.isCompleted()).isTrue();
            assertThat(report.getPartition())
                    .isEqualTo(mockProcessorContext.taskId().partition());
        });
    }

    @Test
    void shouldDeferPendingWorkWhenPunctuationBudgetIsExhausted() {
        String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, emptyMatchDelete());
        String cursorKey = new BulkJobShardCursorModel(new BulkJobIdModel(jobId)).getStoreKey();

        // An already-elapsed budget makes the deadline expire before the first job is processed,
        // so the punctuator forwards nothing and persists no cursor: all work is deferred.
        BulkJobPunctuator exhaustedBudget = new BulkJobPunctuator(
                mockProcessorContext, config, metadataCache, Duration.ofSeconds(-1), UNLIMITED_COMMAND_BUDGET);
        exhaustedBudget.punctuate(System.currentTimeMillis());

        assertThat(mockProcessorContext.forwarded()).isEmpty();
        assertThat(tenantCoreStore.get(cursorKey, BulkJobShardCursorModel.class))
                .isNull();

        // A later punctuation with budget available resumes the still-pending job to completion.
        punctuator.punctuate(System.currentTimeMillis());

        BulkJobShardReportModel report = onlyForwardedShardReport();
        assertThat(report.getBulkJobId().getId()).isEqualTo(jobId);
        assertThat(report.isCompleted()).isTrue();
        assertThat(tenantCoreStore.get(cursorKey, BulkJobShardCursorModel.class).isScanCompleted())
                .isTrue();
    }

    @Test
    void shouldResumeFromTheSameKeyOnTheNextPunctuationWhenBudgetIsExhausted() {
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

        // First punctuation: the budget is exhausted as soon as two deletes have been forwarded,
        // so the scan yields mid-way and persists its position.
        Instant base = Instant.now();
        Supplier<Instant> budgetExhaustedAfterTwoDeletes =
                () -> mockProcessorContext.forwarded().size() >= 2 ? base.plus(Duration.ofHours(1)) : base;
        BulkJobPunctuator firstPunctuation = new BulkJobPunctuator(
                mockProcessorContext,
                config,
                metadataCache,
                Duration.ofMinutes(1),
                UNLIMITED_COMMAND_BUDGET,
                budgetExhaustedAfterTwoDeletes);
        firstPunctuation.punctuate(System.currentTimeMillis());

        // Exactly the first two WfRuns are deleted and the shard is reported as not yet complete.
        assertThat(forwardedDeletedWfRunIds()).containsExactly(wfRunIdA, wfRunIdB);
        assertThat(onlyForwardedShardReport().isCompleted()).isFalse();
        assertThat(onlyForwardedShardReport().getLastSeenTimestamp()).isEqualTo(wfRunCreatedAtB);

        mockProcessorContext.resetForwards();

        // Second punctuation resumes exactly where the first left off: only the remaining WfRun is
        // deleted (no re-processing of the first two), and the shard completes.
        punctuator.punctuate(System.currentTimeMillis());

        assertThat(forwardedDeletedWfRunIds()).containsExactly(wfRunIdC);
        assertThat(onlyForwardedShardReport().isCompleted()).isTrue();
        assertThat(onlyForwardedShardReport().getLastSeenTimestamp()).isEqualTo(wfRunCreatedAtC);
    }

    @Test
    void shouldDeferAllWorkWhenCommandBudgetIsExhaustedBeforeFirstJob() {
        String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, emptyMatchDelete());
        String cursorKey = new BulkJobShardCursorModel(new BulkJobIdModel(jobId)).getStoreKey();

        // A command budget of 1 is consumed by the very first (inter-job) outOfBudget check, so the
        // punctuator yields before touching any job: nothing is forwarded and no cursor is persisted.
        // The generous time budget proves the yield is caused by the command budget, not the clock.
        BulkJobPunctuator exhaustedCommandBudget =
                new BulkJobPunctuator(mockProcessorContext, config, metadataCache, Duration.ofMinutes(1), 0L);
        exhaustedCommandBudget.punctuate(System.currentTimeMillis());

        assertThat(mockProcessorContext.forwarded()).isEmpty();
        assertThat(tenantCoreStore.get(cursorKey, BulkJobShardCursorModel.class))
                .isNull();

        // A later punctuation with an ample command budget resumes the still-pending job to completion.
        punctuator.punctuate(System.currentTimeMillis());

        BulkJobShardReportModel report = onlyForwardedShardReport();
        assertThat(report.getBulkJobId().getId()).isEqualTo(jobId);
        assertThat(report.isCompleted()).isTrue();
        assertThat(tenantCoreStore.get(cursorKey, BulkJobShardCursorModel.class).isScanCompleted())
                .isTrue();
    }

    @Test
    void shouldStopForwardingDeletesWhenCommandBudgetIsExhausted() {
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

        // The shared budget is decremented once by the inter-job check and once per scanned Tag. With
        // a budget of 2 that leaves room for exactly two deletes (a, b) before the third scan iteration
        // exhausts it, so the scan yields mid-way and persists its position. A minutes-long time budget
        // guarantees the yield is driven by the command budget, not the clock.
        BulkJobPunctuator commandBudgetOfFour =
                new BulkJobPunctuator(mockProcessorContext, config, metadataCache, Duration.ofMinutes(1), 2L);
        commandBudgetOfFour.punctuate(System.currentTimeMillis());

        // Exactly the first two WfRuns are deleted and the shard is reported as not yet complete.
        assertThat(forwardedDeletedWfRunIds()).containsExactly(wfRunIdA, wfRunIdB);
        assertThat(onlyForwardedShardReport().isCompleted()).isFalse();
        assertThat(onlyForwardedShardReport().getLastSeenTimestamp()).isEqualTo(wfRunCreatedAtB);

        mockProcessorContext.resetForwards();

        // Second punctuation with an ample budget resumes exactly where the first left off: only the
        // remaining WfRun is deleted (no re-processing of the first two), and the shard completes.
        punctuator.punctuate(System.currentTimeMillis());

        assertThat(forwardedDeletedWfRunIds()).containsExactly(wfRunIdC);
        assertThat(onlyForwardedShardReport().isCompleted()).isTrue();
        assertThat(onlyForwardedShardReport().getLastSeenTimestamp()).isEqualTo(wfRunCreatedAtC);
    }

    @Test
    void shouldBackOffWithoutCompletingWhenBoundaryWfRunIsNotDeletedYet() {
        String jobId = LHUtil.generateGuid();
        seedRunningJob(jobId, deleteForWfSpec(WF_SPEC_NAME));

        // The boundary WfRun (A) is where a previous punctuation left off. Its delete command has
        // NOT been processed yet, so both its Tag and its WfRun getable still exist in the store. A
        // later WfRun (B) also matches and would be deleted if the scan proceeded past the boundary.
        String wfRunIdA = LHUtil.generateGuid();
        String wfRunIdB = LHUtil.generateGuid();
        long now = System.currentTimeMillis();
        Tag boundaryTag = seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdA, new Date(now - 2000));
        seedMatchingWfRunTag(WF_SPEC_NAME, wfRunIdB, new Date(now - 1000));
        // The boundary WfRun still exists: its delete has not been applied yet.
        tenantCoreStore.put(TestUtil.storedWfRun(wfRunIdA));

        // Resume cursor pointing at the boundary Tag, with the scan not yet complete.
        BulkJobShardCursorModel cursor = new BulkJobShardCursorModel(new BulkJobIdModel(jobId));
        cursor.setLastKey(boundaryTag.getStoreKey());
        cursor.setScanCompleted(false);
        tenantCoreStore.put(cursor);

        punctuator.punctuate(System.currentTimeMillis());

        // The punctuator backs off: it forwards no new deletes (not even for the still-pending
        // boundary run, nor for the later WfRun B) and reports the shard as NOT complete.
        assertThat(forwardedDeletedWfRunIds()).isEmpty();
        assertThat(onlyForwardedShardReport().isCompleted()).isFalse();

        // The persisted cursor is unchanged: still incomplete and still pointing at the boundary key,
        // so the next punctuation retries from exactly the same position once the delete lands.
        BulkJobShardCursorModel persisted = tenantCoreStore.get(cursor.getStoreKey(), BulkJobShardCursorModel.class);
        assertThat(persisted.isScanCompleted()).isFalse();
        assertThat(persisted.getLastKey()).isEqualTo(boundaryTag.getStoreKey());
    }

    private void seedRunningJob(String jobId, BulkDeleteWfRunModel deleteWfRun) {
        BulkJobIdModel bulkJobId = new BulkJobIdModel(jobId);
        // Cluster-scoped marker that the discovery range-scan iterates over.
        ActiveBulkJobModel active = new ActiveBulkJobModel(bulkJobId, tenantId);
        clusterStore.put(new StoredGetable<>(active));

        // Tenant-scoped BulkJob that the punctuator loads by id (must be RUNNING).
        BulkJobModel job = new BulkJobModel(bulkJobId, deleteWfRun, NUMBER_OF_PARTITIONS);
        tenantGlobalStore.put(new StoredGetable<>(job));
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
                .map(forward -> forward.record().value().getPayload())
                .filter(MetadataCommandModel.class::isInstance)
                .map(payload -> (BulkJobShardReportModel) ((MetadataCommandModel) payload).getSubCommand())
                .toList();
    }

    private BulkJobShardReportModel getForwardedShardReport(int forwardIndex) {
        List<MockProcessorContext.CapturedForward<? extends String, ? extends CommandProcessorOutput>> forwarded =
                mockProcessorContext.forwarded();
        // No WfRun Tags are seeded, so the scan finds nothing to delete: the only forward is the report.
        assertThat(forwarded).hasSize(1);

        CommandProcessorOutput output = forwarded.get(0).record().value();
        assertThat(output.getTopic()).isEqualTo(METADATA_CMD_TOPIC);
        assertThat(output.getPayload()).isInstanceOf(MetadataCommandModel.class);

        MetadataCommandModel command = (MetadataCommandModel) output.getPayload();
        assertThat(command.getSubCommand()).isInstanceOf(BulkJobShardReportModel.class);
        return (BulkJobShardReportModel) command.getSubCommand();
    }
}
