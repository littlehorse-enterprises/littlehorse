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
import io.littlehorse.server.streams.store.RocksDBWrapper;
import io.littlehorse.server.streams.store.StoredGetable;

/*
 * There is no cacheing implemented in this store. All cacheing is the responsibility
 * of the user of this store; for example, the CoreProcessorDAOImpl should do cacheing.
 */
public class ReadOnlyMetadataStore {

    private ReadOnlyRocksDBWrapper rocksdb;

    public ReadOnlyMetadataStore(RocksDBWrapper rocksdb) {
        this.rocksdb = rocksdb;
    }

    @SuppressWarnings("unchecked")
    public WfSpecModel getWfSpec(String name, Integer version) {
        StoredGetable<WfSpec, WfSpecModel> storedResult;
        if (version != null) {
            WfSpecIdModel id = new WfSpecIdModel(name, version);
            storedResult = (StoredGetable<WfSpec, WfSpecModel>) rocksdb.get(id.getStoreableKey(), StoredGetable.class);
        } else {
            storedResult = rocksdb.getLastFromPrefix(WfSpecIdModel.getPrefix(name), StoredGetable.class);
        }

        return storedResult == null ? null : storedResult.getStoredObject();
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
