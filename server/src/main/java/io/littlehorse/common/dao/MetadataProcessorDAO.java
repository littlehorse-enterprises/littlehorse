package io.littlehorse.common.dao;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.server.streams.store.LHStore;
import io.littlehorse.server.streams.util.MetadataCache;

public abstract class MetadataProcessorDAO extends ReadOnlyMetadataStore {

    public MetadataProcessorDAO(LHStore rocksdb, MetadataCache metadataCache) {
        super(rocksdb, metadataCache);
    }

    /*
     * Lifecycle for processing a Command
     */

    public abstract void initCommand(MetadataCommandModel command);

    public abstract MetadataCommandModel getCommand();

    /*
     * Read/Write processing.
     */

    public abstract <U extends Message, T extends GlobalGetable<U>> T get(ObjectIdModel<?, U, T> id);

    public abstract <U extends Message, T extends GlobalGetable<U>> void put(T getable);

    public abstract <U extends Message, T extends GlobalGetable<U>> void delete(ObjectIdModel<?, U, T> id);
}
