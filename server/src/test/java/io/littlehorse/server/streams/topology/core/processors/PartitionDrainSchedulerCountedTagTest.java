package io.littlehorse.server.streams.topology.core.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.PartitionCountedTagModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.server.streams.store.InMemoryKeyValueIterator;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.storeinternals.MetricsHintModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.PartitionLocalBuffer;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PartitionDrainSchedulerCountedTagTest {

    private static final String CORE_CMD_TOPIC = "core-cmd-topic";
    private static final String TENANT_ID = "my-tenant";

    @Mock
    private LHServerConfig config;

    @Mock
    private ProcessorContext<String, CommandProcessorOutput> ctx;

    @Mock
    private ClusterScopedStore store;

    private final ExecutionContext executionContext = Mockito.mock(ExecutionContext.class, Answers.RETURNS_DEEP_STUBS);

    @Captor
    private ArgumentCaptor<Record<String, CommandProcessorOutput>> recordCaptor;

    private final PartitionLocalBuffer<io.littlehorse.common.model.PartitionMetricWindowModel> metricWindows =
            new PartitionLocalBuffer<>();
    private final PartitionLocalBuffer<PartitionCountedTagModel> countedTags = new PartitionLocalBuffer<>();

    private PartitionDrainScheduler scheduler;

    @BeforeEach
    void setup() {
        scheduler = new PartitionDrainScheduler(metricWindows, countedTags, config, ctx);
    }

    @Test
    void shouldCatchUpCountedTagsFromStoreOnFirstPunctuate() {
        when(config.getCoreCmdTopicName()).thenReturn(CORE_CMD_TOPIC);

        PartitionCountedTagModel tagA = countedTag("attr-a", 3);
        PartitionCountedTagModel tagB = countedTag("attr-b", 7);
        stubCountedTagPrefixScan(tagA, tagB);
        stubMissingMetricsHint();

        scheduler.punctuate(store);

        verify(ctx, times(2)).forward(recordCaptor.capture());
        assertForwardedCountedTag(recordCaptor.getAllValues().get(0), "attr-a", 3);
        assertForwardedCountedTag(recordCaptor.getAllValues().get(1), "attr-b", 7);

        // tags are deserialized fresh from the store, so assert by value rather than identity
        ArgumentCaptor<PartitionCountedTagModel> deletedCaptor =
                ArgumentCaptor.forClass(PartitionCountedTagModel.class);
        verify(store, times(2)).delete(deletedCaptor.capture());
        assertThat(deletedCaptor.getAllValues())
                .extracting(PartitionCountedTagModel::getAttributeString)
                .containsExactlyInAnyOrder("attr-a", "attr-b");
        // catch-up never falls back to range scan when there is no metrics hint
        verify(store, never()).range(anyString(), anyString(), any());
    }

    @Test
    void shouldDrainCountedTagsFromMemoryInMemoryMode() {
        forceMemoryMode();
        when(config.getCoreCmdTopicName()).thenReturn(CORE_CMD_TOPIC);

        PartitionCountedTagModel tagA = countedTag("attr-a", 1);
        PartitionCountedTagModel tagB = countedTag("attr-b", 5);
        countedTags.put(tagA);
        countedTags.put(tagB);

        scheduler.punctuate(store);

        verify(ctx, times(2)).forward(recordCaptor.capture());
        assertThat(recordCaptor.getAllValues()).extracting(Record::key).containsExactlyInAnyOrder("attr-a", "attr-b");

        verify(store).delete(tagA);
        verify(store).delete(tagB);
        // the buffer is drained after forwarding
        assertThat(countedTags.hasEntries()).isFalse();
    }

    @Test
    void shouldForwardNothingWhenMemoryBufferIsEmpty() {
        forceMemoryMode();

        scheduler.punctuate(store);

        verify(ctx, never()).forward(any());
        // the hint is still persisted on every punctuation
        verify(store).put(any(MetricsHintModel.class));
    }

    @Test
    void shouldForwardCountedTagAsRepartitionTimerWithTenantAndAttributes() {
        forceMemoryMode();
        when(config.getCoreCmdTopicName()).thenReturn(CORE_CMD_TOPIC);

        countedTags.put(countedTag("my-attribute", 42));

        scheduler.punctuate(store);

        verify(ctx).forward(recordCaptor.capture());
        Record<String, CommandProcessorOutput> record = recordCaptor.getValue();

        assertThat(record.key()).isEqualTo("my-attribute");

        CommandProcessorOutput cpo = record.value();
        assertThat(cpo.topic).isEqualTo(CORE_CMD_TOPIC);
        assertThat(cpo.payload).isInstanceOf(LHTimer.class);

        LHTimer timer = (LHTimer) cpo.payload;
        assertThat(timer.isRepartition()).isTrue();
        assertThat(timer.getTopic()).isEqualTo(CORE_CMD_TOPIC);
        assertThat(timer.getTenantId().getId()).isEqualTo(TENANT_ID);

        assertThat(HeadersUtil.tenantIdFromMetadata(record.headers()).getId()).isEqualTo(TENANT_ID);

        assertForwardedCountedTag(record, "my-attribute", 42);
    }

    /**
     * Transitions the scheduler from the initial STORE source to MEMORY by running a single
     * punctuation against an empty store, then clears the resulting mock interactions so each
     * test can assert only the memory-drain behavior.
     */
    private void forceMemoryMode() {
        stubCountedTagPrefixScan();
        stubMissingMetricsHint();
        scheduler.punctuate(store);
        clearInvocations(ctx, store);
    }

    private void stubMissingMetricsHint() {
        when(store.get(eq(MetricsHintModel.METRICS_HINT_KEY), eq(MetricsHintModel.class)))
                .thenReturn(null);
    }

    private void stubCountedTagPrefixScan(PartitionCountedTagModel... tags) {
        // Build the iterator mock before stubbing prefixScan to avoid nested/unfinished stubbing.
        LHKeyValueIterator<PartitionCountedTagModel> iterator = iteratorOf(tags);
        when(store.prefixScan(anyString(), eq(PartitionCountedTagModel.class))).thenReturn(iterator);
    }

    private PartitionCountedTagModel countedTag(String attributeString, long count) {
        PartitionCountedTagModel tag = new PartitionCountedTagModel(new TenantIdModel(TENANT_ID), attributeString);
        for (long i = 0; i < count; i++) {
            tag.increment();
        }
        return tag;
    }

    private LHKeyValueIterator<PartitionCountedTagModel> iteratorOf(PartitionCountedTagModel... tags) {
        List<KeyValue<String, Bytes>> entries = new ArrayList<>();
        for (PartitionCountedTagModel tag : tags) {
            entries.add(new KeyValue<>(tag.getFullStoreKey(), Bytes.wrap(tag.toBytes())));
        }
        return new LHKeyValueIterator<>(
                new InMemoryKeyValueIterator(entries), PartitionCountedTagModel.class, executionContext);
    }

    private void assertForwardedCountedTag(
            Record<String, CommandProcessorOutput> record, String expectedAttribute, long expectedCount) {
        assertThat(record.key()).isEqualTo(expectedAttribute);
        LHTimer timer = (LHTimer) record.value().payload;
        try {
            Command command = Command.parseFrom(timer.getPayload());
            assertThat(command.getUpdateCountedTag().getAttributeString()).isEqualTo(expectedAttribute);
            assertThat(command.getUpdateCountedTag().getCount()).isEqualTo(expectedCount);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
