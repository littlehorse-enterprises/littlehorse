package io.littlehorse.server.streams.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;

/**
 * A simple in-memory {@link KeyValueIterator} backed by a list of entries. Intended for tests
 * that need a real {@link LHKeyValueIterator} instead of a mock.
 */
public class InMemoryKeyValueIterator implements KeyValueIterator<String, Bytes> {

    private final Iterator<KeyValue<String, Bytes>> delegate;

    public InMemoryKeyValueIterator(List<KeyValue<String, Bytes>> entries) {
        this.delegate = new ArrayList<>(entries).iterator();
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public KeyValue<String, Bytes> next() {
        return delegate.next();
    }

    @Override
    public String peekNextKey() {
        throw new UnsupportedOperationException("peekNextKey is not supported");
    }

    @Override
    public void close() {
        // no-op
    }
}
