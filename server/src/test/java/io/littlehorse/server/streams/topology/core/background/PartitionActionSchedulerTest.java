package io.littlehorse.server.streams.topology.core.background;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.protobuf.Message;
import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.PartitionCountedTagModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.PartitionCountedTag;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
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
 * Unit tests for the per-partition background worker.
 *
 * <p>These tests exercise the two halves of the threading contract independently: the worker thread
 * staging and submitting actions, and the (simulated) Kafka Streams thread draining them. Every
 * assertion that depends on the worker uses Awaitility rather than sleeps, so the suite stays fast.
 */
@ExtendWith(MockitoExtension.class)
public class PartitionActionSchedulerTest {

    private static final TaskId TASK_ID = new TaskId(0, 3);
    private static final Duration UNLIMITED_BUDGET = Duration.ofMinutes(1);
    private static final int UNLIMITED_ACTIONS = 10_000;
    private static final Duration AWAIT_TIMEOUT = Duration.ofSeconds(5);

    @Mock
    private LHServerConfig config;

    @Mock
    private CoreStoreProvider storeProvider;

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    private final BackgroundContext context = new BackgroundContext();
    private final TenantIdModel tenantId = new TenantIdModel(LHConstants.DEFAULT_TENANT);

    private KeyValueStore<String, Bytes> nativeCoreStore;
    private PartitionActionApplier<CommandProcessorOutput> applier;
    private PartitionActionScheduler<CommandProcessorOutput> scheduler;

    @BeforeEach
    void setup() {
        nativeCoreStore = TestUtil.testStore(ServerTopology.CORE_STORE);
        nativeCoreStore.init(mockProcessorContext.getStateStoreContext(), nativeCoreStore);
        applier = new PartitionActionApplier<>(mockProcessorContext, nativeCoreStore);
    }

    @AfterEach
    void tearDown() {
        if (scheduler != null) {
            scheduler.close();
        }
    }

    // ------------------------------------------------------------------
    // Worker thread lifecycle
    // ------------------------------------------------------------------

    @Test
    void shouldNotStartWorkerWhenNoJobsAreRegistered() {
        scheduler = newScheduler(List.of());

        scheduler.start();

        assertThat(scheduler.isWorkerAlive()).isFalse();
    }

    @Test
    void shouldRunRegisteredJobOnTheWorkerThread() {
        AtomicInteger runs = new AtomicInteger();
        CopyOnWriteArrayList<String> threadNames = new CopyOnWriteArrayList<>();
        scheduler = newScheduler(List.of(job("counter", Duration.ofMillis(5), ctx -> {
            threadNames.add(Thread.currentThread().getName());
            runs.incrementAndGet();
        })));

        scheduler.start();

        await().untilAsserted(() -> assertThat(runs.get()).isGreaterThanOrEqualTo(3));
        assertThat(threadNames).allSatisfy(name -> assertThat(name).isEqualTo("lh-partition-worker-" + TASK_ID));
    }

    @Test
    void shouldRespectEachJobsOwnInterval() {
        AtomicInteger fast = new AtomicInteger();
        AtomicInteger slow = new AtomicInteger();
        scheduler = newScheduler(List.of(
                job("fast", Duration.ofMillis(5), ctx -> fast.incrementAndGet()),
                job("slow", Duration.ofHours(1), ctx -> slow.incrementAndGet())));

        scheduler.start();

        await().untilAsserted(() -> assertThat(fast.get()).isGreaterThanOrEqualTo(10));
        // The hourly job gets exactly one immediate run at startup and nothing more.
        assertThat(slow.get()).isEqualTo(1);
    }

    @Test
    void shouldStopWorkerAndNotifyJobsOnClose() {
        CountDownLatch revoked = new CountDownLatch(1);
        PartitionBackgroundJob<CommandProcessorOutput> job = new PartitionBackgroundJob<>() {
            @Override
            public String name() {
                return "revocation-aware";
            }

            @Override
            public Duration interval() {
                return Duration.ofMillis(5);
            }

            @Override
            public void run(PartitionJobContext<CommandProcessorOutput> ctx) {}

            @Override
            public void onPartitionRevoked() {
                revoked.countDown();
            }
        };
        scheduler = newScheduler(List.of(job));
        scheduler.start();
        await().until(scheduler::isWorkerAlive);

        scheduler.close();

        assertThat(scheduler.isWorkerAlive()).isFalse();
        assertThat(revoked.getCount()).isZero();
    }

    @Test
    void shouldInterruptAJobThatIsBlockedOnAFullQueue() {
        scheduler = newScheduler(
                List.of(job("flooder", Duration.ofMillis(1), ctx -> {
                    while (true) {
                        ctx.forward(forwardRecord("never-drained"));
                        ctx.submit();
                    }
                })),
                2);
        scheduler.start();
        // The worker fills the queue and then blocks forever inside submit().
        await().until(() -> scheduler.pendingBatchCount() >= 2);

        scheduler.close();

        assertThat(scheduler.isWorkerAlive()).isFalse();
    }

    // ------------------------------------------------------------------
    // Queue semantics
    // ------------------------------------------------------------------

    @Test
    void shouldDiscardQueuedActionsOnClose() {
        scheduler = newScheduler(List.of(job("producer", Duration.ofHours(1), ctx -> {
            for (int i = 0; i < 5; i++) {
                ctx.forward(forwardRecord("key-" + i));
                ctx.submit();
            }
        })));
        scheduler.start();
        await().until(() -> scheduler.pendingActionCount() == 5);

        scheduler.close();

        assertThat(scheduler.pendingActionCount()).isZero();
        assertThat(mockProcessorContext.forwarded()).isEmpty();
    }

    @Test
    void shouldRejectSubmitAfterClose() {
        scheduler = newScheduler(List.of());
        scheduler.start();
        scheduler.close();

        assertThatThrownBy(() -> scheduler.submitBatch(List.of(PartitionAction.forward(forwardRecord("late")))))
                .isInstanceOf(InterruptedException.class)
                .hasMessageContaining("no longer owned");
    }

    @Test
    void shouldApplyBackpressureWhenQueueIsFull() {
        int totalActions = 6;
        int capacity = 2;
        scheduler = newScheduler(
                List.of(job("producer", Duration.ofHours(1), ctx -> {
                    for (int i = 0; i < totalActions; i++) {
                        ctx.forward(forwardRecord("key-" + i));
                        ctx.submit();
                    }
                })),
                capacity);
        scheduler.start();

        // The worker can never get ahead of the punctuator by more than the queue capacity.
        List<String> applied = new ArrayList<>();
        await().untilAsserted(() -> {
            assertThat(scheduler.pendingBatchCount()).isLessThanOrEqualTo(capacity);
            scheduler.drain(applier, UNLIMITED_BUDGET, UNLIMITED_ACTIONS);
            applied.clear();
            applied.addAll(forwardedKeys());
            assertThat(applied).hasSize(totalActions);
        });

        assertThat(applied)
                .containsExactlyElementsOf(IntStream.range(0, totalActions)
                        .mapToObj(i -> "key-" + i)
                        .toList());
    }

    // ------------------------------------------------------------------
    // Batching: a submitted batch is the unit of atomicity
    // ------------------------------------------------------------------

    /**
     * The whole point of batching. A job's atomic unit must land inside one punctuation — and
     * therefore one Kafka transaction — even when the punctuator has no budget left.
     */
    @Test
    void shouldApplyAWholeBatchEvenWhenTheBudgetIsAlreadyExhausted() throws Exception {
        scheduler = newScheduler(List.of());
        scheduler.submitBatch(List.of(
                PartitionAction.forward(forwardRecord("a")),
                PartitionAction.forward(forwardRecord("b")),
                PartitionAction.forward(forwardRecord("c"))));

        int applied = scheduler.drain(applier, Duration.ZERO, UNLIMITED_ACTIONS);

        assertThat(applied).isEqualTo(3);
        assertThat(forwardedKeys()).containsExactly("a", "b", "c");
        assertThat(scheduler.pendingActionCount()).isZero();
    }

    @Test
    void shouldNotSplitABatchWhenMaxActionsFallsInsideIt() throws Exception {
        scheduler = newScheduler(List.of());
        scheduler.submitBatch(
                List.of(PartitionAction.forward(forwardRecord("a")), PartitionAction.forward(forwardRecord("b"))));
        scheduler.submitBatch(List.of(PartitionAction.forward(forwardRecord("c"))));

        // maxActions=1 falls inside the first batch; it is still applied whole, and the second
        // batch is deferred rather than being partially applied.
        int applied = scheduler.drain(applier, UNLIMITED_BUDGET, 1);

        assertThat(applied).isEqualTo(2);
        assertThat(forwardedKeys()).containsExactly("a", "b");
        assertThat(scheduler.pendingActionCount()).isEqualTo(1);
    }

    @Test
    void shouldAutoSubmitWhateverARunLeavesStaged() {
        scheduler = newScheduler(List.of(job("forgetful", Duration.ofHours(1), ctx -> {
            ctx.forward(forwardRecord("staged-only"));
            // Never calls submit(); the scheduler does it when run() returns.
        })));
        scheduler.start();

        await().until(() -> scheduler.pendingActionCount() == 1);
    }

    /**
     * A run that dies half way through must not leave a partial unit behind: the staged actions are
     * dropped so the next run redoes the whole thing.
     */
    @Test
    void shouldDiscardStagedActionsWhenARunThrows() {
        scheduler = newScheduler(List.of(job("half-done", Duration.ofHours(1), ctx -> {
            ctx.forward(forwardRecord("first"));
            ctx.forward(forwardRecord("second"));
            throw new IllegalStateException("boom");
        })));
        scheduler.start();

        await().untilAsserted(() -> assertThat(scheduler.isWorkerAlive()).isTrue());
        assertThat(scheduler.pendingActionCount()).isZero();
        scheduler.drain(applier, UNLIMITED_BUDGET, UNLIMITED_ACTIONS);
        assertThat(mockProcessorContext.forwarded()).isEmpty();
    }

    /**
     * Batches already submitted before the failure are unaffected — only the staged remainder is
     * dropped.
     */
    @Test
    void shouldKeepBatchesSubmittedBeforeARunFailed() {
        scheduler = newScheduler(List.of(job("partial", Duration.ofHours(1), ctx -> {
            ctx.forward(forwardRecord("committed"));
            ctx.submit();
            ctx.forward(forwardRecord("staged"));
            throw new IllegalStateException("boom");
        })));
        scheduler.start();

        await().until(() -> scheduler.pendingActionCount() == 1);
        scheduler.drain(applier, UNLIMITED_BUDGET, UNLIMITED_ACTIONS);
        assertThat(forwardedKeys()).containsExactly("committed");
    }

    // ------------------------------------------------------------------
    // drain(): ordering, budgets and fault tolerance
    // ------------------------------------------------------------------

    @Test
    void shouldReturnZeroWhenNothingIsQueued() {
        scheduler = newScheduler(List.of());

        assertThat(scheduler.drain(applier, UNLIMITED_BUDGET, UNLIMITED_ACTIONS))
                .isZero();
        assertThat(mockProcessorContext.forwarded()).isEmpty();
    }

    @Test
    void shouldApplyActionsInFifoOrder() throws Exception {
        scheduler = newScheduler(List.of());
        submitOnePerBatch(5);

        int applied = scheduler.drain(applier, UNLIMITED_BUDGET, UNLIMITED_ACTIONS);

        assertThat(applied).isEqualTo(5);
        assertThat(forwardedKeys()).containsExactly("key-0", "key-1", "key-2", "key-3", "key-4");
    }

    @Test
    void shouldStopAtMaxActionsAndLeaveTheRestQueued() throws Exception {
        scheduler = newScheduler(List.of());
        submitOnePerBatch(5);

        int applied = scheduler.drain(applier, UNLIMITED_BUDGET, 2);

        assertThat(applied).isEqualTo(2);
        assertThat(scheduler.pendingActionCount()).isEqualTo(3);
        assertThat(forwardedKeys()).containsExactly("key-0", "key-1");
    }

    @Test
    void shouldResumeWhereItLeftOffOnTheNextDrain() throws Exception {
        scheduler = newScheduler(List.of());
        submitOnePerBatch(4);

        scheduler.drain(applier, UNLIMITED_BUDGET, 2);
        scheduler.drain(applier, UNLIMITED_BUDGET, 2);

        assertThat(scheduler.pendingActionCount()).isZero();
        assertThat(forwardedKeys()).containsExactly("key-0", "key-1", "key-2", "key-3");
    }

    @Test
    void shouldYieldAfterExhaustingTheTimeBudget() throws Exception {
        scheduler = newScheduler(List.of());
        submitOnePerBatch(5);

        // An already-expired budget still applies one batch, so the queue can never stall.
        int applied = scheduler.drain(applier, Duration.ZERO, UNLIMITED_ACTIONS);

        assertThat(applied).isEqualTo(1);
        assertThat(scheduler.pendingActionCount()).isEqualTo(4);
    }

    /**
     * An action that throws now aborts the punctuation rather than being swallowed. That is what
     * makes a batch all-or-nothing: Kafka Streams rolls the transaction back, and because the batch
     * is only dequeued once every action has landed, it is still there to be retried.
     */
    @Test
    void shouldPropagateActionFailureAndLeaveTheBatchQueuedForRetry() throws Exception {
        scheduler = newScheduler(List.of());
        scheduler.submitBatch(List.of(
                PartitionAction.forward(forwardRecord("before")),
                PartitionAction.put(null, new ExplodingStoreable()),
                PartitionAction.forward(forwardRecord("after"))));

        assertThatThrownBy(() -> scheduler.drain(applier, UNLIMITED_BUDGET, UNLIMITED_ACTIONS))
                .isInstanceOf(RuntimeException.class);

        assertThat(scheduler.pendingActionCount()).isEqualTo(3);
    }

    // ------------------------------------------------------------------
    // End to end: a job produces store writes that the punctuator applies
    // ------------------------------------------------------------------

    @Test
    void shouldApplyStoreWritesProducedByAJob() {
        PartitionCountedTagModel tag = new PartitionCountedTagModel(tenantId, "1/attribute-string");
        tag.setCount(7L);
        scheduler = newScheduler(List.of(job("writer", Duration.ofHours(1), ctx -> ctx.put(null, tag))));
        scheduler.start();
        await().until(() -> scheduler.pendingActionCount() == 1);

        scheduler.drain(applier, UNLIMITED_BUDGET, UNLIMITED_ACTIONS);

        ClusterScopedStore readBack = ClusterScopedStore.newInstance(nativeCoreStore, context);
        PartitionCountedTagModel stored = readBack.get(tag.getStoreKey(), PartitionCountedTagModel.class);
        assertThat(stored).isNotNull();
        assertThat(stored.getCount()).isEqualTo(7L);
    }

    @Test
    void shouldApplyStoreDeletesProducedByAJob() {
        PartitionCountedTagModel tag = new PartitionCountedTagModel(tenantId, "1/attribute-string");
        ClusterScopedStore seed = ClusterScopedStore.newInstance(nativeCoreStore, context);
        seed.put(tag);

        scheduler = newScheduler(List.of(job("deleter", Duration.ofHours(1), ctx -> ctx.delete(null, tag))));
        scheduler.start();
        await().until(() -> scheduler.pendingActionCount() == 1);

        scheduler.drain(applier, UNLIMITED_BUDGET, UNLIMITED_ACTIONS);

        assertThat(seed.get(tag.getStoreKey(), PartitionCountedTagModel.class)).isNull();
    }

    @Test
    void shouldKeepRunningJobsWhenOneOfThemThrows() {
        AtomicInteger healthyRuns = new AtomicInteger();
        scheduler = newScheduler(List.of(
                job("broken", Duration.ofMillis(5), ctx -> {
                    throw new IllegalStateException("boom");
                }),
                job("healthy", Duration.ofMillis(5), ctx -> healthyRuns.incrementAndGet())));

        scheduler.start();

        await().untilAsserted(() -> assertThat(healthyRuns.get()).isGreaterThanOrEqualTo(3));
        assertThat(scheduler.isWorkerAlive()).isTrue();
    }

    @Test
    void shouldBackOffButSurviveWhenInteractiveQueriesAreUnavailable() {
        AtomicInteger attempts = new AtomicInteger();
        scheduler = newScheduler(List.of(job("iq-reader", Duration.ofMillis(1), ctx -> {
            attempts.incrementAndGet();
            throw new InvalidStateStoreException("rebalancing");
        })));

        scheduler.start();

        await().until(() -> attempts.get() > 1);
        assertThat(scheduler.isWorkerAlive()).isTrue();
        // The 1s backoff means we should NOT have hammered IQ despite the 1ms interval.
        assertThat(attempts.get()).isLessThan(5);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private PartitionActionScheduler<CommandProcessorOutput> newScheduler(
            List<PartitionBackgroundJob<CommandProcessorOutput>> jobs) {
        return new PartitionActionScheduler<>(TASK_ID, config, storeProvider, jobs);
    }

    private PartitionActionScheduler<CommandProcessorOutput> newScheduler(
            List<PartitionBackgroundJob<CommandProcessorOutput>> jobs, int queueCapacity) {
        return new PartitionActionScheduler<>(TASK_ID, config, storeProvider, jobs, queueCapacity);
    }

    private void submitOnePerBatch(int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            scheduler.submitBatch(List.of(PartitionAction.forward(forwardRecord("key-" + i))));
        }
    }

    private static org.awaitility.core.ConditionFactory await() {
        return Awaitility.await().atMost(AWAIT_TIMEOUT).pollInterval(Duration.ofMillis(10));
    }

    private List<String> forwardedKeys() {
        List<String> keys = new ArrayList<>();
        mockProcessorContext.forwarded().forEach(f -> keys.add(f.record().key()));
        return keys;
    }

    private Record<String, CommandProcessorOutput> forwardRecord(String key) {
        CommandProcessorOutput output = new CommandProcessorOutput();
        output.partitionKey = key;
        return new Record<>(key, output, System.currentTimeMillis());
    }

    private PartitionBackgroundJob<CommandProcessorOutput> job(String name, Duration interval, JobBody body) {
        return new PartitionBackgroundJob<CommandProcessorOutput>() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Duration interval() {
                return interval;
            }

            @Override
            public void run(PartitionJobContext<CommandProcessorOutput> ctx) throws InterruptedException {
                body.run(ctx);
            }
        };
    }

    @FunctionalInterface
    private interface JobBody {
        void run(PartitionJobContext<CommandProcessorOutput> ctx) throws InterruptedException;
    }

    /**
     * A Storeable that blows up during serialization, used to drive the failure path in
     * {@code drain()}. {@code getStoreKey()} deliberately still works, since
     * {@link PartitionAction#describe()} needs it for the error log.
     */
    private static final class ExplodingStoreable extends Storeable<PartitionCountedTag> {

        @Override
        public PartitionCountedTag.Builder toProto() {
            throw new IllegalStateException("serialization exploded");
        }

        @Override
        public void initFrom(Message proto, ExecutionContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<PartitionCountedTag> getProtoBaseClass() {
            return PartitionCountedTag.class;
        }

        @Override
        public String getStoreKey() {
            return "exploding-storeable";
        }

        @Override
        public StoreableType getType() {
            return StoreableType.PARTITION_COUNTED_TAG;
        }
    }

    static {
        // Awaitility's default poll delay would add 100ms to every await() in this class.
        Awaitility.setDefaultPollDelay(0, TimeUnit.MILLISECONDS);
    }
}
