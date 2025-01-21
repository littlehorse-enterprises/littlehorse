package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Slf4j
public class GetableManager extends ReadOnlyGetableManager {

    private final CommandModel command;
    private final TenantScopedStore store;
    private final TagStorageManager tagStorageManager;

    public GetableManager(
            final TenantScopedStore store,
            final ProcessorContext<String, CommandProcessorOutput> ctx,
            final LHServerConfig config,
            final CommandModel command,
            final ExecutionContext executionContext) {
        super(store);
        this.store = store;
        this.command = command;
        this.tagStorageManager = new TagStorageManager(this.store, ctx, config, executionContext);
    }

    /**
     * Puts a Getable in the buffer to be flushed when LHDAO#commit() is called.
     *
     * @param <U>     is the proto type of the getable.
     * @param <T>     is the class of the getable.
     * @param getable is the getable to store.
     * @throws IllegalStateException if a different getable Java Object has been
     *                               put into the store in the same transaction BUT
     *                               with also the same objectId.
     */
    public <U extends Message, T extends CoreGetable<U>> void put(T getable) throws IllegalStateException {

        log.trace("Putting {} with key {}", getable.getClass(), getable.getObjectId());

        @SuppressWarnings("unchecked")
        GetableToStore<U, T> bufferedResult = (GetableToStore<U, T>)
                uncommittedChanges.get(getable.getObjectId().getStoreableKey());

        if (bufferedResult != null) {
            if (bufferedResult.getObjectToStore() != getable) {
                throw new IllegalStateException(
                        "Appears that Getable " + getable.getObjectId() + " was re-instantiated");
            }
            // We know that the buffer already has a pointer to the thing
            // we are storing. We can safely return now.
            return;
        }

        if (getable instanceof ExternalEventModel) {
            WfRunStoredInventoryModel inventory = getOrCreateStoredInventory(
                    ((ExternalEventModel) getable).getId().getWfRunId());
            inventory.getExternalEventIds().add(((ExternalEventModel) getable).getObjectId());
            store.put(inventory);
        }

        // At this point, we know that `getable` has not yet been stored *in this
        // transaction* since it's not in the buffer. So we need to store it.
        //
        // However, the AbstractGetable *may or may not* be in the store. If it is in
        // the
        // store, we need to store the TagsCache in the buffer. Therefore, we still
        // have to call get().

        @SuppressWarnings("unchecked")
        StoredGetable<U, T> previousValue =
                (StoredGetable<U, T>) store.get(getable.getObjectId().getStoreableKey(), StoredGetable.class);

        @SuppressWarnings("unchecked")
        GetableToStore<U, T> toPut = new GetableToStore<>(previousValue, (Class<T>) getable.getClass());

        toPut.setObjectToStore(getable);
        uncommittedChanges.put(getable.getObjectId().getStoreableKey(), toPut);
    }

    private WfRunStoredInventoryModel getOrCreateStoredInventory(WfRunIdModel wfRunId) {
        WfRunStoredInventoryModel result = store.get(wfRunId.getStoreableKey(), WfRunStoredInventoryModel.class);
        if (result == null) {
            result = new WfRunStoredInventoryModel();
            result.setWfRunId(wfRunId);
        }
        return result;
    }

    /**
     * Marks a provided Getable for deletion upon the committing of the
     * "transaction"
     * when we call {@link GetableManager#commit()}.
     *
     * @param <U> is the proto type of the Getable to delete.
     * @param <T> is the java type of the Getable to delete.
     * @param id  is the ObjectId of the Getable to delete.
     * @return the Getable we deleted, if it exists, or null otherwise.
     */
    public <U extends Message, T extends CoreGetable<U>> T delete(CoreObjectId<?, U, T> id) {

        log.trace("Deleting {} with key {}", id.getType(), id.getStoreableKey());
        T thingToDelete = get(id);

        if (thingToDelete == null) {
            return null;
        }

        if (thingToDelete instanceof ExternalEventModel) {
            WfRunStoredInventoryModel inventory = getOrCreateStoredInventory(
                    ((ExternalEventModel) thingToDelete).getId().getWfRunId());
            inventory.getExternalEventIds().remove(((ExternalEventModel) thingToDelete).getObjectId());
            if (inventory.getExternalEventIds().isEmpty()) {
                store.delete(inventory.getStoreKey(), StoreableType.WFRUN_STORED_INVENTORY);
            } else {
                store.put(inventory);
            }
        }

        // Then we need to update the GetableToStore to reflect that we're
        // going to delete it. Also note that since we called get(), we know
        // that the thing is already in the buffer.
        @SuppressWarnings("unchecked")
        GetableToStore<U, T> bufferEntry = (GetableToStore<U, T>) uncommittedChanges.get(id.getStoreableKey());

        if (bufferEntry == null) {
            throw new IllegalStateException("Impossible to get null buffer entry after successfull this#get()");
        }
        // Mark it for deletion
        bufferEntry.setObjectToStore(null);

        return thingToDelete;
    }

    public <U extends Message, T extends CoreGetable<U>> void deleteAllByPrefix(String prefix, Class<T> cls) {
        log.trace("Deleting all {} with prefix {}", cls.getSimpleName(), prefix);

        // Note this iterates in a non-paginated way through all NodeRun's in the
        // WfRun. Fine for most use-cases, but if there's a WfRUn that runs for a
        // year and has hundreds of tasks per day, it will be a problem.
        List<GetableToStore<U, T>> allItems = iterateOverPrefixAndPutInUncommittedChanges(prefix, cls);

        for (GetableToStore<U, T> itemToDelete : allItems) {
            // Marking the objectToStore as null causes the flush() to delete it.
            itemToDelete.setObjectToStore(null);
        }
    }

    public void deleteAllExternalEventsFor(WfRunIdModel wfRunId) {
        log.trace("Deleting all ExternalEvents for WfRun {}", wfRunId);

        WfRunStoredInventoryModel inventory = getOrCreateStoredInventory(wfRunId);
        for (ExternalEventIdModel externalEventId : inventory.getExternalEventIds()) {
            delete(externalEventId);
        }
    }

    public void commit() {
        for (Map.Entry<String, GetableToStore<?, ?>> entry : uncommittedChanges.entrySet()) {
            String storeableKey = entry.getKey();
            GetableToStore<?, ?> entity = entry.getValue();

            if (entity.getObjectToStore() != null) {
                // Actually put it in the key-value store.
                // Note: we know this is a CoreGetable, but no need to cast, so
                // we use AbstractGetable here.
                AbstractGetable<?> getable = entity.getObjectToStore();
                store.put(new StoredGetable<>(getable));
                tagStorageManager.store(getable.getIndexEntries(), entity.getTagsPresentBeforeUpdate());

            } else {
                // Do a deletion!
                store.delete(storeableKey, StoreableType.STORED_GETABLE);
                tagStorageManager.store(List.of(), entity.getTagsPresentBeforeUpdate());
            }
        }

        uncommittedChanges.clear();

        // Note: no need to call uncommittedChanges.clear() because on every
        // Command, we create a completely new GetableStorageManager.
    }
}
