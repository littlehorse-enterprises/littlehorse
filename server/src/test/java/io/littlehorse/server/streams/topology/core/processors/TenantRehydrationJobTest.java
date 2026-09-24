package io.littlehorse.server.streams.topology.core.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.TestCoreProcessorContext;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.TaskQueueHintModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.background.PartitionActionScheduler;
import io.littlehorse.server.streams.topology.core.background.PartitionJobContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
 * Covers rebuilding the in-memory task queue now that it runs on the shared background worker
 * instead of blocking {@code CommandProcessor.init()}.
 *
 * <p>{@code run()} is driven synchronously on the test thread so every scenario is deterministic.
 * Unlike the other background jobs this one produces no {@code PartitionAction}s — the task queue is
 * in-memory and lock-guarded — so the assertions are on calls to {@link LHServer#onTaskScheduled}.
 */
@ExtendWith(MockitoExtension.class)
public class TenantRehydrationJobTest {

    private static final TaskId TASK_ID = new TaskId(0, 0);
    private static final Duration UNLIMITED_BUDGET = Duration.ofMinutes(1);
    private static final TenantIdModel DEFAULT_TENANT = new TenantIdModel(LHConstants.DEFAULT_TENANT);
    private static final TenantIdModel OTHER_TENANT = new TenantIdModel("my-tenant");

    @Mock
    private LHServerConfig config;

    @Mock
    private CoreStoreProvider storeProvider;

    private final LHServer server = mock(LHServer.class);
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();
    private final BackgroundContext context = new BackgroundContext();

    private KeyValueStore<String, Bytes> globalStore;
    private KeyValueStore<String, Bytes> coreStore;
    private ClusterScopedStore clusterStore;
    private PartitionActionScheduler<CommandProcessorOutput> scheduler;

    @BeforeEach
    void setup() {
        globalStore = TestUtil.testStore(ServerTopology.GLOBAL_METADATA_STORE);
        coreStore = TestUtil.testStore(ServerTopology.CORE_STORE);
        globalStore.init(mockProcessorContext.getStateStoreContext(), globalStore);
        coreStore.init(mockProcessorContext.getStateStoreContext(), coreStore);

        when(storeProvider.getNativeGlobalStore()).thenReturn(globalStore);
        when(storeProvider.nativeCoreStore(TASK_ID.partition())).thenReturn(coreStore);

        clusterStore = ClusterScopedStore.newInstance(globalStore, context);
    }

    @Test
    void shouldReofferScheduledTasksForEveryRegisteredTenant() throws Exception {
        registerTenant(DEFAULT_TENANT);
        registerTenant(OTHER_TENANT);
        ScheduledTaskModel inDefault = seedScheduledTask(DEFAULT_TENANT);
        ScheduledTaskModel inOther = seedScheduledTask(OTHER_TENANT);

        TenantRehydrationJob job = newJob(UNLIMITED_BUDGET);
        job.run(jobContext());

        verify(server).onTaskScheduled(eq(TASK_ID), any(), eq(inDefault.getTaskRunId()), eq(DEFAULT_TENANT));
        verify(server).onTaskScheduled(eq(TASK_ID), any(), eq(inOther.getTaskRunId()), eq(OTHER_TENANT));
        assertThat(job.isComplete()).isTrue();
    }

    @Test
    void shouldIgnoreTenantsThatHaveNoScheduledTasks() throws Exception {
        registerTenant(OTHER_TENANT);

        TenantRehydrationJob job = newJob(UNLIMITED_BUDGET);
        job.run(jobContext());

        verify(server, never()).onTaskScheduled(any(), any(), any(), any());
        assertThat(job.isComplete()).isTrue();
    }

    /**
     * Rehydration is a one-shot. If it kept re-running it would re-offer the entire backlog every
     * couple of hundred milliseconds for the life of the partition.
     */
    @Test
    void shouldNotRescanOnceComplete() throws Exception {
        registerTenant(DEFAULT_TENANT);
        ScheduledTaskModel task = seedScheduledTask(DEFAULT_TENANT);

        TenantRehydrationJob job = newJob(UNLIMITED_BUDGET);
        job.run(jobContext());
        job.run(jobContext());
        job.run(jobContext());

        verify(server, times(1)).onTaskScheduled(any(), any(), eq(task.getTaskRunId()), any());
    }

    /**
     * The worker is shared with the timer scan, so a large backlog must not monopolise it. A pass
     * that runs out of budget has to stop without losing its place.
     */
    @Test
    void shouldYieldOnBudgetAndResumeOnTheNextPass() throws Exception {
        registerTenant(DEFAULT_TENANT);
        for (int i = 0; i < 5; i++) {
            seedScheduledTask(DEFAULT_TENANT);
        }

        // A zero budget expires after the first task, so each pass makes exactly one task of
        // progress. This is the case that catches a resume point which fails to advance.
        TenantRehydrationJob job = newJob(Duration.ZERO);
        job.run(jobContext());
        assertThat(job.isComplete()).isFalse();

        for (int i = 0; i < 10 && !job.isComplete(); i++) {
            job.run(jobContext());
        }

        assertThat(job.isComplete()).isTrue();
        verify(server, times(5)).onTaskScheduled(any(), any(), any(), eq(DEFAULT_TENANT));
    }

    /**
     * The hint is what keeps rehydration from walking every tombstone left by completed tasks, so
     * the scan must actually start from it.
     */
    @Test
    void shouldResumeFromThePersistedTaskQueueHint() throws Exception {
        registerTenant(DEFAULT_TENANT);
        ScheduledTaskModel old = seedScheduledTaskAt(DEFAULT_TENANT, new Date(1_000L));
        ScheduledTaskModel recent = seedScheduledTaskAt(DEFAULT_TENANT, new Date(9_000L));
        tenantStore(DEFAULT_TENANT).put(new TaskQueueHintModel(new Date(5_000L)));

        newJob(UNLIMITED_BUDGET).run(jobContext());

        verify(server, never()).onTaskScheduled(any(), any(), eq(old.getTaskRunId()), any());
        verify(server).onTaskScheduled(any(), any(), eq(recent.getTaskRunId()), any());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private TenantRehydrationJob newJob(Duration budget) {
        return new TenantRehydrationJob(TASK_ID, server, budget);
    }

    private PartitionJobContext<CommandProcessorOutput> jobContext() {
        if (scheduler == null) {
            scheduler = new PartitionActionScheduler<>(TASK_ID, config, storeProvider, List.of());
        }
        return new PartitionJobContext<>(TASK_ID.partition(), config, storeProvider, scheduler);
    }

    private void registerTenant(TenantIdModel tenantId) {
        clusterStore.put(new StoredGetable<>(new TenantModel(tenantId.getId())));
    }

    private TenantScopedStore tenantStore(TenantIdModel tenantId) {
        return TenantScopedStore.newInstance(coreStore, tenantId, context);
    }

    private ScheduledTaskModel seedScheduledTask(TenantIdModel tenantId) {
        return seedScheduledTaskAt(tenantId, new Date());
    }

    private ScheduledTaskModel seedScheduledTaskAt(TenantIdModel tenantId, Date createdAt) {
        TestCoreProcessorContext processorContext = TestCoreProcessorContext.create(
                io.littlehorse.common.proto.Command.newBuilder()
                        .setRunWf(io.littlehorse.sdk.common.proto.RunWfRequest.newBuilder()
                                .setWfSpecName("name")
                                .build())
                        .build(),
                HeadersUtil.metadataHeadersFor(tenantId.getId(), "anonymous"),
                mockProcessorContext);
        NodeRunModel nodeRun = TestUtil.nodeRun();
        UserTaskRunModel userTaskRun = TestUtil.userTaskRun(UUID.randomUUID().toString(), nodeRun, processorContext);
        ScheduledTaskModel scheduledTask = new ScheduledTaskModel(
                TestUtil.taskDef("my-task").getObjectId(), List.of(), userTaskRun, processorContext);
        scheduledTask.setCreatedAt(createdAt);
        // Give each task a distinct TaskRunId so the queue's de-duplication does not hide anything.
        scheduledTask.setTaskRunId(new TaskRunIdModel(
                userTaskRun.getId().getWfRunId(), UUID.randomUUID().toString()));
        tenantStore(tenantId).put(scheduledTask);
        return scheduledTask;
    }
}
