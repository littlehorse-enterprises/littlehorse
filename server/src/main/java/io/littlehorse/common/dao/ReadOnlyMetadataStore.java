package io.littlehorse.common.dao;

import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.server.streams.store.ReadOnlyRocksDBWrapper;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.function.Supplier;

/*
 * There is no cacheing implemented in this store. All cacheing is the responsibility
 * of the user of this store; for example, the CoreProcessorDAOImpl should do cacheing.
 */
public class ReadOnlyMetadataStore {

    private final ReadOnlyRocksDBWrapper rocksdb;

    private final MetadataCache wfSpecCache;

    public ReadOnlyMetadataStore(ReadOnlyRocksDBWrapper rocksdb, MetadataCache wfSpecCache) {
        this.rocksdb = rocksdb;
        this.wfSpecCache = wfSpecCache;
    }

    @SuppressWarnings("unchecked")
    public WfSpecModel getWfSpec(String name, Integer version) {
        Supplier<WfSpecModel> findWfSpec = () -> {
            final StoredGetable<WfSpec, WfSpecModel> storedResult;
            if (version != null) {
                storedResult = (StoredGetable<WfSpec, WfSpecModel>)
                        rocksdb.get(new WfSpecIdModel(name, version).getStoreableKey(), StoredGetable.class);
            } else {
                storedResult = rocksdb.getLastFromPrefix(WfSpecIdModel.getPrefix(name), StoredGetable.class);
            }
            return storedResult == null ? null : storedResult.getStoredObject();
        };
        return wfSpecCache.getOrCache(name, version, findWfSpec);
    }

    public TaskDefModel getTaskDef(String name) {
        @SuppressWarnings("unchecked")
        StoredGetable<TaskDef, TaskDefModel> storedResult = (StoredGetable<TaskDef, TaskDefModel>)
                rocksdb.get(new TaskDefIdModel(name).getStoreableKey(), StoredGetable.class);

        return storedResult == null ? null : storedResult.getStoredObject();
    }

    public ExternalEventDefModel getExternalEventDef(String name) {
        @SuppressWarnings("unchecked")
        StoredGetable<ExternalEventDef, ExternalEventDefModel> storedResult =
                (StoredGetable<ExternalEventDef, ExternalEventDefModel>)
                        rocksdb.get(new ExternalEventDefIdModel(name).getStoreableKey(), StoredGetable.class);

        return storedResult == null ? null : storedResult.getStoredObject();
    }

    @SuppressWarnings("unchecked")
    public UserTaskDefModel getUserTaskDef(String name, Integer version) {
        StoredGetable<UserTaskDef, UserTaskDefModel> storedResult;
        if (version != null) {
            UserTaskDefIdModel id = new UserTaskDefIdModel(name, version);
            storedResult = (StoredGetable<UserTaskDef, UserTaskDefModel>)
                    rocksdb.get(id.getStoreableKey(), StoredGetable.class);
        } else {
            storedResult = rocksdb.getLastFromPrefix(UserTaskDefIdModel.getPrefix(name), StoredGetable.class);
        }

        return storedResult == null ? null : storedResult.getStoredObject();
    }
}
