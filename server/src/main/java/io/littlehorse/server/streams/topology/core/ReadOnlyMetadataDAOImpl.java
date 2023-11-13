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

    @Override
    public List<String> adminPrincipalIds() {
        String startKey =
                "%s/%s/__isAdmin_true".formatted(StoreableType.TAG.getNumber(), GetableClassEnum.PRINCIPAL.getNumber());
        String endKey = startKey + "~";
        LHKeyValueIterator<Tag> result = lhStore.range(startKey, endKey, Tag.class);
        List<String> adminPrincipalIds = new ArrayList<>();
        result.forEachRemaining(tagLHIterKeyValue -> {
            adminPrincipalIds.add(tagLHIterKeyValue.getValue().getDescribedObjectId());
        });
        return adminPrincipalIds;
    }

    /**
     * NOTE for Eduwer--- after thinking about it a bit, I am not sure if it was a good idea for me to add
     * this method here. Because:
     * - Principal is NOT scoped to a Tenant (in the implementation of this commit)
     * - WfSpec/TaskDef/ExternalEventDef are INDEED scoped to a Tenant
     *
     * Which basically means that I think there should be two different DAO's:
     * - TenantScopedMetadataDAO (WfSpec, TaskDef, etc)
     * - ClusterScopedMetadataDAO (all the rest)
     *
     * Because depending on how this MetadataDAO is instantiated, we may either have a DefaultModelStore
     * or a TenantModelStore.
     *
     * I think the Default or Tenant ones should have separate DAO's. What do you think?
     */
    @Override
    public PrincipalModel getPrincipal(String id) {
        if (id == null) {
            id = LHConstants.ANONYMOUS_PRINCIPAL;
        }

        @SuppressWarnings("unchecked")
        StoredGetable<Principal, PrincipalModel> storedResult = (StoredGetable<Principal, PrincipalModel>)
                lhStore.get(new PrincipalIdModel(id).getStoreableKey(), StoredGetable.class);

        if (storedResult == null && id.equals(LHConstants.ANONYMOUS_PRINCIPAL)) {
            // This means that all of the following are true:
            // - The `anonymous` Principal has not yet been modified by the customer.
            // - We just implicitly create it.
            return createAnonymousPrincipal();
        }

        return storedResult == null ? null : storedResult.getStoredObject();
    }

    private static PrincipalModel createAnonymousPrincipal() {
        List<ACLAction> allActions = List.of(ACLAction.ALL_ACTIONS);
        List<ACLResource> allResources = List.of(ACLResource.ACL_ALL_RESOURCES);

        ServerACLsModel acls = new ServerACLsModel();
        acls.getAcls().add(new ServerACLModel(allResources, allActions));

        PrincipalModel out = new PrincipalModel();
        out.setId(LHConstants.ANONYMOUS_PRINCIPAL);
        out.setGlobalAcls(acls);
        return out;
    }
}
