package io.littlehorse.server.streams.topology.core;

import com.google.protobuf.Message;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.dao.ReadOnlyMetadataDAO;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLsModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.ACLAction;
import io.littlehorse.common.proto.ACLResource;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.Principal;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.Tenant;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.ReadOnlyModelStore;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ReadOnlyMetadataDAOImpl implements ReadOnlyMetadataDAO {

    private final ReadOnlyModelStore lhStore;
    private final MetadataCache metadataCache;
    private final AuthorizationContext context;

    public ReadOnlyMetadataDAOImpl(
            final ReadOnlyModelStore lhStore, final MetadataCache metadataCache, final AuthorizationContext context) {
        this.lhStore = lhStore;
        this.metadataCache = metadataCache;
        this.context = context;
    }

    public <U extends Message, T extends AbstractGetable<U>> T get(ObjectIdModel<?, U, T> id) {
        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storeResult = lhStore.get(id.getStoreableKey(), StoredGetable.class);

        if (storeResult == null) {
            return null;
        }
        return storeResult.getStoredObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public WfSpecModel getWfSpec(String name, Integer version) {
        Supplier<WfSpecModel> findWfSpec = () -> {
            final StoredGetable<WfSpec, WfSpecModel> storedResult;
            if (version != null) {
                storedResult = lhStore.get(new WfSpecIdModel(name, version).getStoreableKey(), StoredGetable.class);
            } else {
                storedResult = lhStore.getLastFromPrefix(WfSpecIdModel.getPrefix(name), StoredGetable.class);
            }
            return storedResult == null ? null : storedResult.getStoredObject();
        };
        return metadataCache.getOrCache(name, version, findWfSpec);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TenantModel getTenant(String name) {
        TenantIdModel id = new TenantIdModel(name);
        Supplier<TenantModel> findTenant = () -> {
            StoredGetable<Tenant, TenantModel> storedResult = lhStore.get(id.getStoreableKey(), StoredGetable.class);
            return storedResult.getStoredObject();
        };

        return (TenantModel) metadataCache.getOrCache(id, findTenant::get);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TaskDefModel getTaskDef(String name) {
        TaskDefIdModel id = new TaskDefIdModel(name);
        Supplier<TaskDefModel> findTaskDef = () -> {
            StoredGetable<TaskDef, TaskDefModel> storedResult = lhStore.get(id.getStoreableKey(), StoredGetable.class);
            return storedResult == null ? null : storedResult.getStoredObject();
        };
        return (TaskDefModel) metadataCache.getOrCache(id, findTaskDef::get);
    }

    @Override
    public ExternalEventDefModel getExternalEventDef(String name) {
        @SuppressWarnings("unchecked")
        StoredGetable<ExternalEventDef, ExternalEventDefModel> storedResult =
                (StoredGetable<ExternalEventDef, ExternalEventDefModel>)
                        lhStore.get(new ExternalEventDefIdModel(name).getStoreableKey(), StoredGetable.class);

        return storedResult == null ? null : storedResult.getStoredObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public UserTaskDefModel getUserTaskDef(String name, Integer version) {
        StoredGetable<UserTaskDef, UserTaskDefModel> storedResult;
        if (version != null) {
            UserTaskDefIdModel id = new UserTaskDefIdModel(name, version);
            storedResult = (StoredGetable<UserTaskDef, UserTaskDefModel>)
                    lhStore.get(id.getStoreableKey(), StoredGetable.class);
        } else {
            storedResult = lhStore.getLastFromPrefix(UserTaskDefIdModel.getPrefix(name), StoredGetable.class);
        }

        return storedResult == null ? null : storedResult.getStoredObject();
    }

    @Override
    public AuthorizationContext context() {
        return context;
    }

}
