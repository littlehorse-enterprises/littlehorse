package io.littlehorse.server.streams.store;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;

/**
 * Composite KeyValueIterator that sequentially iterates through multiple underlying iterators.
 * Delegates close() to all of them.
 */
public class CompositeKeyValueIterator implements KeyValueIterator<String, Bytes> {

    private final Iterator<KeyValueIterator<String, Bytes>> iteratorList;
    private KeyValueIterator<String, Bytes> current;

    public CompositeKeyValueIterator(List<KeyValueIterator<String, Bytes>> iterators) {
        if (iterators == null || iterators.isEmpty()) {
            this.iteratorList = List.<KeyValueIterator<String, Bytes>>of().iterator();
            this.current = null;
        } else {
            this.iteratorList = iterators.iterator();
            this.current = this.iteratorList.next();
        }
    }

    @Override
    public boolean hasNext() {
        while (current != null) {
            if (current.hasNext()) return true;
            if (iteratorList.hasNext()) {
                current = iteratorList.next();
            } else {
                current = null;
            }
        }
        return false;
    }

    @Override
    public KeyValue<String, Bytes> next() {
        if (!hasNext()) throw new NoSuchElementException();
        return current.next();
    }

    @Override
    public void close() {
        if (current != null) current.close();
        iteratorList.forEachRemaining(it -> {
            try {
                it.close();
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public String peekNextKey() {
        while (current != null) {
            if (current.hasNext()) return current.peekNextKey();
            if (iteratorList.hasNext()) {
                current = iteratorList.next();
            } else {
                current = null;
            }
        }
        return null;
    }
}
