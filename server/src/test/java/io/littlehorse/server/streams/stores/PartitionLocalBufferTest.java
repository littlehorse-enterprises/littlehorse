package io.littlehorse.server.streams.stores;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PartitionLocalBufferTest {

    private PartitionLocalBuffer<FakeStoreable> buffer;

    @BeforeEach
    void setup() {
        buffer = new PartitionLocalBuffer<>();
    }

    @Test
    void shouldStartEmpty() {
        assertThat(buffer.hasEntries()).isFalse();
        assertThat(buffer.values()).isEmpty();
    }

    @Test
    void shouldPutAndGetByStoreKey() {
        FakeStoreable item = new FakeStoreable("key-1", "value-1");
        buffer.put(item);

        assertThat(buffer.get("key-1")).isSameAs(item);
        assertThat(buffer.hasEntries()).isTrue();
    }

    @Test
    void shouldReturnNullForMissingKey() {
        assertThat(buffer.get("nonexistent")).isNull();
    }

    @Test
    void shouldOverwriteExistingItem() {
        buffer.put(new FakeStoreable("key-1", "old"));
        FakeStoreable updated = new FakeStoreable("key-1", "new");
        buffer.put(updated);

        assertThat(buffer.get("key-1").value).isEqualTo("new");
        assertThat(buffer.values()).hasSize(1);
    }

    @Test
    void shouldDrainItemsMatchingPredicate() {
        buffer.put(new FakeStoreable("a", "ready"));
        buffer.put(new FakeStoreable("b", "not-ready"));
        buffer.put(new FakeStoreable("c", "ready"));

        List<FakeStoreable> drained = buffer.drain(item -> item.value.equals("ready"));

        assertThat(drained).hasSize(2);
        assertThat(drained).extracting(s -> s.key).containsExactlyInAnyOrder("a", "c");
        assertThat(buffer.get("a")).isNull();
        assertThat(buffer.get("c")).isNull();
        assertThat(buffer.get("b")).isNotNull();
    }

    @Test
    void shouldReturnEmptyListWhenNothingMatchesPredicate() {
        buffer.put(new FakeStoreable("a", "nope"));

        List<FakeStoreable> drained = buffer.drain(item -> item.value.equals("yes"));

        assertThat(drained).isEmpty();
        assertThat(buffer.hasEntries()).isTrue();
    }

    @Test
    void shouldDrainAllItems() {
        buffer.put(new FakeStoreable("a", "v1"));
        buffer.put(new FakeStoreable("b", "v2"));
        buffer.put(new FakeStoreable("c", "v3"));

        List<FakeStoreable> drained = buffer.drainAll();

        assertThat(drained).hasSize(3);
        assertThat(buffer.hasEntries()).isFalse();
    }

    @Test
    void shouldDrainAllFromEmptyBuffer() {
        List<FakeStoreable> drained = buffer.drainAll();

        assertThat(drained).isEmpty();
    }

    @Test
    void shouldRemoveByKey() {
        buffer.put(new FakeStoreable("key-1", "value"));
        buffer.put(new FakeStoreable("key-2", "value"));

        buffer.remove("key-1");

        assertThat(buffer.get("key-1")).isNull();
        assertThat(buffer.get("key-2")).isNotNull();
    }

    @Test
    void shouldClearAllItems() {
        buffer.put(new FakeStoreable("a", "v1"));
        buffer.put(new FakeStoreable("b", "v2"));

        buffer.clear();

        assertThat(buffer.hasEntries()).isFalse();
        assertThat(buffer.get("a")).isNull();
    }

    @Test
    void shouldStopDrainingWhenPredicateReturnsFalse() {
        buffer.put(new FakeStoreable("a", "ready"));
        buffer.put(new FakeStoreable("b", "ready"));
        buffer.put(new FakeStoreable("c", "ready"));

        int[] count = {0};
        List<FakeStoreable> drained = buffer.drain(item -> {
            if (count[0] >= 1) return false;
            count[0]++;
            return true;
        });

        assertThat(drained).hasSize(1);
        assertThat(buffer.values()).hasSize(2);
    }

    /**
     * Minimal Storeable implementation for testing.
     */
    static class FakeStoreable extends Storeable<Message> {
        final String key;
        final String value;

        FakeStoreable(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getStoreKey() {
            return key;
        }

        @Override
        public StoreableType getType() {
            return StoreableType.TAG;
        }

        @Override
        public void initFrom(Message proto, ExecutionContext context) {}

        @Override
        public GeneratedMessage.Builder<?> toProto() {
            return null;
        }

        @Override
        public Class<GeneratedMessage> getProtoBaseClass() {
            return GeneratedMessage.class;
        }
    }
}
