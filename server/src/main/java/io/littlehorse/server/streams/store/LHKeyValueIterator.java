package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Iterator;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;

public class LHKeyValueIterator<T extends Storeable<?>> implements Iterator<LHIterKeyValue<T>>, AutoCloseable {

    private KeyValueIterator<Bytes, byte[]> rawIter;
    private Class<T> cls;
    private ExecutionContext executionContext;

    public LHKeyValueIterator(
            KeyValueIterator<Bytes, byte[]> rawIter, Class<T> cls, ExecutionContext executionContext) {
        this.cls = cls;
        this.rawIter = rawIter;
        this.executionContext = executionContext;
    }

    @Override
    public boolean hasNext() {
        return rawIter.hasNext();
    }

    @Override
    public LHIterKeyValue<T> next() {
        KeyValue<Bytes, byte[]> next = rawIter.next();
        return new LHIterKeyValue<>(new KeyValue<>(next.key.toString(), Bytes.wrap(next.value)), cls, executionContext);
    }

    public void close() {
        rawIter.close();
    }
}
