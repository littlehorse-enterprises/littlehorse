package io.littlehorse.server.streams.topology.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Guards the store lookup against the window between "Kafka Streams starts shutting down" and "the
 * processor is closed". During that window the background workers are still ticking, and letting
 * {@code KafkaStreams.store()} throw its IllegalStateException there produces a stack trace per
 * tick, which is what flooded the logs in the shutdown scenario.
 */
@ExtendWith(MockitoExtension.class)
public class CoreStoreProviderTest {

    @Mock
    private KafkaStreams serverInstance;

    @Mock
    private ReadOnlyKeyValueStore<String, Bytes> store;

    private CoreStoreProvider provider;

    @BeforeEach
    void setUp() {
        provider = new CoreStoreProvider(serverInstance);
    }

    @Test
    @DisplayName("Store lookups are reported as unavailable once Kafka Streams starts shutting down")
    void shouldReportUnavailableWhileShuttingDown() {
        when(serverInstance.state()).thenReturn(KafkaStreams.State.PENDING_SHUTDOWN);

        assertThatThrownBy(() -> provider.nativeCoreStore())
                .isInstanceOfSatisfying(
                        LHApiException.class,
                        e -> assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE));

        verify(serverInstance, never()).store(any());
    }

    @Test
    @DisplayName("A stopped Kafka Streams is unavailable rather than throwing from store()")
    void shouldReportUnavailableWhenNotRunning() {
        when(serverInstance.state()).thenReturn(KafkaStreams.State.NOT_RUNNING);

        assertThatThrownBy(() -> provider.getNativeGlobalStore())
                .isInstanceOfSatisfying(
                        LHApiException.class,
                        e -> assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE));

        verify(serverInstance, never()).store(any());
    }

    @Test
    @DisplayName("Store lookups keep working while Kafka Streams is running")
    void shouldReadStoreWhileRunning() {
        when(serverInstance.state()).thenReturn(KafkaStreams.State.RUNNING);
        when(serverInstance.store(any())).thenReturn(store);

        assertThat(provider.nativeCoreStore()).isSameAs(store);
    }

    @Test
    @DisplayName("Stale reads stay allowed during a rebalance")
    void shouldAllowStaleReadsWhileRebalancing() {
        when(serverInstance.state()).thenReturn(KafkaStreams.State.REBALANCING);
        when(serverInstance.store(any())).thenReturn(store);

        assertThat(provider.nativeCoreStore()).isSameAs(store);
    }
}
