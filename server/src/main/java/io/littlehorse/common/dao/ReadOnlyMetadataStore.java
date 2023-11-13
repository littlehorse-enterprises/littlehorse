package io.littlehorse.common.dao;

import io.littlehorse.server.streams.store.ReadOnlyModelStore;
import io.littlehorse.server.streams.util.MetadataCache;

/*
 * There is no cacheing implemented in this store. All cacheing is the responsibility
 * of the user of this store; for example, the CoreProcessorDAOImpl should do cacheing.
 */
public class ReadOnlyMetadataStore {

    private final ReadOnlyModelStore rocksdb;

    private final MetadataCache metadataCache;

    public ReadOnlyMetadataStore(ReadOnlyModelStore rocksdb, MetadataCache metadataCache) {
        this.rocksdb = rocksdb;
        this.metadataCache = metadataCache;
    }
}
