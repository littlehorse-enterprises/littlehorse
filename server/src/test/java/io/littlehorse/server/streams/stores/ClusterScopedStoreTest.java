package io.littlehorse.server.streams.stores;

import static org.assertj.core.api.Assertions.*;

import com.google.common.collect.ImmutableList;
import io.littlehorse.TestUtil;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

public class ClusterScopedStoreTest {

    private final KeyValueStore<String, Bytes> nativeInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore("myStore"), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private CoreProcessorContext executionContext = Mockito.mock(Answers.RETURNS_DEEP_STUBS);

    private final ClusterScopedStore store = ClusterScopedStore.newInstance(nativeInMemoryStore, executionContext);

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    private final StoredGetable<WfRun, WfRunModel> getableToSave =
            TestUtil.storedWfRun(UUID.randomUUID().toString());

    @BeforeEach
    public void setup() {
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
    }

    @Test
    void shouldSupportSaveAndDelete() {
        final int storedGetableType = getableToSave.getType().getNumber();
        final int objectTypeNumber =
                getableToSave.getStoredObject().getObjectId().getType().getNumber();
        store.put(getableToSave);
        StoredGetable<WfRun, WfRunModel> wfRunWfRunModelStoredGetable =
                store.get(getableToSave.getStoredObject().getObjectId().getStoreableKey(), StoredGetable.class);
        assertThat(wfRunWfRunModelStoredGetable.getStoredObject()).isNotNull();
        List<KeyValue<String, Bytes>> keyValues = ImmutableList.copyOf(nativeInMemoryStore.all());
        assertThat(keyValues).hasSize(1);
        KeyValue<String, Bytes> storedRecord = keyValues.get(0);
        assertThat(storedRecord.key)
                .isEqualTo("%s/%s/%s"
                        .formatted(
                                storedGetableType,
                                objectTypeNumber,
                                getableToSave.getStoredObject().getId()));
        store.delete(getableToSave);
        assertThat(ImmutableList.copyOf(nativeInMemoryStore.all())).isEmpty();
    }
}
