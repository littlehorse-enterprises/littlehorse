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
import io.littlehorse.common.model.getable.global.wfspec.migration.WfRunMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.MigrationPlanIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.MigrationPlanId;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WfService {

    private final ReadOnlyMetadataManager metadataManager;

    public WfService(ReadOnlyMetadataManager metadataManager) {
        this.metadataManager = Objects.requireNonNull(metadataManager);
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

    /**
     *  Implementation inspired by {@link WfService#getWfSpec(String, Integer, Integer)}
     */
    public StructDefModel getStructDef(String name, Integer version) {
        Supplier<StructDefModel> findStructDef = () -> {
            final StructDefModel storedResult;

            if (version != null) {
                storedResult = metadataManager.get(new StructDefIdModel(name, version));
            } else {
                storedResult =
                        metadataManager.getLastFromPrefix(StructDefIdModel.getPrefix(name), StructDefModel.class);
            }

            return storedResult;
        };
        return findStructDef.get();
    }

    public StructDefModel getStructDef(StructDefIdModel id) {
        return getStructDef(id.getName(), id.getVersion());
    }

    public TaskDefModel getTaskDef(String name) {
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

    public WfRunMigrationPlanModel getWfRunMigrationPlan(String name){
        MigrationPlanIdModel id = new MigrationPlanIdModel(name);
        return metadataManager.get(id);

    }
}
