package io.littlehorse.server.streams.topology.timer;

import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.util.serde.LHSerde;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TimerProcessorTest {

    private final KeyValueStore<String, LHTimer> nativeInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.TIMER_STORE),
                    Serdes.String(),
                    new LHSerde<>(LHTimer.class))
            .withLoggingDisabled()
            .build();

    private final MockProcessorContext<String, LHTimer> mockProcessorContext = new MockProcessorContext<>();

    private TimerProcessor processor;

    private Punctuator timerPunctuator;

    private final String testTopicName = "testTopic";

    private MockProcessorContext.CapturedPunctuator scheduledPunctuator;

    @BeforeEach
    public void setup() {
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
        processor = new TimerProcessor();
        processor.init(mockProcessorContext);
        Assertions.assertThat(mockProcessorContext.scheduledPunctuators()).hasSize(1);
        scheduledPunctuator = mockProcessorContext.scheduledPunctuators().get(0);
        timerPunctuator = scheduledPunctuator.getPunctuator();
    }

    @Test
    public void supportSystemTimePunctuatorConsistency() {
        Assertions.assertThat(scheduledPunctuator.getType()).isEqualTo(PunctuationType.WALL_CLOCK_TIME);
    }

    @Test
    public void supportPunctuatorIntervalConfigConsistency() {
        Assertions.assertThat(scheduledPunctuator.getInterval()).isEqualTo(LHConstants.PUNCTUATOR_INERVAL);
    }

    @Test
    public void shouldForwardTimerOnMaturationTimeReached() {
        LocalDateTime currentTime = LocalDateTime.now();
        LHTimer tomorrowTask = buildNewTimer("tomorrowTask", currentTime.plusDays(1));
        LHTimer nextWeekTask = buildNewTimer("nextWeekTask", currentTime.plusWeeks(1));
        LHTimer nextMothTask = buildNewTimer("nextMothTask", currentTime.plusMonths(1));
        processor.process(new Record<>("tomorrowTask", tomorrowTask, 0L));
        processor.process(new Record<>("nextWeekTask", nextWeekTask, 0L));
        processor.process(new Record<>("nextMothTask", nextMothTask, 0L));

        punctuateAndVerifyForwardedRecords(currentTime, 0);
        punctuateAndVerifyForwardedRecords(currentTime.plusDays(2), 1);
        punctuateAndVerifyForwardedRecords(currentTime.plusWeeks(2), 1);
        punctuateAndVerifyForwardedRecords(currentTime.plusMonths(2), 1);
        punctuateAndVerifyForwardedRecords(currentTime.plusYears(1), 0);
    }

    @Test
    public void shouldForwardTimerWithHeadersMetadata() {
        LocalDateTime currentTime = LocalDateTime.now();
        LHTimer tomorrowTask = buildNewTimer("tomorrowTask", currentTime);
        processor.process(new Record<>("tomorrowTask", tomorrowTask, 0L));

        timerPunctuator.punctuate(timeToDate(currentTime.plusHours(1)).getTime());
        List<MockProcessorContext.CapturedForward<? extends String, ? extends LHTimer>> forwardedRecords =
                mockProcessorContext.forwarded();
        Assertions.assertThat(forwardedRecords).hasSize(1);
        Record<? extends String, ? extends LHTimer> forwardedRecord =
                forwardedRecords.get(0).record();
        Headers metadata = forwardedRecord.headers();
        Assertions.assertThat(HeadersUtil.tenantIdFromMetadata(metadata).getId())
                .isEqualTo("tenant1");
        Assertions.assertThat(HeadersUtil.principalIdFromMetadata(metadata).getId())
                .isEqualTo("principal1");
    }

    @Test
    public void shouldRemoveTimerOnMaturationTimeReached() {
        LocalDateTime currentTime = LocalDateTime.now();
        LHTimer tomorrowTask = buildNewTimer("my-task", currentTime.plusDays(1));
        processor.process(new Record<>("my-task", tomorrowTask, 0L));
        Assertions.assertThat(ImmutableList.copyOf(nativeInMemoryStore.all())).hasSize(1);
        punctuateAndVerifyForwardedRecords(currentTime.plusDays(2), 1);
        Assertions.assertThat(ImmutableList.copyOf(nativeInMemoryStore.all())).isEmpty();
    }

    @Test
    public void supportTimerDistinctionByTenant() {
        LHTimer tenantATask = buildNewTimer("my-task", LocalDateTime.now());
        tenantATask.setTenantId(new TenantIdModel("tenantA"));
        LHTimer tenantBTask = buildNewTimer("otherTask", LocalDateTime.now());
        tenantBTask.setTenantId(new TenantIdModel("tenantB"));
        processor.process(new Record<>("my-task", tenantATask, 0L));
        processor.process(new Record<>("my-task", tenantBTask, 0L));
        Assertions.assertThat(ImmutableList.copyOf(nativeInMemoryStore.all())).hasSize(2);
        ImmutableList<KeyValue<String, LHTimer>> tasks = ImmutableList.copyOf(nativeInMemoryStore.all());
        Assertions.assertThat(tasks)
                .map(stringLHTimerKeyValue -> stringLHTimerKeyValue.key)
                .anyMatch(key -> key.contains("/tenant_tenantA"))
                .anyMatch(key -> key.contains("/tenant_tenantB"))
                .hasSize(2);
    }

    private void punctuateAndVerifyForwardedRecords(LocalDateTime time, int expectedSize) {
        timerPunctuator.punctuate(timeToDate(time).getTime());
        Assertions.assertThat(mockProcessorContext.forwarded()).hasSize(expectedSize);
        mockProcessorContext.resetForwards();
    }

    private LHTimer buildNewTimer(String partitionKey, LocalDateTime maturationTime) {
        CommandModel mockCommand = mock(Answers.RETURNS_DEEP_STUBS);
        when(mockCommand.getTime()).thenReturn(timeToDate(maturationTime));
        when(mockCommand.toProto().build().toByteArray()).thenReturn("Hi!".getBytes());
        when(mockCommand.getPartitionKey()).thenReturn(partitionKey);
        LHTimer timer = new LHTimer(mockCommand);
        timer.topic = "myTopic";
        timer.setPrincipalId(new PrincipalIdModel("principal1"));
        timer.setTenantId(new TenantIdModel("tenant1"));
        return timer;
    }

    private Date timeToDate(LocalDateTime maturationTime) {
        return Date.from(maturationTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
