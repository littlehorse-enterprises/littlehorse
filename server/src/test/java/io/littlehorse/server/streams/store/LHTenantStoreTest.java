package io.littlehorse.server.streams.store;

import static org.assertj.core.api.Assertions.*;

import com.google.common.collect.ImmutableList;
import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LHTenantStoreTest {

    @Mock
    private LHServerConfig lhConfig;

    private final String tenantA = "A";
    private final String tenantB = "B";
    private final KeyValueStore<String, Bytes> nativeInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore("myStore"), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    private final LHStore storeForTenantA = new LHTenantStore(nativeInMemoryStore, lhConfig, tenantA);
    private final LHStore storeForTenantB = new LHTenantStore(nativeInMemoryStore, lhConfig, tenantB);

    private final StoredGetable<WfRun, WfRunModel> getableToSave =
            TestUtil.storedWfRun(UUID.randomUUID().toString());

    @BeforeEach
    public void setup() {
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
    }

    @Test
    public void shouldSupportSaveAndDeleteWithTenantIsolation() {
        String testId = getableToSave.getStoredObject().getId();
        int storedGetableTypeIndex = getableToSave.getType().getNumber();
        int objectTypeIndex =
                getableToSave.getStoredObject().getObjectId().getType().getNumber();
        storeForTenantA.put(getableToSave);
        storeForTenantB.put(getableToSave);
        String expectedKey = "%s/%s/%s/%s".formatted(tenantA, storedGetableTypeIndex, objectTypeIndex, testId);
        List<KeyValue<String, Bytes>> allElementsInStore = ImmutableList.copyOf(nativeInMemoryStore.all());
        assertThat(allElementsInStore).hasSize(2);
        Bytes storedBytes = nativeInMemoryStore.get(expectedKey);
        assertThat(storedBytes).isNotNull();
        storeForTenantA.delete(getableToSave);
        allElementsInStore = ImmutableList.copyOf(nativeInMemoryStore.all());
        assertThat(allElementsInStore).hasSize(1);
        storeForTenantB.delete(getableToSave);
        allElementsInStore = ImmutableList.copyOf(nativeInMemoryStore.all());
        assertThat(allElementsInStore).hasSize(0);
    }

    @Test
    public void shouldSupportRangeScan() {
        storeForTenantA.put(getableToSave);
        storeForTenantB.put(getableToSave);
        String startKey = "";
        String endKey = "~";
        try (LHKeyValueIterator<?> wfRunStoredOnTenantA =
                storeForTenantA.range(startKey, endKey, StoredGetable.class)) {
            assertThat(ImmutableList.copyOf(wfRunStoredOnTenantA)).hasSize(1);
        }
        try (LHKeyValueIterator<?> wfRunStoredOnTenantB =
                storeForTenantB.range(startKey, endKey, StoredGetable.class)) {
            assertThat(ImmutableList.copyOf(wfRunStoredOnTenantB)).hasSize(1);
        }
    }

    @Test
    public void shouldSupportReverseScan() {
        storeForTenantA.put(getableToSave);
        storeForTenantB.put(getableToSave);
        String greatestAsciiCharacter = "~";
        storeForTenantA.put(TestUtil.storedWfRun(greatestAsciiCharacter));
        storeForTenantB.put(TestUtil.storedWfRun(greatestAsciiCharacter));
        try (LHKeyValueIterator<?> wfRunStoredOnTenantA = storeForTenantA.reversePrefixScan("", StoredGetable.class)) {
            assertThat(wfRunStoredOnTenantA.next()).isNotNull();
            assertThat(wfRunStoredOnTenantA.next().getValue().getStoreKey()).isEqualTo(getableToSave.getStoreKey());
            assertThat(wfRunStoredOnTenantA.hasNext()).isFalse();
        }
        try (LHKeyValueIterator<?> wfRunStoredOnTenantB = storeForTenantB.reversePrefixScan("", StoredGetable.class)) {
            assertThat(wfRunStoredOnTenantB.next()).isNotNull();
            assertThat(wfRunStoredOnTenantB.next().getValue().getStoreKey()).isEqualTo(getableToSave.getStoreKey());
            assertThat(wfRunStoredOnTenantB.hasNext()).isFalse();
        }
    }

    @Test
    public void shouldFindLastStoredGetableFromPrefix() {
        storeForTenantA.put(getableToSave);
        storeForTenantB.put(getableToSave);
        String greatestAsciiCharacter = "~";
        storeForTenantA.put(TestUtil.storedWfRun(greatestAsciiCharacter + "A"));
        storeForTenantB.put(TestUtil.storedWfRun(greatestAsciiCharacter + "B"));
        StoredGetable<WfRun, WfRunModel> lastStoredWfRunForTenantA =
                storeForTenantA.getLastFromPrefix("", StoredGetable.class);
        StoredGetable<WfRun, WfRunModel> lastStoredWfRunForTenantB =
                storeForTenantB.getLastFromPrefix("", StoredGetable.class);
        assertThat(lastStoredWfRunForTenantA.getStoredObject().getId()).isEqualTo(greatestAsciiCharacter + "A");
        assertThat(lastStoredWfRunForTenantB.getStoredObject().getId()).isEqualTo(greatestAsciiCharacter + "B");
    }

    @Test
    public void shouldDeleteSpecificStoreKey() {
        String otherTenant = "otherTenant";
        String objectFullKey = Storeable.getFullStoreKey(StoreableType.TAG, "123");
        String roomerKey = "%s/%s".formatted(tenantA, objectFullKey);
        String outsiderKey = "%s/%s".formatted(otherTenant, objectFullKey);
        nativeInMemoryStore.put(roomerKey, new Bytes(roomerKey.getBytes()));
        nativeInMemoryStore.put(outsiderKey, new Bytes(outsiderKey.getBytes()));

        storeForTenantA.delete("123", StoreableType.TAG);

        List<KeyValue<String, Bytes>> finalState = ImmutableList.copyOf(nativeInMemoryStore.all());
        assertThat(finalState).hasSize(1);
        assertThat(nativeInMemoryStore.get(roomerKey)).isNull();
        assertThat(nativeInMemoryStore.get(outsiderKey)).isNotNull();
    }

    @Test
    public void shouldFindStoredGetable() {
        storeForTenantA.put(getableToSave);
        storeForTenantB.put(getableToSave);

        assertThat(storeForTenantA.get(getableToSave.getStoreKey(), StoredGetable.class))
                .isNotNull();
        assertThat(storeForTenantB.get(getableToSave.getStoreKey(), StoredGetable.class))
                .isNotNull();
        assertThat(storeForTenantA.get(getableToSave.getStoredObject().getObjectId()))
                .isNotNull();
        assertThat(storeForTenantB.get(getableToSave.getStoredObject().getObjectId()))
                .isNotNull();
    }
}
