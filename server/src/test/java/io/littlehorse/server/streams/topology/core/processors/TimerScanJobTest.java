package io.littlehorse.server.streams.topology.core.processors;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.TimerIteratorHintModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.background.PartitionActionApplier;
import io.littlehorse.server.streams.topology.core.background.PartitionActionScheduler;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.TaskId;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Covers the timer scan now that it runs off the Kafka Streams thread, on the same single worker
 * that serves the rest of the task: the job runs on a real background worker reading through a
 * stubbed {@link CoreStoreProvider}, and the actions it produces are applied by a real
 * {@link PartitionActionApplier} on the test thread, exactly as {@code CommandProcessor}'s
 * punctuator would.
 *
 * <p>The interesting cases here are not "does a timer fire" but the two failure modes the split
 * introduces: delivering a timer twice because the background scan re-read a stale view, and losing
 * a timer because the cursor advanced past one the scan never saw.
 */
@ExtendWith(MockitoExtension.class)
public class TimerScanJobTest {

    private static final TaskId TASK_ID = new TaskId(0, 0);
    private static final String TENANT_ID = "my-tenant";
    private static final String TIMER_TOPIC = "my-cluster-timer";
    private static final long ONE_SECOND = 1_000L;

    @Mock
    private LHServerConfig config;

    @Mock
    private CoreStoreProvider storeProvider;

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();
    private final BackgroundContext context = new BackgroundContext();

    private KeyValueStore<String, Bytes> nativeCoreStore;
    private ClusterScopedStore coreStore;
    private PartitionActionApplier<CommandProcessorOutput> applier;
    private PartitionActionScheduler<CommandProcessorOutput> scheduler;
    private TimerCursor cursor;

    @BeforeEach
    void setup() {
        nativeCoreStore = TestUtil.testStore(ServerTopology.CORE_STORE);
        nativeCoreStore.init(mockProcessorContext.getStateStoreContext(), nativeCoreStore);
        coreStore = ClusterScopedStore.newInstance(nativeCoreStore, context);
        applier = new PartitionActionApplier<>(mockProcessorContext, nativeCoreStore);
        cursor = new TimerCursor();
    }

    @AfterEach
    void tearDown() {
        if (scheduler != null) {
            scheduler.close();
        }
    }

    // ------------------------------------------------------------------
    // Scanning
    // ------------------------------------------------------------------

    @Test
    void shouldDeliverMaturedTimersAndRemoveThemFromTheStore() {
        LHTimer first = seedTimer(secondsAgo(30));
        LHTimer second = seedTimer(secondsAgo(20));

        start();
        drainUntil(() -> forwardedCount() == 2);

        assertThat(coreStore.get(first.getStoreKey(), LHTimer.class)).isNull();
        assertThat(coreStore.get(second.getStoreKey(), LHTimer.class)).isNull();
    }

    @Test
    void shouldNotDeliverTimersThatHaveNotMaturedYet() {
        LHTimer future = seedTimer(secondsFromNow(60));

        start();
        // Give the worker a few ticks to prove it leaves the timer alone.
        await().pollDelay(Duration.ofMillis(300)).untilAsserted(() -> {
            scheduler.drain(applier, Duration.ofMinutes(1), 1000);
            assertThat(forwardedCount()).isZero();
        });

        assertThat(coreStore.get(future.getStoreKey(), LHTimer.class)).isNotNull();
    }

    /**
     * The delivery has to leave as a {@link CommandProcessorOutput} wrapping the timer: that is the
     * shape the command router knows how to route to the timer node. Emitting a bare LHTimer would
     * blow up in the router, since the punctuator forwards from the CommandProcessor node.
     */
    @Test
    void shouldForwardTheTimerWrappedAsCommandProcessorOutputWithItsTenantHeaders() {
        seedTimer(secondsAgo(30));

        start();
        drainUntil(() -> forwardedCount() == 1);

        Record<? extends String, ? extends CommandProcessorOutput> record =
                mockProcessorContext.forwarded().get(0).record();
        assertThat(record.value().getPayload()).isInstanceOf(LHTimer.class);
        assertThat(record.value().getTopic()).isEqualTo(TIMER_TOPIC);
        assertThat(HeadersUtil.tenantIdFromMetadata(record.headers()).getId()).isEqualTo(TENANT_ID);
    }

    @Test
    void shouldAdvanceTheCursorPastDeliveredTimers() {
        long maturation = secondsAgo(30);
        seedTimer(maturation);

        start();
        drainUntil(() -> cursor.get() > 0);

        assertThat(cursor.get()).isGreaterThanOrEqualTo(maturation);
    }

    // ------------------------------------------------------------------
    // Duplicate delivery
    // ------------------------------------------------------------------

    @Test
    void shouldDeliverATimerOnlyOnceEvenIfTheScanRediscoversIt() {
        // The exact shape of a stale IQ read: the worker scheduled the same timer twice because the
        // delete from the first pass was not visible to it yet.
        LHTimer timer = seedTimer(secondsAgo(30));

        new MatureTimerAction(timer, TIMER_TOPIC).apply(applier);
        new MatureTimerAction(timer, TIMER_TOPIC).apply(applier);

        assertThat(forwardedCount()).isEqualTo(1);
        assertThat(coreStore.get(timer.getStoreKey(), LHTimer.class)).isNull();
    }

    @Test
    void shouldNotDeliverATimerThatWasAlreadyRemovedFromTheStore() {
        LHTimer timer = timerAt(secondsAgo(30));

        // Never stored: nothing to consume, so nothing to deliver.
        new MatureTimerAction(timer, TIMER_TOPIC).apply(applier);

        assertThat(forwardedCount()).isZero();
    }

    // ------------------------------------------------------------------
    // Cursor safety
    // ------------------------------------------------------------------

    @Test
    void shouldAdvanceTheCursorToTheProposedInstantWhenNothingWasMissed() {
        long proposed = secondsAgo(10);

        new TimerCursorAdvanceAction(cursor, proposed).apply(applier);

        assertThat(cursor.get()).isEqualTo(proposed);
    }

    @Test
    void shouldClampTheCursorToATimerTheScanMissed() {
        // A timer written by process() after the background scan took its stale snapshot. If the
        // cursor moved to the proposed instant it would sit past this timer's key, and no later
        // scan would ever find it: the WfRun would wait on it forever.
        long missedAt = secondsAgo(20);
        LHTimer missed = seedTimer(missedAt);
        long proposed = secondsAgo(5);

        new TimerCursorAdvanceAction(cursor, proposed).apply(applier);

        assertThat(cursor.get()).isEqualTo(missedAt);
        assertThat(coreStore.get(missed.getStoreKey(), LHTimer.class)).isNotNull();
    }

    @Test
    void shouldRescanATimerThatTheCursorWasClampedTo() {
        long missedAt = secondsAgo(20);
        LHTimer missed = seedTimer(missedAt);
        new TimerCursorAdvanceAction(cursor, secondsAgo(5)).apply(applier);

        // The clamp is only worth anything if the next pass actually picks the timer back up.
        start();
        drainUntil(() -> forwardedCount() == 1);

        assertThat(coreStore.get(missed.getStoreKey(), LHTimer.class)).isNull();
    }

    @Test
    void shouldNeverMoveTheCursorBackwards() {
        new TimerCursorAdvanceAction(cursor, secondsAgo(10)).apply(applier);
        long advanced = cursor.get();

        new TimerCursorAdvanceAction(cursor, secondsAgo(60)).apply(applier);

        assertThat(cursor.get()).isEqualTo(advanced);
    }

    @Test
    void shouldCheckpointTheResumeHintWhenTheCursorAdvances() {
        long proposed = secondsAgo(10);

        new TimerCursorAdvanceAction(cursor, proposed).apply(applier);

        TimerIteratorHintModel hint =
                coreStore.get(TimerIteratorHintModel.TIMER_ITERATOR_HINT_KEY, TimerIteratorHintModel.class);
        assertThat(hint).isNotNull();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void start() {
        Mockito.when(config.getTimerTopic()).thenReturn(TIMER_TOPIC);
        Mockito.when(storeProvider.nativeCoreStore(TASK_ID.partition())).thenReturn(nativeCoreStore);
        scheduler = new PartitionActionScheduler<>(
                TASK_ID, config, storeProvider, List.of(new TimerScanJob(cursor, config)));
        scheduler.start();
    }

    /**
     * Plays the role of the punctuator: keeps draining until the expected state is reached.
     */
    private void drainUntil(BooleanSupplier done) {
        await().untilAsserted(() -> {
            scheduler.drain(applier, Duration.ofMinutes(1), 1000);
            assertThat(done.getAsBoolean()).isTrue();
        });
    }

    private int forwardedCount() {
        return mockProcessorContext.forwarded().size();
    }

    private static long secondsAgo(int seconds) {
        return System.currentTimeMillis() - (seconds * ONE_SECOND);
    }

    private static long secondsFromNow(int seconds) {
        return System.currentTimeMillis() + (seconds * ONE_SECOND);
    }

    private LHTimer seedTimer(long maturationMillis) {
        LHTimer timer = timerAt(maturationMillis);
        coreStore.put(timer);
        return timer;
    }

    private LHTimer timerAt(long maturationMillis) {
        LHTimer timer = new LHTimer();
        timer.setMaturationTime(new Date(maturationMillis));
        timer.setTopic("core-cmd-topic");
        timer.setPartitionKey("partition-key");
        timer.setPayload(new byte[] {1, 2, 3});
        timer.setTenantId(new TenantIdModel(TENANT_ID));
        timer.setPrincipalId(new PrincipalIdModel("anonymous"));
        // Materialise the store key up front so the test and the store agree on it.
        timer.getStoreKey();
        return timer;
    }

    private static ConditionFactory await() {
        return Awaitility.await().atMost(Duration.ofSeconds(5)).pollInterval(Duration.ofMillis(10));
    }

    static {
        Awaitility.setDefaultPollDelay(0, TimeUnit.MILLISECONDS);
    }
}
