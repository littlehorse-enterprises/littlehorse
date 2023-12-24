package io.littlehorse.server.streams.stores;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.server.streams.store.LHKeyValueIterator;

interface ReadOnlyBaseStore {

    <U extends Message, T extends Storeable<U>> T get(String storeKey, Class<T> cls);

    <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(String key, Class<T> cls);

    <T extends Storeable<?>> LHKeyValueIterator<T> reversePrefixScan(String prefix, Class<T> cls);

    <T extends Storeable<?>> LHKeyValueIterator<T> range(String start, String end, Class<T> cls);
}
