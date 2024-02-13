package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.LHConstants;
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
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class WfService {

    private final ReadOnlyMetadataManager metadataManager;
    private final MetadataCache metadataCache;

    public WfService(
            ReadOnlyMetadataManager metadataManager, MetadataCache metadataCache, ExecutionContext executionContext) {
        this.metadataCache = metadataCache;
        this.metadataManager = metadataManager;
    }

    public WfSpecModel getWfSpec(String name, Integer majorVersion, Integer revision) {
        Supplier<WfSpecModel> findWfSpec = () -> {
            final WfSpecModel storedResult;

            if (majorVersion != null && revision != null) {
                storedResult = metadataManager.get(new WfSpecIdModel(name, majorVersion, revision));
            } else if (majorVersion != null) {
                storedResult = metadataManager.getLastFromPrefix(
                        WfSpecIdModel.getPrefix(name, majorVersion), WfSpecModel.class);
            } else {
                storedResult = metadataManager.getLastFromPrefix(WfSpecIdModel.getPrefix(name), WfSpecModel.class);
            }

            return storedResult;
        };
        // return metadataCache.getOrCache(name, majorVersion, findWfSpec);
        return findWfSpec.get();
    }

    public WfSpecModel getWfSpec(WfSpecIdModel id) {
        return getWfSpec(id.getName(), id.getMajorVersion(), id.getRevision());
    }

    public UserTaskDefModel getUserTaskDef(String name, Integer version) {
        if (version != null) {
            UserTaskDefIdModel id = new UserTaskDefIdModel(name, version);
            return metadataManager.get(id);
        } else {
            return metadataManager.getLastFromPrefix(UserTaskDefIdModel.getPrefix(name), UserTaskDefModel.class);
        }
    }

    public ExternalEventDefModel getExternalEventDef(String name) {
        return metadataManager.get(new ExternalEventDefIdModel(name));
    }

    public TaskDefModel getTaskDef(String name) {
        /*TaskDefIdModel id = new TaskDefIdModel(name);
        Supplier<TaskDef> findTaskDef = () -> {
            TaskDefModel result = metadataManager.get(id);
            if (result != null) {
                return result.toProto().build();
            }
            return null;
        };
        StoredGetablePb result = (StoredGetablePb) metadataCache.getOrCache(id.toProto().build(), findTaskDef::get);

        if(result != null) {
            try {
                TaskDef taskDef = TaskDef.parseFrom(result.getGetablePayload());
                return LHSerializable.fromProto(taskDef, TaskDefModel.class, executionContext);
            } catch (Exception ex){
                return null;
            }
        }else {
            return null;
        }*/
        TaskDefIdModel id = new TaskDefIdModel(name);
        return metadataManager.get(id);
    }

    public PrincipalModel getPrincipal(PrincipalIdModel id) {
        if (id == null) {
            id = new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL);
        }

        PrincipalModel storedResult = metadataManager.get(id);

        if (storedResult == null && id.getId().equals(LHConstants.ANONYMOUS_PRINCIPAL)) {
            // This means that all of the following are true:
            // - The `anonymous` Principal has not yet been modified by the customer.
            // - We just implicitly create it.
            return createAnonymousPrincipal();
        }

        return storedResult;
    }

    private PrincipalModel createAnonymousPrincipal() {
        List<ACLAction> allActions = List.of(ACLAction.ALL_ACTIONS);
        List<ACLResource> allResources = List.of(ACLResource.ACL_ALL_RESOURCES);

        ServerACLsModel acls = new ServerACLsModel();
        acls.getAcls().add(new ServerACLModel(allResources, allActions));

        PrincipalModel out = new PrincipalModel();
        out.setId(new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL));
        out.setGlobalAcls(acls);
        return out;
    }

    public List<String> adminPrincipalIds() {
        List<Tag> result =
                metadataManager.tagScan(GetableClassEnum.PRINCIPAL, List.of(new Attribute("isAdmin", "true")));
        List<String> adminPrincipalIds = new ArrayList<>();
        for (Tag storedTag : result) {
            adminPrincipalIds.add(storedTag.getDescribedObjectId());
        }
        return adminPrincipalIds;
    }
}
