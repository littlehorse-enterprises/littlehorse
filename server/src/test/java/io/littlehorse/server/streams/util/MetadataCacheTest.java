package io.littlehorse.server.streams.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.protobuf.Message;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.server.streams.store.StoredGetable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetadataCacheTest {

    private MetadataCache cache;

    @BeforeEach
    void setup() {
        cache = new MetadataCache();
    }

    @Test
    void cachesSuppliedValue() {
        var value = mockStoredGetable();
        AtomicInteger loads = new AtomicInteger();

        assertThat(cache.getOrUpdate("key", () -> {
                    loads.incrementAndGet();
                    return value;
                }))
                .isSameAs(value);
        assertThat(cache.getOrUpdate("key", () -> {
                    loads.incrementAndGet();
                    return mockStoredGetable();
                }))
                .isSameAs(value);

        assertThat(loads).hasValue(1);
    }

    @Test
    void cachesNullValue() {
        AtomicInteger loads = new AtomicInteger();

        assertThat(cache.getOrUpdate("key", () -> {
                    loads.incrementAndGet();
                    return null;
                }))
                .isNull();
        assertThat(cache.getOrUpdate("key", () -> {
                    loads.incrementAndGet();
                    return mockStoredGetable();
                }))
                .isNull();

        assertThat(loads).hasValue(1);
    }

    @Test
    void updatesAndEvictsEntries() {
        var first = mockStoredGetable();
        var second = mockStoredGetable();
        cache.update("key", first);

        cache.update("key", second);
        assertThat(cache.getOrUpdate("key", () -> null)).isSameAs(second);

        cache.evict("key");
        assertThat(cache.getOrUpdate("key", () -> first)).isSameAs(first);
    }

    @Test
    void loadsValueOnceDuringConcurrentAccess() throws InterruptedException {
        AtomicInteger loads = new AtomicInteger();
        var value = mockStoredGetable();
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    cache.getOrUpdate("concurrent-key", () -> {
                        loads.incrementAndGet();
                        return value;
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
        executor.shutdownNow();

        assertThat(loads).hasValue(1);
    }

    @Test
    void clearRemovesAllEntries() {
        cache.update("key-1", mockStoredGetable());
        cache.update("key-2", mockStoredGetable());

        assertThat(cache.size()).isEqualTo(2);

        cache.clear();

        assertThat(cache.size()).isZero();
    }

    @Test
    void evictsLeastRecentlyUsedEntryAtCapacity() {
        var first = mockStoredGetable();
        var second = mockStoredGetable();
        cache.update("first", first);
        cache.update("second", second);
        for (int i = 2; i < MetadataCache.MAX_ENTRIES; i++) {
            cache.update("key-" + i, mockStoredGetable());
        }

        assertThat(cache.getOrUpdate("first", () -> null)).isSameAs(first);
        cache.update("overflow", mockStoredGetable());

        assertThat(cache.size()).isEqualTo(MetadataCache.MAX_ENTRIES);
        assertThat(cache.containsKey("first")).isTrue();
        assertThat(cache.containsKey("second")).isFalse();
        assertThat(cache.containsKey("overflow")).isTrue();
    }

    @SuppressWarnings("unchecked")
    private StoredGetable<? extends Message, ? extends MetadataGetable<?>> mockStoredGetable() {
        return mock(StoredGetable.class);
    }
}
