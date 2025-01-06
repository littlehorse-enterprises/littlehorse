package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.TaskId;

@Slf4j
public class ReadOnlyGetableManager {

    protected final Map<String, GetableToStore<?, ?>> uncommittedChanges = new TreeMap<>();
    private final ReadOnlyTenantScopedStore store;
    private final TaskId specificTask;

    public ReadOnlyGetableManager(ReadOnlyTenantScopedStore store) {
        this.store = store;
        this.specificTask = null;
    }

    public ReadOnlyGetableManager(ReadOnlyTenantScopedStore store, TaskId specificTask) {
        this.store = store;
        this.specificTask = specificTask;
    }

    /**
     * Gets a getable with a provided ID from the store (within a transaction). Note
     * that if you make any modifications to the Java object returned by this
     * method,
     * then those changes will be persisted in the state store when the
     * "transaction"
     * is committed, i.e. when we call {@link LHDAO#commitChanges()}.
     *
     * @param <U> is the proto type of the AbstractGetable.
     * @param <T> is the java class type of the AbstractGetable.
     * @param id  is the ObjectId to look for.
     * @return the specified AbstractGetable, or null if it doesn't exist.
     */
    public <U extends Message, T extends CoreGetable<U>> T get(CoreObjectId<?, U, T> id) {
        log.trace("Getting {} with key {}", id.getType(), id);
        T out = null;

        // First check the cache.
        @SuppressWarnings("unchecked")
        GetableToStore<U, T> bufferedResult = (GetableToStore<U, T>) uncommittedChanges.get(id.getStoreableKey());
        if (bufferedResult != null) {
            return bufferedResult.getObjectToStore();
        }

        // Next check the store.
        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storeResult = (StoredGetable<U, T>) store.get(id.getStoreableKey(), StoredGetable.class);

        if (storeResult == null) return null;

        // If we got here, that means that:
        // 1. The Getable exists in the store, and
        // 2. This is the first time in this txn (eg. Command Processing) that
        // we are getting the
        out = storeResult.getStoredObject();

        uncommittedChanges.put(id.getStoreableKey(), new GetableToStore<>(storeResult, id.getObjectClass()));
        return out;
    }

    public <U extends Message, T extends CoreGetable<U>, EX extends Throwable> T getOrThrow(
            CoreObjectId<?, U, T> id, Supplier<EX> throwable) throws EX {
        T result = get(id);
        if (result == null) {
            throw throwable.get();
        }
        return result;
    }

    // Note that this is an expensive operation. It's used when deleting a WfRun.
    protected <U extends Message, T extends CoreGetable<U>>
            List<GetableToStore<U, T>> iterateOverPrefixAndPutInUncommittedChanges(String prefix, Class<T> cls) {

        List<GetableToStore<U, T>> out = iterateOverPrefixInternal(prefix, cls);

        // put everything in the buffer.
        for (GetableToStore<U, T> thing : out) {
            uncommittedChanges.put(thing.getObjectToStore().getObjectId().getStoreableKey(), thing);
        }

        return out;
    }

    @Deprecated(forRemoval = true)
    public <T extends StoredGetable<?, ?>> LHKeyValueIterator<T> range(String start, String end, Class<T> cls) {
        return store.range(start, end, cls);
    }

    /**
     * Iterate over tags
     */
    public LHKeyValueIterator<Tag> tagScan(String start, String end) {
        return store.range(start, end, Tag.class);
    }

    /**
     * Only use this method if you need to retrieve a Storeable
     * @param storeKey Storeable's storekey
     * @param cls Storeable's class
     * @return Null if not found
     */
    public ScheduledTaskModel getScheduledTask(TaskRunIdModel scheduledTaskId) {
        return store.get(scheduledTaskId.toString(), ScheduledTaskModel.class);
    }
    /**
     * Only use this method if you need to retrieve a Storeable
     * @return Null if not found
     */
    public ScheduledTaskModel getScheduledTask(String storeKey) {
        return store.get(storeKey, ScheduledTaskModel.class);
    }

    public ExternalEventModel getUnclaimedEvent(WfRunIdModel wfRunId, ExternalEventDefIdModel externalEventDefId) {
        // We want to find the first Event that has the following characteristics:
        // - matches the wfRunId
        // - matches the externalEventDefName
        // - isClaimed == false
        // - is first by creation timestamp.
        //
        // The way to do that is by using a TagScan over: (wfRunId, extEvtDefName, isClaimed)
        //
        // However, we also need to take into consideration the fact that there may be
        // ExternalEvents inside our buffer that fit the representation too. So we need
        // to:
        // 1. Get the first (if any) from the Tag Scan
        // 2. Get the first (if any) from the buffer (getablesToStore())
        // 3. Return whichever of the first two was created earlier.

        ExternalEventModel earliestFromTags = null;
        ExternalEventModel earliestFromGetablesToStore = null;
        // Tag Scan
        List<Attribute> attributes = List.of(
                new Attribute("wfRunId", wfRunId.toString()),
                new Attribute("extEvtDefName", externalEventDefId.toString()),
                new Attribute("isClaimed", "false"));

        String prefixToScan = Tag.getAttributeString(GetableClassEnum.EXTERNAL_EVENT, attributes) + "/";

        try (LHKeyValueIterator<Tag> iterator = store.prefixScan(prefixToScan, Tag.class)) {
            if (iterator.hasNext()) {
                LHIterKeyValue<Tag> next = iterator.next();
                Tag tag = next.getValue();
                ExternalEventIdModel externalEventId = (ExternalEventIdModel)
                        ObjectIdModel.fromString(tag.getDescribedObjectId(), ExternalEventIdModel.class);
                earliestFromTags = get(externalEventId);
            }
        }

        // Check the buffer
        // Overwrite what's in the store with what's in the buffer.
        for (GetableToStore<?, ?> getableInBuffer : uncommittedChanges.values()) {
            if (getableInBuffer.getObjectToStore() == null
                    || getableInBuffer.getObjectType() != GetableClassEnum.EXTERNAL_EVENT) {
                continue;
            }
            ExternalEventModel candidate = (ExternalEventModel) getableInBuffer.getObjectToStore();
            if (!candidate.getId().getWfRunId().equals(wfRunId)
                    || !candidate.getId().getExternalEventDefId().equals(externalEventDefId)
                    || candidate.isClaimed()) {
                continue;
            }
            if (earliestFromGetablesToStore == null
                    || candidate.getCreatedAt().before(earliestFromGetablesToStore.getCreatedAt())) {
                earliestFromGetablesToStore = candidate;
            }
        }

        if (earliestFromTags != null && earliestFromGetablesToStore != null) {
            return earliestFromTags.getCreatedAt().compareTo(earliestFromGetablesToStore.getCreatedAt()) < 0
                    ? earliestFromTags
                    : earliestFromGetablesToStore;
        } else if (earliestFromTags != null) {
            return earliestFromTags;
        } else {
            return earliestFromGetablesToStore;
        }
    }

    // Note that this is an expensive operation. It's used by External Event Nodes.
    @SuppressWarnings("unchecked")
    private <U extends Message, T extends CoreGetable<U>> List<GetableToStore<U, T>> iterateOverPrefixInternal(
            String prefix, Class<T> cls) {
        Map<String, GetableToStore<U, T>> all = new HashMap<>();

        // First iterate over what's in the store.
        String storePrefix = StoredGetable.getRocksDBKey(prefix, AbstractGetable.getTypeEnum(cls));

        try (LHKeyValueIterator<?> iterator = store.range(storePrefix, storePrefix + "~", StoredGetable.class)) {

            while (iterator.hasNext()) {
                LHIterKeyValue<? extends Storeable<?>> next = iterator.next();

                StoredGetable<U, T> item = (StoredGetable<U, T>) next.getValue();
                all.put(item.getStoreKey(), new GetableToStore<>(item, cls));
            }
        }

        // Overwrite what's in the store with what's in the buffer.
        for (Map.Entry<String, GetableToStore<?, ?>> entry : uncommittedChanges.entrySet()) {
            if (entry.getKey().startsWith(storePrefix)) {
                all.put(entry.getKey(), (GetableToStore<U, T>) entry.getValue());
            }
        }

        return all.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .toList();
    }

    private <U extends Message, T extends CoreGetable<U>> List<T> iterateOverPrefix(String prefix, Class<T> cls) {
        return iterateOverPrefixInternal(prefix, cls).stream()
                .map(getableToStore -> getableToStore.getObjectToStore())
                .toList();
    }

    public List<WorkflowEventModel> getWorkflowEvents(WfRunIdModel wfRunId, WorkflowEventDefIdModel eventDefId) {
        String startKey = LHUtil.getCompositeId(wfRunId.toString(), eventDefId.toString());
        return iterateOverPrefix(startKey, WorkflowEventModel.class);
    }

    public List<WorkflowEventModel> getWorkflowEvents(WfRunIdModel wfRunId) {
        return iterateOverPrefix(wfRunId.toString() + "/", WorkflowEventModel.class);
    }

    public Optional<TaskId> getSpecificTask() {
        return Optional.ofNullable(specificTask);
    }
}
