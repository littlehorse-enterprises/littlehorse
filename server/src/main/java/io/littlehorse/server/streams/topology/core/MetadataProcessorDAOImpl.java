package io.littlehorse.server.streams.topology.core;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.AuthorizationContext;
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
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class MetadataProcessorDAOImpl extends ReadOnlyMetadataDAOImpl implements MetadataProcessorDAO {

    private MetadataCommandModel command;
    private final ModelStore lhStore;

    /*
     * TODO Eduwer/Mateo: Why was the metadataCache not used?
     */
    public MetadataProcessorDAOImpl(ModelStore lhStore, MetadataCache metadataCache, AuthorizationContext context) {
        super(lhStore, metadataCache, context);
        this.lhStore = lhStore;
        // this.metadataCache = metadataCache;
    }

    @Override
    public void initCommand(MetadataCommandModel command) {
        this.command = command;
    }

    @Override
    public MetadataCommandModel getCommand() {
        return command;
    }

    // TODO: we should re-use some of the infrastrucutre in the CoreProcessorDAO for putting
    // and deleting Tags.
    public <U extends Message, T extends GlobalGetable<U>> void put(T getable) {

        // The cast is necessary to tell the store that the ObjectId belongs to a
        // GlobalGetable.
        @SuppressWarnings("unchecked")
        StoredGetable<?, ?> old = lhStore.get((ObjectIdModel<?, U, T>) getable.getObjectId());
        if (old != null) {
            log.trace("removing tags for metadata getable {}", getable.getObjectId());
            for (String tagId : old.getIndexCache().getTagIds()) {
                lhStore.delete(tagId, StoreableType.TAG);
            }
        }

        StoredGetable<U, T> toStore = new StoredGetable<U, T>(getable);
        lhStore.put(toStore);
        for (Tag tag : getable.getIndexEntries()) {
            lhStore.put(tag);
        }
    }

    public <U extends Message, T extends GlobalGetable<U>> void delete(ObjectIdModel<?, U, T> id) {
        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storeResult = lhStore.get(id.getStoreableKey(), StoredGetable.class);
        log.trace("trying to delete " + id.getStoreableKey());

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
