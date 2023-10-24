package io.littlehorse.server.streams.topology.core;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.ServerContext;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.util.MetadataCache;

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
public class MetadataProcessorDAOImpl extends ReadOnlyMetadataProcessorDAOImpl implements MetadataProcessorDAO {

    private MetadataCommandModel command;
    private final ModelStore lhStore;
    private final MetadataCache metadataCache;

    public MetadataProcessorDAOImpl(ModelStore lhStore, MetadataCache metadataCache, ServerContext context) {
        super(lhStore, metadataCache, context);
        this.lhStore = lhStore;
        this.metadataCache = metadataCache;
    }

    @Override
    public void initCommand(MetadataCommandModel command) {
        this.command = command;
    }

    @Override
    public MetadataCommandModel getCommand() {
        return command;
    }

    // Note that as of now, modifying a GlobalGetable is not supported. That may
    // change when we introduce the `Principal` and `Tenant` GlobalGetable's. At
    // that time, we will extend this method.
    public <U extends Message, T extends GlobalGetable<U>> void put(T getable) {

        // The cast is necessary to tell the store that the ObjectId belongs to a
        // GlobalGetable.
        @SuppressWarnings("unchecked")
        /*AbstractGetable<?> old = get((ObjectIdModel<?, U, T>) getable.getObjectId());

        if (old != null) {
            throw new IllegalStateException(
                    "As of now, metadata processor does not support editing values. Coming in future.");
        }*/

        StoredGetable<U, T> toStore = new StoredGetable<U, T>(getable);
        lhStore.put(toStore);
        for (Tag tag : getable.getIndexEntries()) {
            lhStore.put(tag);
        }
    }

    public <U extends Message, T extends GlobalGetable<U>> void delete(ObjectIdModel<?, U, T> id) {
        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storeResult = lhStore.get(id.getStoreableKey(), StoredGetable.class);

        if (storeResult == null) {
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "Couldn't find provided " + id.getObjectClass().getSimpleName());
        }

        lhStore.delete(id.getStoreableKey(), StoreableType.STORED_GETABLE);

        // Now delete all the tags
        for (String tagId : storeResult.getIndexCache().getTagIds()) {
            lhStore.delete(tagId, StoreableType.TAG);
        }
    }
}
