package io.littlehorse.server.streams.topology.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessingContextTest {

    @Mock
    private LHServerConfig serverConfig;

    @Mock
    private AuthorizationContext authorizationContext;

    @Mock
    private TenantIdModel tenantId;

    private TestProcessingContext processingContext;

    @BeforeEach
    void setUp() {
        processingContext = new TestProcessingContext(serverConfig, authorizationContext);
    }

    @Test
    void shouldReturnTrueWhenAllTopicsExist() throws Exception {
        TopicDescription topicDescription = mock(TopicDescription.class);
        KafkaFuture<TopicDescription> future1 = kafkaFutureOf(topicDescription);
        KafkaFuture<TopicDescription> future2 = kafkaFutureOf(topicDescription);

        when(authorizationContext.tenantId()).thenReturn(tenantId);
        when(serverConfig.outputTopicExistsFor(tenantId)).thenReturn(Map.of("topic1", future1, "topic2", future2));

        assertThat(processingContext.outputTopicsExist().get()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenTopicMapIsEmpty() throws Exception {
        when(authorizationContext.tenantId()).thenReturn(tenantId);
        when(serverConfig.outputTopicExistsFor(tenantId)).thenReturn(Map.of());

        assertThat(processingContext.outputTopicsExist().get()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenATopicDescriptionIsNull() throws Exception {
        KafkaFuture<TopicDescription> nullFuture = kafkaFutureOf(null);

        when(authorizationContext.tenantId()).thenReturn(tenantId);
        when(serverConfig.outputTopicExistsFor(tenantId)).thenReturn(Map.of("missingTopic", nullFuture));

        assertThat(processingContext.outputTopicsExist().get()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenATopicFutureCompletesExceptionally() throws Exception {
        KafkaFuture<TopicDescription> failedFuture = kafkaFutureFailedWith(new RuntimeException("Kafka admin error"));

        when(authorizationContext.tenantId()).thenReturn(tenantId);
        when(serverConfig.outputTopicExistsFor(tenantId)).thenReturn(Map.of("errorTopic", failedFuture));

        assertThat(processingContext.outputTopicsExist().get()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAtLeastOneTopicIsMissing() throws Exception {
        TopicDescription topicDescription = mock(TopicDescription.class);
        KafkaFuture<TopicDescription> existingFuture = kafkaFutureOf(topicDescription);
        KafkaFuture<TopicDescription> missingFuture = kafkaFutureOf(null);

        when(authorizationContext.tenantId()).thenReturn(tenantId);
        when(serverConfig.outputTopicExistsFor(tenantId))
                .thenReturn(Map.of("existingTopic", existingFuture, "missingTopic", missingFuture));

        assertThat(processingContext.outputTopicsExist().get()).isFalse();
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenTenantIdIsNull() {
        when(authorizationContext.tenantId()).thenReturn(null);

        assertThatThrownBy(() -> processingContext.outputTopicsExist())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Execution context without a valid Tenant ID");
    }

    @SuppressWarnings("unchecked")
    private static KafkaFuture<TopicDescription> kafkaFutureOf(TopicDescription value) {
        KafkaFuture<TopicDescription> future = mock(KafkaFuture.class);
        when(future.toCompletionStage()).thenReturn(CompletableFuture.completedFuture(value));
        return future;
    }

    @SuppressWarnings("unchecked")
    private static KafkaFuture<TopicDescription> kafkaFutureFailedWith(Throwable cause) {
        KafkaFuture<TopicDescription> future = mock(KafkaFuture.class);
        when(future.toCompletionStage()).thenReturn(CompletableFuture.failedFuture(cause));
        return future;
    }

    /** Minimal concrete subclass to exercise the abstract {@link ProcessingContext}. */
    private static class TestProcessingContext extends ProcessingContext {
        private final AuthorizationContext authorizationContext;

        TestProcessingContext(LHServerConfig serverConfig, AuthorizationContext authorizationContext) {
            super(serverConfig);
            this.authorizationContext = authorizationContext;
        }

        @Override
        public AuthorizationContext authorization() {
            return authorizationContext;
        }

        @Override
        public WfService service() {
            return null;
        }

        @Override
        public ReadOnlyMetadataManager metadataManager() {
            return null;
        }

        @Override
        public LHServerConfig serverConfig() {
            return null;
        }
    }
}
