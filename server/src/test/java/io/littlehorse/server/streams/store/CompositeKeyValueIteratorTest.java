package io.littlehorse.server.streams.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.junit.jupiter.api.Test;

class CompositeKeyValueIteratorTest {

    private static KeyValue<String, Bytes> kv(String key) {
        return new KeyValue<>(key, Bytes.wrap(key.getBytes()));
    }

    private static InMemoryKeyValueIterator iterator(String... keys) {
        List<KeyValue<String, Bytes>> entries = new ArrayList<>();
        for (String key : keys) {
            entries.add(kv(key));
        }
        return new InMemoryKeyValueIterator(entries);
    }

    private static List<String> drainKeys(KeyValueIterator<String, Bytes> iterator) {
        List<String> keys = new ArrayList<>();
        while (iterator.hasNext()) {
            keys.add(iterator.next().key);
        }
        return keys;
    }

    @Test
    void shouldIterateSequentiallyAcrossMultipleIterators() {
        CompositeKeyValueIterator composite =
                new CompositeKeyValueIterator(List.of(iterator("a", "b"), iterator("c"), iterator("d", "e")));

        assertThat(drainKeys(composite)).containsExactly("a", "b", "c", "d", "e");
    }

    @Test
    void shouldBehaveAsEmptyWhenGivenNullList() {
        CompositeKeyValueIterator composite = new CompositeKeyValueIterator(null);

        assertThat(composite.hasNext()).isFalse();
        assertThat(composite.peekNextKey()).isNull();
    }

    @Test
    void shouldBehaveAsEmptyWhenGivenEmptyList() {
        CompositeKeyValueIterator composite = new CompositeKeyValueIterator(Collections.emptyList());

        assertThat(composite.hasNext()).isFalse();
        assertThat(composite.peekNextKey()).isNull();
    }

    @Test
    void shouldBehaveAsEmptyWhenAllUnderlyingIteratorsAreEmpty() {
        CompositeKeyValueIterator composite =
                new CompositeKeyValueIterator(List.of(iterator(), iterator(), iterator()));

        assertThat(composite.hasNext()).isFalse();
        assertThat(drainKeys(composite)).isEmpty();
    }

    @Test
    void shouldSkipEmptyIteratorsInBetween() {
        CompositeKeyValueIterator composite = new CompositeKeyValueIterator(
                List.of(iterator(), iterator("a"), iterator(), iterator("b"), iterator()));

        assertThat(drainKeys(composite)).containsExactly("a", "b");
    }

    @Test
    void hasNextShouldBeIdempotentAndNotConsumeElements() {
        CompositeKeyValueIterator composite = new CompositeKeyValueIterator(List.of(iterator("a", "b")));

        assertThat(composite.hasNext()).isTrue();
        assertThat(composite.hasNext()).isTrue();
        assertThat(drainKeys(composite)).containsExactly("a", "b");
    }

    @Test
    void nextShouldThrowWhenExhausted() {
        CompositeKeyValueIterator composite = new CompositeKeyValueIterator(List.of(iterator("a")));

        assertThat(composite.next().key).isEqualTo("a");
        assertThatThrownBy(composite::next).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void nextShouldThrowWhenAllIteratorsAreEmpty() {
        CompositeKeyValueIterator composite = new CompositeKeyValueIterator(List.of(iterator(), iterator()));

        assertThatThrownBy(composite::next).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void peekNextKeyShouldReturnNextKeyWithoutConsuming() {
        CompositeKeyValueIterator composite = new CompositeKeyValueIterator(List.of(iterator("a", "b"), iterator("c")));

        assertThat(composite.peekNextKey()).isEqualTo("a");
        // Peeking again returns the same key.
        assertThat(composite.peekNextKey()).isEqualTo("a");
        assertThat(composite.next().key).isEqualTo("a");

        assertThat(composite.peekNextKey()).isEqualTo("b");
        assertThat(composite.next().key).isEqualTo("b");

        // Crosses into the next underlying iterator.
        assertThat(composite.peekNextKey()).isEqualTo("c");
        assertThat(composite.next().key).isEqualTo("c");

        assertThat(composite.peekNextKey()).isNull();
    }

    @Test
    void peekNextKeyShouldCloseUnderlyingEmptyIterators() {
        InMemoryKeyValueIterator first = iterator();
        InMemoryKeyValueIterator second = iterator();
        InMemoryKeyValueIterator third = iterator("a", "b");
        CompositeKeyValueIterator composite = new CompositeKeyValueIterator(List.of(first, second, third));

        assertThat(composite.peekNextKey()).isEqualTo("a");

        assertThat(first.isClosed()).isTrue();
        assertThat(second.isClosed()).isTrue();
        assertThat(third.isClosed()).isFalse();
        composite.close();
        assertThat(third.isClosed()).isTrue();
    }

    @Test
    void peekNextKeyShouldSkipEmptyIterators() {
        CompositeKeyValueIterator composite =
                new CompositeKeyValueIterator(List.of(iterator(), iterator(), iterator("a")));

        assertThat(composite.peekNextKey()).isEqualTo("a");
    }

    @Test
    void closeShouldCloseAllUnderlyingIterators() {
        InMemoryKeyValueIterator first = iterator("a", "b");
        InMemoryKeyValueIterator second = iterator("c");
        InMemoryKeyValueIterator third = iterator("d");

        CompositeKeyValueIterator composite = new CompositeKeyValueIterator(List.of(first, second, third));
        drainKeys(composite);

        composite.close();

        assertThat(first.isClosed()).isTrue();
        assertThat(second.isClosed()).isTrue();
        assertThat(third.isClosed()).isTrue();
    }

    @Test
    void closeShouldCloseRemainingIteratorsEvenAfterPartialConsumption() {
        InMemoryKeyValueIterator first = iterator("a");
        InMemoryKeyValueIterator second = iterator("b");
        InMemoryKeyValueIterator third = iterator("c");

        CompositeKeyValueIterator composite = new CompositeKeyValueIterator(List.of(first, second, third));

        // Consume only the two first elements;
        assertThat(composite.next().key).isEqualTo("a");
        assertThat(composite.next().key).isEqualTo("b");

        composite.close();

        assertThat(first.isClosed()).isTrue();
        assertThat(second.isClosed()).isTrue();
        assertThat(third.isClosed()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void closeShouldIgnoreExceptionsFromRemainingIterators() {
        InMemoryKeyValueIterator first = iterator("a");
        KeyValueIterator<String, Bytes> failing = mock(KeyValueIterator.class);
        InMemoryKeyValueIterator last = iterator("b");

        doThrow(new RuntimeException("boom")).when(failing).close();

        CompositeKeyValueIterator composite = new CompositeKeyValueIterator(List.of(first, failing, last));

        composite.close();

        assertThat(first.isClosed()).isTrue();
        verify(failing, times(1)).close();
        assertThat(last.isClosed()).isTrue();
    }
}
