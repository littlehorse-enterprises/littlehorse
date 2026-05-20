package io.littlehorse.server.streams.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.protobuf.Message;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.server.streams.store.StoredGetable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetadataCacheTest {

    private MetadataCache cache;

    @BeforeEach
    void setup() {
        cache = new MetadataCache();
    }

    @Test
    void getOrUpdateShouldCallSupplierWhenKeyIsNotCached() {
        StoredGetable<?, ?> storedGetable = mockStoredGetable();
        Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> supplier = mockSupplier(storedGetable);

        StoredGetable<?, ?> result = cache.getOrUpdate("my-key", supplier);

        assertThat(result).isEqualTo(storedGetable);
        verify(supplier, times(1)).get();
    }

    @Test
    void getOrUpdateShouldReturnCachedValueWithoutCallingSupplier() {
        StoredGetable<?, ?> storedGetable = mockStoredGetable();
        cache.update("my-key", (StoredGetable) storedGetable);

        Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> supplier = mockSupplier(null);

        StoredGetable<?, ?> result = cache.getOrUpdate("my-key", supplier);

        assertThat(result).isEqualTo(storedGetable);
        verify(supplier, times(0)).get();
    }

    @Test
    void getOrUpdateShouldReturnNullWhenKeyIsCachedAsNull() {
        // Simulate caching a null value (key exists but value is null)
        cache.update("my-key", null);

        Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> supplier = mockSupplier(null);

        StoredGetable<?, ?> result = cache.getOrUpdate("my-key", supplier);

        assertThat(result).isNull();
        verify(supplier, times(0)).get();
    }

    @Test
    void getOrUpdateShouldCacheNullWhenSupplierReturnsNull() {
        Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> supplier = mockSupplier(null);

        StoredGetable<?, ?> result = cache.getOrUpdate("my-key", supplier);

        assertThat(result).isNull();
        verify(supplier, times(1)).get();

        // Second call should not invoke the supplier again (null is cached)
        Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> secondSupplier = mockSupplier(null);
        cache.getOrUpdate("my-key", secondSupplier);
        verify(secondSupplier, times(0)).get();
    }

    @Test
    void evictShouldRemoveKeyFromCache() {
        StoredGetable<?, ?> storedGetable = mockStoredGetable();
        cache.update("my-key", (StoredGetable) storedGetable);

        cache.evict("my-key");

        Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> supplier = mockSupplier(storedGetable);
        StoredGetable<?, ?> result = cache.getOrUpdate("my-key", supplier);

        // After eviction, the supplier should be called again
        verify(supplier, times(1)).get();
    }

    @Test
    void updateShouldOverwriteExistingValue() {
        StoredGetable<?, ?> first = mockStoredGetable();
        StoredGetable<?, ?> second = mockStoredGetable();

        cache.update("my-key", (StoredGetable) first);
        cache.update("my-key", (StoredGetable) second);

        Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> supplier = mockSupplier(null);
        StoredGetable<?, ?> result = cache.getOrUpdate("my-key", supplier);

        assertThat(result).isEqualTo(second);
        verify(supplier, times(0)).get();
    }

    @Test
    void getOrUpdateShouldOnlyCallSupplierOnceForConcurrentAccessToSameKey() throws InterruptedException {
        AtomicInteger supplierCallCount = new AtomicInteger(0);
        StoredGetable<?, ?> storedGetable = mockStoredGetable();

        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    cache.getOrUpdate("concurrent-key", () -> {
                        supplierCallCount.incrementAndGet();
                        return (StoredGetable) storedGetable;
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // computeIfAbsent guarantees the supplier is called at most once per key
        assertThat(supplierCallCount.get()).isEqualTo(1);
    }

    @Test
    void clearShouldRemoveAllEntries() {
        cache.update("key1", (StoredGetable) mockStoredGetable());
        cache.update("key2", (StoredGetable) mockStoredGetable());

        assertThat(cache.size()).isEqualTo(2);

        cache.clear();

        assertThat(cache.size()).isEqualTo(0);
    }

    @Test
    void sizeShouldReflectNumberOfCachedEntries() {
        assertThat(cache.size()).isEqualTo(0);

        cache.update("key1", (StoredGetable) mockStoredGetable());
        assertThat(cache.size()).isEqualTo(1);

        cache.update("key2", (StoredGetable) mockStoredGetable());
        assertThat(cache.size()).isEqualTo(2);

        cache.evict("key1");
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void getOrUpdateShouldHandleDifferentKeysIndependently() {
        StoredGetable<?, ?> value1 = mockStoredGetable();
        StoredGetable<?, ?> value2 = mockStoredGetable();

        cache.getOrUpdate("key1", () -> (StoredGetable) value1);
        cache.getOrUpdate("key2", () -> (StoredGetable) value2);

        assertThat(cache.size()).isEqualTo(2);

        // Verify each key returns its own value without invoking the supplier again
        Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> noOpSupplier = mockSupplier(null);
        StoredGetable<?, ?> result1 = cache.getOrUpdate("key1", noOpSupplier);
        StoredGetable<?, ?> result2 = cache.getOrUpdate("key2", noOpSupplier);

        assertThat(result1).isEqualTo(value1);
        assertThat(result2).isEqualTo(value2);
        verify(noOpSupplier, times(0)).get();
    }

    @SuppressWarnings("unchecked")
    private Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> mockSupplier(
            StoredGetable<?, ?> returnValue) {
        Supplier<StoredGetable<? extends Message, ? extends MetadataGetable<?>>> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn((StoredGetable) returnValue);
        return supplier;
    }

    private StoredGetable<?, ?> mockStoredGetable() {
        return mock(StoredGetable.class);
    }
}
