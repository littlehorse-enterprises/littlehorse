package io.littlehorse.server.streams.stores;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.PartitionCountedTagModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/**
 * Guards the single-thread ownership invariant that makes the metrics drain correct.
 *
 * <p>The buffer is a write-through cache over the core store: command processing reads the buffer
 * and falls back to the store on a miss, while the drain removes from the buffer and deletes from
 * the store as two separate steps. Those compose correctly only because both run on the Kafka
 * Streams thread. If someone ever moves the drain onto the per-partition background worker, these
 * tests are what should fail first — instead of counts silently going wrong in production.
 */
public class PartitionLocalBufferOwnershipTest {

    private final TenantIdModel tenantId = new TenantIdModel(LHConstants.DEFAULT_TENANT);

    @Test
    void shouldAllowRepeatedAccessFromTheOwningThread() {
        PartitionLocalBuffer<PartitionCountedTagModel> buffer = new PartitionLocalBuffer<>();

        buffer.put(tag("attr-a"));
        buffer.put(tag("attr-b"));

        assertThat(buffer.hasEntries()).isTrue();
        assertThat(buffer.drainAll()).hasSize(2);
    }

    @Test
    void shouldRejectAccessFromAnotherThread() throws Exception {
        PartitionLocalBuffer<PartitionCountedTagModel> buffer = new PartitionLocalBuffer<>();
        // The first thread to touch the buffer claims ownership.
        buffer.put(tag("attr-a"));

        assertThat(runOnOtherThread(() -> buffer.put(tag("attr-b"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must only be")
                .hasMessageContaining("Kafka Streams thread");
    }

    @Test
    void shouldRejectDrainingFromAnotherThread() throws Exception {
        PartitionLocalBuffer<PartitionCountedTagModel> buffer = new PartitionLocalBuffer<>();
        buffer.put(tag("attr-a"));

        // This is precisely the migration that would corrupt counts.
        assertThat(runOnOtherThread(buffer::drainAll)).isInstanceOf(IllegalStateException.class);
    }

    /**
     * Also proves assertions are enabled under the Gradle test task; without {@code -ea} the
     * ownership check compiles out and these tests would see no failure at all.
     */
    @Test
    void assertionsMustBeEnabledForThisGuardToDoAnything() {
        boolean enabled = false;
        assert enabled = true;
        assertThat(enabled).isTrue();
    }

    private Throwable runOnOtherThread(Runnable action) throws InterruptedException {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread other = new Thread(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                thrown.set(t);
            }
        });
        other.start();
        other.join();
        return thrown.get();
    }

    private PartitionCountedTagModel tag(String attributeString) {
        return new PartitionCountedTagModel(tenantId, attributeString);
    }
}
