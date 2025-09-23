package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.CoreOutputTopicGetable;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.metadatacommand.OutputTopicConfigModel;
import io.littlehorse.common.model.outputtopic.OutputTopicRecordModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Slf4j
public class GetableManager extends ReadOnlyGetableManager {

    private final CommandModel command;
    private final TenantScopedStore store;
    private final TagStorageManager tagStorageManager;
    private final CoreProcessorContext ctx;
    private final OutputTopicConfigModel outputTopicConfig;

    public GetableManager(
            final TenantScopedStore store,
            final ProcessorContext<String, CommandProcessorOutput> streamsContext,
            final LHServerConfig config,
            final CommandModel command,
            final CoreProcessorContext executionContext,
            final OutputTopicConfigModel outputTopicConfig) {
        super(store);
        this.store = store;
        this.command = command;
        this.tagStorageManager = new TagStorageManager(this.store, streamsContext, config, executionContext);
        this.ctx = executionContext;
        this.outputTopicConfig = outputTopicConfig;
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

            ExternalEventIdModel evtId = ((ExternalEventModel) getable).getObjectId();

            // Only add it if it doesn't already exist
            if (!inventory.getExternalEventIds().stream().anyMatch(candidate -> {
                return evtId.equals(candidate);
            })) {
                inventory.getExternalEventIds().add(((ExternalEventModel) getable).getObjectId());
            }
            store.put(inventory);
        }

        // At this point, we know that `getable` has not yet been stored *in this
        // transaction* since it's not in the buffer. So we need to store it.
        //
        // However, the AbstractGetable *may or may not* be in the store. If it is in
        // the
        // store, we need to store the TagsCache in the buffer. Therefore, we still
        // have to call get().
        boolean alreadyExists =
                uncommittedChanges.containsKey(getable.getObjectId().getStoreableKey());

        GetableToStore<U, T> toPut;
        if (alreadyExists) {
            @SuppressWarnings("unchecked")
            StoredGetable<U, T> previousValue =
                    (StoredGetable<U, T>) store.get(getable.getObjectId().getStoreableKey(), StoredGetable.class);
            toPut = new GetableToStore<>(getable, previousValue, (Class<T>) getable.getClass());
        } else {
            toPut = new GetableToStore<>(getable, (StoredGetable<U, T>) null, (Class<T>) getable.getClass());
        }
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
        uncommittedChanges.put(
                id.getStoreableKey(),
                GetableToStore.deletion(bufferEntry.getCls(), bufferEntry.getTagsPresentBeforeUpdate()));

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
            uncommittedChanges.put(
                    itemToDelete.getObjectToStore().getObjectId().getStoreableKey(),
                    GetableToStore.deletion(itemToDelete.getCls(), itemToDelete.getTagsPresentBeforeUpdate()));
        }
    }

    public void deleteAllExternalEventsFor(WfRunIdModel wfRunId) {
        log.trace("Deleting all ExternalEvents for WfRun {}", wfRunId);

        WfRunStoredInventoryModel inventory = getOrCreateStoredInventory(wfRunId);
        for (ExternalEventIdModel externalEventId : inventory.getExternalEventIds()) {
            delete(externalEventId);
        }
    }

    private <U extends Message, T extends CoreGetable<U>> Optional<OutputTopicRecordModel> processEntity(
            String storeableKey, GetableToStore<U, T> entity) {
        if (entity.isDeletion()) {
            // Do a deletion!
            store.delete(storeableKey, StoreableType.STORED_GETABLE);
            tagStorageManager.store(List.of(), entity.getTagsPresentBeforeUpdate());
        } else if (entity.containsUpdate()) {
            T getable = entity.getObjectToStore();
            store.put(new StoredGetable<>(getable));
            tagStorageManager.store(getable.getIndexEntries(), entity.getTagsPresentBeforeUpdate());

            if (outputTopicConfig != null && getable instanceof CoreOutputTopicGetable) {
                CoreOutputTopicGetable<U> outputTopicCandidate = (CoreOutputTopicGetable<U>) getable;
                U previouslyStoredProto = entity.getPreviouslyStoredProto();

                if (outputTopicCandidate.shouldProduceToOutputTopic(
                        previouslyStoredProto, ctx.metadataManager(), this, outputTopicConfig)) {
                    return Optional.of(new OutputTopicRecordModel(outputTopicCandidate, command.getTime()));
                }
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public void commit() {
        List<OutputTopicRecordModel> outputTopicRecords = new ArrayList<>();

        for (Map.Entry<String, GetableToStore<?, ?>> entry : uncommittedChanges.entrySet()) {
            String storeableKey = entry.getKey();
            GetableToStore entity = entry.getValue();
            Optional<OutputTopicRecordModel> newRecord = processEntity(storeableKey, entity);
            if (newRecord.isPresent()) {
                outputTopicRecords.add(newRecord.get());
            }
        }

        // For any WfRun record, if the status is running, we want it to be the first
        // in the list. Otherwise, we want the WfRun to be the last in the list.
        outputTopicRecords.sort((o1, o2) -> {
            CoreGetable<?> getable1 = o1.getSubrecord();
            CoreGetable<?> getable2 = o2.getSubrecord();
            if (getable1 instanceof WfRunModel) {
                WfRunModel wfRun1 = (WfRunModel) getable1;
                return wfRun1.getStatus() == LHStatus.RUNNING ? -1 : 1;
            } else if (getable2 instanceof WfRunModel) {
                WfRunModel wfRun2 = (WfRunModel) getable2;
                return wfRun2.getStatus() == LHStatus.RUNNING ? 1 : -1;
            }
            return 0;
        });

        for (OutputTopicRecordModel record : outputTopicRecords) {
            ctx.forward(record);
        }

        uncommittedChanges.clear();

        // Note: no need to call uncommittedChanges.clear() because on every
        // Command, we create a completely new GetableStorageManager.
    }
}
