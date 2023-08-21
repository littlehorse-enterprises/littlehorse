package io.littlehorse.server.streams.topology.core;

import com.google.protobuf.Message;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.server.streams.store.RocksDBWrapper;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Tag;

/*
 * Note that this class is a MUCH SIMPLER version of the CoreProcessorDAO. This class
 * may become more complex in the future, but for now there are a few reasons why
 * we can get away with the extra simplicity:
 *
 * 1. As of now, there are no MetadataCommands that modify more than one GlobalGetable.
 * 2. The throughput of the Metadata Processor is so low that cacheing is not necessary.
 * 3. All Tag's are LOCAL, since there is only one partition.
 *
 * Therefore, we just do everything manually in this class. It's much simpler that
 * way.
 */
public class MetadataProcessorDAOImpl implements MetadataProcessorDAO {

    private RocksDBWrapper rocksdb;
    private MetadataCommandModel command;

    public MetadataProcessorDAOImpl(RocksDBWrapper rocksdb) {
        this.rocksdb = rocksdb;
    }

    @Override
    public void initCommand(MetadataCommandModel command) {
        this.command = command;
    }

    @Override
    public MetadataCommandModel getCommand() {
        return command;
    }

    public <U extends Message, T extends GlobalGetable<U>> T get(ObjectIdModel<?, U, T> id) {
        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storeResult = rocksdb.get(id.getStoreKey(), StoredGetable.class);

        if (storeResult == null) {
            return null;
        }
        return storeResult.getStoredObject();
    }

    // Note that as of now, modifying a GlobalGetable is not supported. That may
    // change when we introduce the `Principal` and `Tenant` GlobalGetable's. At
    // that time, we will extend this method.
    public <U extends Message, T extends GlobalGetable<U>> void put(T getable) {

        // The cast is necessary to tell the store that the ObjectId belongs to a
        // GlobalGetable.
        @SuppressWarnings("unchecked")
        GlobalGetable<?> old = get((ObjectIdModel<?, U, T>) getable.getObjectId());

        if (old == null) {
            throw new IllegalStateException(
                    "As of now, metadata processor does not support editing values. Coming in future.");
        }

        StoredGetable<U, T> toStore = new StoredGetable<U, T>(getable);
        rocksdb.put(toStore);
        for (Tag tag : getable.getIndexEntries()) {
            rocksdb.put(tag);
        }
    }

    public <U extends Message, T extends GlobalGetable<U>> DeleteObjectReply delete(ObjectIdModel<?, U, T> id) {
        return null;
    }
}
