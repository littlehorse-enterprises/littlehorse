package io.littlehorse.server.streams.topology.core;

import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.events.WorkflowEventDefModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WfService {

    private final ReadOnlyMetadataManager metadataManager;
    private final MetadataCache metadataCache;
    private final ExecutionContext executionContext;

    public WfService(
            ReadOnlyMetadataManager metadataManager, MetadataCache metadataCache, ExecutionContext executionContext) {
        this.metadataCache = metadataCache;
        this.metadataManager = metadataManager;
        this.executionContext = executionContext;
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

    public WorkflowEventDefModel getWorkflowEventDef(String name) {
        return getWorkflowEventDef(new WorkflowEventDefIdModel(name));
    }

    public WorkflowEventDefModel getWorkflowEventDef(WorkflowEventDefIdModel id) {
        return metadataManager.get(id);
    }

    public StructDefModel getStructDef(String name, Integer version) {
        StructDefIdModel sd = new StructDefIdModel(name, version);
        return metadataManager.get(sd);
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

        PrincipalModel principalModel = metadataManager.get(id);

        if (principalModel == null && id.getId().equals(LHConstants.ANONYMOUS_PRINCIPAL)) {
            log.info(
                    "Anonymous Principal not found in store, likely due to initialization of global store. Should resolve within seconds.");
            throw new LHApiException(Status.UNAVAILABLE, "Server Initializing");
        }

        return principalModel;
    }

    public List<PrincipalIdModel> adminPrincipalIds() {
        List<Tag> result = metadataManager.clusterScopedTagScan(
                GetableClassEnum.PRINCIPAL, List.of(new Attribute("isAdmin", "true")));
        List<PrincipalIdModel> adminPrincipalIds = new ArrayList<>();
        for (Tag storedTag : result) {
            adminPrincipalIds.add(new PrincipalIdModel((storedTag.getDescribedObjectId())));
        }
        return adminPrincipalIds;
    }
}
