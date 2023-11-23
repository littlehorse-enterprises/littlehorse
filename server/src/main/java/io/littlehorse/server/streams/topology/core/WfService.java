package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLsModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.ACLAction;
import io.littlehorse.common.proto.ACLResource;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.Principal;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.ReadOnlyModelStore;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class WfService {

    private final ReadOnlyModelStore coreStore;
    private final ReadOnlyModelStore globalStore;
    private final MetadataCache metadataCache;
    private final ReadOnlyGetableManager storageManager;

    public WfService(
            ReadOnlyModelStore coreStore,
            ReadOnlyModelStore globalStore,
            MetadataCache metadataCache,
            ReadOnlyGetableManager storageManager) {
        this.coreStore = coreStore;
        this.metadataCache = metadataCache;
        this.storageManager = storageManager;
        this.globalStore = globalStore;
    }

    public WfSpecModel getWfSpec(String name, Integer version) {
        Supplier<WfSpecModel> findWfSpec = () -> {
            final StoredGetable<WfSpec, WfSpecModel> storedResult;
            if (version != null) {
                storedResult = globalStore.get(new WfSpecIdModel(name, version).getStoreableKey(), StoredGetable.class);
            } else {
                storedResult = globalStore.getLastFromPrefix(WfSpecIdModel.getPrefix(name), StoredGetable.class);
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
                    globalStore.get(id.getStoreableKey(), StoredGetable.class);
        } else {
            storedResult = globalStore.getLastFromPrefix(UserTaskDefIdModel.getPrefix(name), StoredGetable.class);
        }

        return storedResult == null ? null : storedResult.getStoredObject();
    }

    public ExternalEventDefModel getExternalEventDef(String name) {
        @SuppressWarnings("unchecked")
        StoredGetable<ExternalEventDef, ExternalEventDefModel> storedResult =
                (StoredGetable<ExternalEventDef, ExternalEventDefModel>)
                        globalStore.get(new ExternalEventDefIdModel(name).getStoreableKey(), StoredGetable.class);

        return storedResult == null ? null : storedResult.getStoredObject();
    }

    public TaskDefModel getTaskDef(String name) {
        TaskDefIdModel id = new TaskDefIdModel(name);
        Supplier<TaskDefModel> findTaskDef = () -> {
            StoredGetable<TaskDef, TaskDefModel> storedResult =
                    globalStore.get(id.getStoreableKey(), StoredGetable.class);
            return storedResult == null ? null : storedResult.getStoredObject();
        };
        return (TaskDefModel) metadataCache.getOrCache(id, findTaskDef::get);
    }

    public PrincipalModel getPrincipal(String id) {
        if (id == null) {
            id = LHConstants.ANONYMOUS_PRINCIPAL;
        }

        @SuppressWarnings("unchecked")
        StoredGetable<Principal, PrincipalModel> storedResult = (StoredGetable<Principal, PrincipalModel>)
                globalStore.get(new PrincipalIdModel(id).getStoreableKey(), StoredGetable.class);

        if (storedResult == null && id.equals(LHConstants.ANONYMOUS_PRINCIPAL)) {
            // This means that all of the following are true:
            // - The `anonymous` Principal has not yet been modified by the customer.
            // - We just implicitly create it.
            return createAnonymousPrincipal();
        }

        return storedResult == null ? null : storedResult.getStoredObject();
    }

    private PrincipalModel createAnonymousPrincipal() {
        List<ACLAction> allActions = List.of(ACLAction.ALL_ACTIONS);
        List<ACLResource> allResources = List.of(ACLResource.ACL_ALL_RESOURCES);

        ServerACLsModel acls = new ServerACLsModel();
        acls.getAcls().add(new ServerACLModel(allResources, allActions));

        PrincipalModel out = new PrincipalModel();
        out.setId(LHConstants.ANONYMOUS_PRINCIPAL);
        out.setGlobalAcls(acls);
        return out;
    }

    public List<String> adminPrincipalIds() {
        String startKey =
                "%s/%s/__isAdmin_true".formatted(StoreableType.TAG.getNumber(), GetableClassEnum.PRINCIPAL.getNumber());
        String endKey = startKey + "~";
        LHKeyValueIterator<Tag> result = globalStore.range(startKey, endKey, Tag.class);
        List<String> adminPrincipalIds = new ArrayList<>();
        result.forEachRemaining(tagLHIterKeyValue -> {
            adminPrincipalIds.add(tagLHIterKeyValue.getValue().getDescribedObjectId());
        });
        return adminPrincipalIds;
    }

    public WfRunModel getWfRun(String id) {
        return storageManager.get(new WfRunIdModel(id));
    }
}
