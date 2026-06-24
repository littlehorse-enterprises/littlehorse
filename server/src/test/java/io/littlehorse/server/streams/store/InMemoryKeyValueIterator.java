package io.littlehorse.server.streams.store;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;

/**
 * A simple in-memory {@link KeyValueIterator} backed by a list of entries. Intended for tests
 * that need a real {@link KeyValueIterator} instead of a mock. It supports {@link #peekNextKey()}
 * and tracks whether {@link #close()} has been called.
 */
public class InMemoryKeyValueIterator implements KeyValueIterator<String, Bytes> {

    private final List<KeyValue<String, Bytes>> entries;
    private int index = 0;
    private boolean closed = false;

    public InMemoryKeyValueIterator(List<KeyValue<String, Bytes>> entries) {
        this.entries = new ArrayList<>(entries);
    }

    @Override
    public boolean hasNext() {
        return index < entries.size();
    }

    @Override
    public KeyValue<String, Bytes> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return entries.get(index++);
    }

    @Override
    public String peekNextKey() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return entries.get(index).key;
    }

    @Override
    public void close() {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }
}
