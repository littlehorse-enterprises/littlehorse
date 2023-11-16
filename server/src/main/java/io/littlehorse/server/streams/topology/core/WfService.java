package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.GetableStorageManager;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.function.Supplier;

public class WfService {

    private final ModelStore modelStore;
    private final MetadataCache metadataCache;
    private final GetableStorageManager storageManager;

    public WfService(ModelStore modelStore, MetadataCache metadataCache, GetableStorageManager storageManager) {
        this.modelStore = modelStore;
        this.metadataCache = metadataCache;
        this.storageManager = storageManager;
    }

    public WfSpecModel getWfSpec(String name, Integer version) {
        Supplier<WfSpecModel> findWfSpec = () -> {
            final StoredGetable<WfSpec, WfSpecModel> storedResult;
            if (version != null) {
                storedResult = modelStore.get(new WfSpecIdModel(name, version).getStoreableKey(), StoredGetable.class);
            } else {
                storedResult = modelStore.getLastFromPrefix(WfSpecIdModel.getPrefix(name), StoredGetable.class);
            }
            return storedResult == null ? null : storedResult.getStoredObject();
        };
        return metadataCache.getOrCache(name, version, findWfSpec);
    }

    public UserTaskDefModel getUserTaskDef(String name, Integer version) {
        StoredGetable<UserTaskDef, UserTaskDefModel> storedResult;
        if (version != null) {
            UserTaskDefIdModel id = new UserTaskDefIdModel(name, version);
            storedResult = (StoredGetable<UserTaskDef, UserTaskDefModel>)
                    modelStore.get(id.getStoreableKey(), StoredGetable.class);
        } else {
            storedResult = modelStore.getLastFromPrefix(UserTaskDefIdModel.getPrefix(name), StoredGetable.class);
        }

        return storedResult == null ? null : storedResult.getStoredObject();
    }

    public ExternalEventDefModel getExternalEventDef(String name) {
        @SuppressWarnings("unchecked")
        StoredGetable<ExternalEventDef, ExternalEventDefModel> storedResult =
                (StoredGetable<ExternalEventDef, ExternalEventDefModel>)
                        modelStore.get(new ExternalEventDefIdModel(name).getStoreableKey(), StoredGetable.class);

        return storedResult == null ? null : storedResult.getStoredObject();
    }

    public TaskDefModel getTaskDef(String name) {
        TaskDefIdModel id = new TaskDefIdModel(name);
        Supplier<TaskDefModel> findTaskDef = () -> {
            StoredGetable<TaskDef, TaskDefModel> storedResult =
                    modelStore.get(id.getStoreableKey(), StoredGetable.class);
            return storedResult == null ? null : storedResult.getStoredObject();
        };
        return (TaskDefModel) metadataCache.getOrCache(id, findTaskDef::get);
    }

    public ExternalEventModel getUnclaimedEvent(String wfRunId, String externalEventDefName) {

        String extEvtPrefix = ExternalEventModel.getStorePrefix(wfRunId, externalEventDefName);

        return storageManager.getFirstByCreatedTimeFromPrefix(
                extEvtPrefix, ExternalEventModel.class, externalEvent -> !externalEvent.isClaimed());
    }

    public WfRunModel getWfRun(String id) {
        return storageManager.get(new WfRunIdModel(id));
    }
}
