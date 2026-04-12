package io.littlehorse.server.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.health.InProgressRestoration;
import io.littlehorse.server.monitoring.health.ServerHealthState;
import io.littlehorse.server.monitoring.http.ContentType;
import io.littlehorse.server.monitoring.http.LHHttpException;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.processor.StandbyUpdateListener;
import org.apache.kafka.streams.processor.TaskId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    private final StatusServer statusServer = mock();
    private final LHServerConfig config = mock();
    private final KafkaStreams coreStreams = mock();
    private final KafkaStreams timerStreams = mock();
    private final TaskQueueManager taskQueueManager = mock();
    private final MetadataCache metadataCache = mock();
    private final BackendInternalComms internalComms = mock();
    private final Map<String, Supplier<String>> handlers = new HashMap<>();
    private KafkaStreams.StateListener coreStateListener;
    private KafkaStreams.StateListener timerStateListener;
    private HealthService healthService;
    private final Gson gson = new Gson();

    @BeforeEach
    void setup() {
        when(config.getLHClusterId()).thenReturn("test-cluster");
        when(config.getServerMetricLevel()).thenReturn("INFO");
        when(config.getStateDirectory()).thenReturn(System.getProperty("java.io.tmpdir"));
        when(config.getLHInstanceName()).thenReturn("test-instance");
        when(config.partitionsByTopic()).thenReturn(Map.of("test-cluster-core-store-changelog", 10, "test-topic", 10));
        when(config.getPrometheusExporterPath()).thenReturn("/metrics");
        when(config.getLivenessPath()).thenReturn("/liveness");
        when(config.getReadinessPath()).thenReturn("/readiness");
        when(config.getStatusPath()).thenReturn("/status");
        when(config.getDiskUsagePath()).thenReturn("/disk");
        when(config.getStandbyStatusPath()).thenReturn("/standby");

        lenient().when(coreStreams.metadataForLocalThreads()).thenReturn(Collections.emptySet());

        doAnswer(invocation -> {
                    handlers.put(invocation.getArgument(0), invocation.getArgument(2));
                    return null;
                })
                .when(statusServer)
                .handle(anyString(), any(ContentType.class), any());

        doAnswer(invocation -> {
                    coreStateListener = invocation.getArgument(0);
                    return null;
                })
                .when(coreStreams)
                .setStateListener(any());

        doAnswer(invocation -> {
                    timerStateListener = invocation.getArgument(0);
                    return null;
                })
                .when(timerStreams)
                .setStateListener(any());

        healthService = new HealthService(
                statusServer, config, coreStreams, timerStreams, taskQueueManager, metadataCache, internalComms);
    }

    @Nested
    class ReadinessTests {

        @Test
        void shouldBeReadyWhenCoreStateIsRunning() {
            setCoreState(State.RUNNING);
            assertThat(readinessHandler().get()).isEqualTo("OK!");
        }

        @Test
        void shouldBeReadyWhenCoreStateIsRebalancing() {
            setCoreState(State.REBALANCING);
            assertThat(readinessHandler().get()).isEqualTo("OK!");
        }

        @Test
        void shouldNotBeReadyWhenNoStateYet() {
            assertThatThrownBy(() -> readinessHandler().get()).isInstanceOf(LHHttpException.class);
        }

        @Test
        void shouldNotBeReadyWhenCoreStateIsCreated() {
            setCoreState(State.CREATED);
            assertThatThrownBy(() -> readinessHandler().get()).isInstanceOf(LHHttpException.class);
        }

        @Test
        void shouldNotBeReadyWhenCoreStateIsInError() {
            setCoreState(State.ERROR);
            assertThatThrownBy(() -> readinessHandler().get()).isInstanceOf(LHHttpException.class);
        }

        @Test
        void shouldNotBeReadyWhenCoreStateIsNotRunning() {
            setCoreState(State.NOT_RUNNING);
            assertThatThrownBy(() -> readinessHandler().get()).isInstanceOf(LHHttpException.class);
        }

        @Test
        void shouldNotBeReadyWhenCoreStateIsPendingShutdown() {
            setCoreState(State.PENDING_SHUTDOWN);
            assertThatThrownBy(() -> readinessHandler().get()).isInstanceOf(LHHttpException.class);
        }

        @Test
        void shouldNotBeReadyWhenCoreStateIsPendingError() {
            setCoreState(State.PENDING_ERROR);
            assertThatThrownBy(() -> readinessHandler().get()).isInstanceOf(LHHttpException.class);
        }
    }

    @Nested
    class LivenessTests {

        @Test
        void shouldBeAliveWhenNoStateYet() {
            assertThat(livenessHandler().get()).isEqualTo("OK!");
        }

        @Test
        void shouldBeAliveWhenBothTopologiesAreRunning() {
            setCoreState(State.RUNNING);
            setTimerState(State.RUNNING);
            assertThat(livenessHandler().get()).isEqualTo("OK!");
        }

        @Test
        void shouldBeAliveWhenCoreIsRebalancing() {
            setCoreState(State.REBALANCING);
            assertThat(livenessHandler().get()).isEqualTo("OK!");
        }

        @Test
        void shouldBeAliveWhenCoreIsPendingShutdown() {
            setCoreState(State.PENDING_SHUTDOWN);
            assertThat(livenessHandler().get()).isEqualTo("OK!");
        }

        @Test
        void shouldNotBeAliveWhenCoreIsInError() {
            setCoreState(State.ERROR);
            assertThatThrownBy(() -> livenessHandler().get()).isInstanceOf(LHHttpException.class);
        }

        @Test
        void shouldNotBeAliveWhenCoreIsPendingError() {
            setCoreState(State.PENDING_ERROR);
            assertThatThrownBy(() -> livenessHandler().get()).isInstanceOf(LHHttpException.class);
        }

        @Test
        void shouldNotBeAliveWhenCoreIsNotRunning() {
            setCoreState(State.NOT_RUNNING);
            assertThatThrownBy(() -> livenessHandler().get()).isInstanceOf(LHHttpException.class);
        }

        @Test
        void shouldNotBeAliveWhenTimerIsInError() {
            setCoreState(State.RUNNING);
            setTimerState(State.ERROR);
            assertThatThrownBy(() -> livenessHandler().get()).isInstanceOf(LHHttpException.class);
        }

        @Test
        void shouldNotBeAliveWhenTimerIsNotRunning() {
            setCoreState(State.RUNNING);
            setTimerState(State.NOT_RUNNING);
            assertThatThrownBy(() -> livenessHandler().get()).isInstanceOf(LHHttpException.class);
        }

        @Test
        void shouldNotBeAliveWhenTimerIsPendingError() {
            setCoreState(State.RUNNING);
            setTimerState(State.PENDING_ERROR);
            assertThatThrownBy(() -> livenessHandler().get()).isInstanceOf(LHHttpException.class);
        }
    }

    @Nested
    class StateRestoreListenerTests {

        private static final String CHANGELOG_TOPIC = "test-changelog";

        @Test
        void shouldTrackRestorationOnStart() {
            TopicPartition tp = new TopicPartition(CHANGELOG_TOPIC, 0);
            healthService.onRestoreStart(tp, "test-store", 0L, 1000L);
            healthService.onBatchRestored(tp, "test-store", 500, 500);

            assertThat(restorationStatus(tp).getCurrentOffset()).isEqualTo(500);
            assertThat(restorationStatus(tp).getEndOffset()).isEqualTo(1000L);
            assertThat(restorationStatus(tp).getTotalRestored()).isEqualTo(500);

            healthService.onBatchRestored(tp, "test-store", 999L, 499L);

            assertThat(restorationStatus(tp).getCurrentOffset()).isEqualTo(999L);
            assertThat(restorationStatus(tp).getEndOffset()).isEqualTo(1000L);
            assertThat(restorationStatus(tp).getTotalRestored()).isEqualTo(999L);
        }

        @Test
        void shouldCompleteRestorationLifecycle() {
            TopicPartition tp = new TopicPartition(CHANGELOG_TOPIC, 0);
            healthService.onRestoreStart(tp, "test-store", 0L, 1000L);
            healthService.onBatchRestored(tp, "test-store", 500L, 500L);
            healthService.onBatchRestored(tp, "test-store", 1000L, 500L);
            assertThat(status().getRestorations()).isNotEmpty();
            healthService.onRestoreEnd(tp, "test-store", 1000L);
            assertThat(status().getRestorations()).isEmpty();
        }

        @Test
        void shouldHandleMultiplePartitions() {
            TopicPartition tp0 = new TopicPartition(CHANGELOG_TOPIC, 0);
            TopicPartition tp1 = new TopicPartition(CHANGELOG_TOPIC, 1);

            healthService.onRestoreStart(tp0, "test-store", 0, 1000);
            healthService.onRestoreStart(tp1, "test-store", 0, 2000);

            assertThat(status().getRestorations()).hasSize(2);
            assertThat(restorationStatus(tp0)).isNotNull();
            assertThat(restorationStatus(tp1)).isNotNull();
        }

        @Test
        void shouldRemoveRestorationOnSuspend() {
            TopicPartition tp = new TopicPartition(CHANGELOG_TOPIC, 0);
            healthService.onRestoreStart(tp, "test-store", 0, 1000);
            assertThat(status().getRestorations()).hasSize(1);
            healthService.onRestoreSuspended(tp, "test-store", 500);
            assertThat(status().getRestorations()).isEmpty();
        }
    }

    @Nested
    class StandbyUpdateListenerTests {

        @Test
        void shouldTrackStandbyStoreOnUpdateStart() {
            TopicPartition tp = new TopicPartition("test-topic", 0);
            healthService.onUpdateStart(tp, "test-store", 100);

            assertThat(standbyStores("test-store")).isNotNull();
        }

        @Test
        void shouldTrackMultipleStandbyStores() {
            TopicPartition tp0 = new TopicPartition("test-topic", 0);
            TopicPartition tp1 = new TopicPartition("test-topic", 1);

            healthService.onUpdateStart(tp0, "store-a", 100);
            healthService.onUpdateStart(tp1, "store-b", 200);

            assertThat(standbyStores("store-a")).isNotNull();
            assertThat(standbyStores("store-b")).isNotNull();
        }

        @Test
        void shouldUpdateStandbyStoreOnBatchLoaded() {
            TopicPartition tp = new TopicPartition("test-topic", 0);
            TaskId taskId = new TaskId(1, 0);

            healthService.onUpdateStart(tp, "test-store", 0L);
            healthService.onBatchLoaded(tp, "test-store", taskId, 250L, 250L, 2000L);
            healthService.onBatchLoaded(tp, "test-store", taskId, 500L, 250L, 2000L);
            Set<StandbyTopicPartitionMetrics> runningPartitions =
                    standbyStores("test-store").getPartitions();
            assertThat(runningPartitions).hasSize(1);
            StandbyTopicPartitionMetrics runningPartition =
                    runningPartitions.toArray(new StandbyTopicPartitionMetrics[0])[0];
            assertThat(runningPartition.getTopic()).isEqualTo(tp.topic());
            assertThat(runningPartition.getPartition()).isEqualTo(tp.partition());
            assertThat(runningPartition.getCurrentOffset()).isEqualTo(500L);
            assertThat(runningPartition.getEndOffset()).isEqualTo(2000L);
            assertThat(runningPartition.getCurrentLag()).isEqualTo(1500L);
        }

        @Test
        void shouldHandleStandbyPartitionSuspension() {
            TopicPartition tp0 = new TopicPartition("test-topic", 0);
            TopicPartition tp1 = new TopicPartition("test-topic", 1);
            healthService.onUpdateStart(tp0, "test-store", 0L);
            healthService.onUpdateStart(tp1, "test-store", 100L);
            assertThat(standbyStores("test-store").getPartitions()).hasSize(2);
            healthService.onUpdateSuspended(tp1, "test-store", 1L, 2L, StandbyUpdateListener.SuspendReason.PROMOTED);
            assertThat(standbyStores("test-store").getPartitions()).hasSize(1);
        }
    }

    private Supplier<String> statusHandler() {
        return handlers.get("/status");
    }

    private Supplier<String> readinessHandler() {
        return handlers.get("/readiness");
    }

    private Supplier<String> livenessHandler() {
        return handlers.get("/liveness");
    }

    private Supplier<String> standbyHandler() {
        return handlers.get("/standby");
    }

    private void setCoreState(State newState) {
        coreStateListener.onChange(newState, State.CREATED);
    }

    private void setTimerState(State newState) {
        timerStateListener.onChange(newState, State.CREATED);
    }

    private ServerHealthState status() {
        return gson.fromJson(statusHandler().get(), ServerHealthState.class);
    }

    private InProgressRestoration restorationStatus(TopicPartition tp) {
        return status().getRestorations().stream()
                .filter(restoration ->
                        restoration.getTopic().equals(tp.topic()) && restoration.getPartition() == tp.partition())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No restoration found for partition " + tp));
    }

    @SuppressWarnings("unchecked")
    private StandbyStoresOnInstance standbyStores(String storeName) {
        String status = standbyHandler().get();
        return gson.fromJson(
                gson.toJson(gson.fromJson(status, Map.class).get(storeName)), StandbyStoresOnInstance.class);
    }
}
