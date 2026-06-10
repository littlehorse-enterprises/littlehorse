package io.littlehorse.common.model.metadatacommand.subcommand;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import com.google.protobuf.Message;

import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.migrations.NodeMigrationPlanModel;
import io.littlehorse.common.model.getable.global.migrations.ThreadMigrationPlanModel;
import io.littlehorse.common.model.getable.global.migrations.WorkflowMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowMigrationPlanIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.PutWorkflowMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlan;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import io.littlehorse.server.streams.topology.core.WfService;

public class PutWorkflowMigrationPlanRequestModel extends MetadataSubCommand<PutWorkflowMigrationPlanRequest> {
    private String name;
    private WfSpecIdModel oldWfSpecId;
    private int majorVersion;
    private int revision;
    private Map<String, ThreadMigrationPlanModel> threadMigrations;

    public Map<String, ThreadMigrationPlanModel> getThreadMigrations() {
        return threadMigrations;
    }

    @Override
    public Message process(MetadataProcessorContext executionContext) {
        WfService service = executionContext.service();
        MetadataManager metadataManager = executionContext.metadataManager();

        WfSpecIdModel newWfSpecId = new WfSpecIdModel(oldWfSpecId.getName(), majorVersion, revision);
        WfSpecModel newWfSpec = service.getWfSpec(newWfSpecId);
        if (newWfSpec == null) {
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "Destination WfSpec %s does not exist".formatted(newWfSpecId.toString()));
        }

        WfSpecModel oldWfSpec = service.getWfSpec(oldWfSpecId);
        if (oldWfSpec == null) {
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "Source WfSpec %s does not exist".formatted(oldWfSpecId.toString()));
        }

        validateThreadMigrations(oldWfSpec, newWfSpec);
        validateMigrationVars(newWfSpec, oldWfSpec);
        WorkflowMigrationPlanIdModel id = new WorkflowMigrationPlanIdModel(name);
        WorkflowMigrationPlanModel plan = new WorkflowMigrationPlanModel(
                id, new Date(), threadMigrations, oldWfSpecId, majorVersion, revision);
        metadataManager.put(plan);

        return plan.toProto().build();
    }

  

    private void validateThreadMigrations(WfSpecModel oldWfSpec, WfSpecModel newWfSpec) {
        if (threadMigrations == null || threadMigrations.isEmpty()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "threadMigrations cannot be null or empty");
        }

        for (Map.Entry<String, ThreadMigrationPlanModel> entry : threadMigrations.entrySet()) {
            String oldThreadName = entry.getKey();
            ThreadMigrationPlanModel migration = entry.getValue();

            // Validate old thread exists
            ThreadSpecModel oldThread = oldWfSpec.threadSpecs.get(oldThreadName);
            if (oldThread == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Source WfSpec has no threadSpec %s".formatted(oldThreadName));
            }

            // Validate new thread exists
            ThreadSpecModel newThread = newWfSpec.threadSpecs.get(migration.getNewThreadName());
            if (newThread == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Destination WfSpec has no threadSpec %s".formatted(migration.getNewThreadName()));
            }

            // Validate entrypoint threads only migrate to entrypoint threads,
            // and child threads only migrate to child threads
            boolean oldIsEntrypoint = oldThreadName.equals(oldWfSpec.entrypointThreadName);
            boolean newIsEntrypoint = migration.getNewThreadName().equals(newWfSpec.entrypointThreadName);
            if (oldIsEntrypoint && !newIsEntrypoint) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Entrypoint thread '%s' cannot migrate to non-entrypoint thread '%s'"
                                .formatted(oldThreadName, migration.getNewThreadName()));
            }
            if (!oldIsEntrypoint && newIsEntrypoint) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Child thread '%s' cannot migrate to entrypoint thread '%s'"
                                .formatted(oldThreadName, migration.getNewThreadName()));
            }

            // Validate each node migration: the old node (key) must exist on the source
            // thread, and the new node it maps to must exist on the destination thread.
            for (Map.Entry<String, NodeMigrationPlanModel> nodeEntry : migration.getNodeMigrations().entrySet()) {
                String oldNodeName = nodeEntry.getKey();
                NodeMigrationPlanModel nodeMigration = nodeEntry.getValue();

                if (oldThread.nodes.get(oldNodeName) == null) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "ThreadSpec %s on source WfSpec does not have node %s"
                                    .formatted(oldThreadName, oldNodeName));
                }

                if (newThread.nodes.get(nodeMigration.getNewNodeName()) == null) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "ThreadSpec %s on destination WfSpec does not have node %s"
                                    .formatted(migration.getNewThreadName(), nodeMigration.getNewNodeName()));
                }
            }

        }
    }

    private void validateMigrationVars(WfSpecModel newWfSpec, WfSpecModel oldWfSpec) {
        for (Map.Entry<String, ThreadMigrationPlanModel> e : threadMigrations.entrySet()) {
            String oldThreadName = e.getKey();
            ThreadMigrationPlanModel migration = e.getValue();
            ThreadSpecModel oldThreadSpec = oldWfSpec.getThreadSpecs().get(oldThreadName);
            ThreadSpecModel newThreadSpec = newWfSpec.getThreadSpecs().get(migration.getNewThreadName());

            for (String var : migration.getRequiredVariables()) {
                boolean isVarInScopeOfOld = oldThreadSpec.isVarInScope(var);
                boolean isVarInScopeOfNew = newThreadSpec.isVarInScope(var);

                if (!isVarInScopeOfOld && !isVarInScopeOfNew) {
                    throw new LHApiException(Status.NOT_FOUND,
                        "The variable '" + var + "' is listed as a required migration variable, but is not in the scope of source thread '" + oldThreadName + "' or destination thread '" + newThreadSpec.getName() + "'");
                }

                if (isVarInScopeOfOld && !isVarInScopeOfNew) {
                    throw new LHApiException(Status.FAILED_PRECONDITION,
                        "The variable '" + var + "' is in scope on source thread '" + oldThreadName + "' but is not accessible in destination thread '" + newThreadSpec.getName() + "'");
                }

                if (!isVarInScopeOfOld && isVarInScopeOfNew) {
                    // Var exists in new spec but not old — the thread that owns it in the new spec
                    // must exist before this thread migrates. Record it as a dependency (by new name).
                    String ownerThreadName = newWfSpec.getVarToThreadSpecMap().get(var);
                    if (!ownerThreadName.equals(newThreadSpec.getName())) {
                        boolean ownerIsInPlan = threadMigrations.values().stream()
                                .anyMatch(m -> ownerThreadName.equals(m.getNewThreadName()));
                        if (!ownerIsInPlan) {
                            throw new LHApiException(Status.FAILED_PRECONDITION,
                                "The variable '" + var + "' is required by the migration but the thread that defines it ('" + ownerThreadName + "') is not included in this migration plan");
                        }
                        if (!migration.getDependencies().contains(ownerThreadName)) {
                            migration.getDependencies().add(ownerThreadName);
                        }
                    }
                }
            }
        }
    }

    @Override
    public PutWorkflowMigrationPlanRequest.Builder toProto() {
        PutWorkflowMigrationPlanRequest.Builder out = PutWorkflowMigrationPlanRequest.newBuilder()
                .setName(name)
                .setOldWfSpec(oldWfSpecId.toProto())
                .setMajorVersion(majorVersion)
                .setRevision(revision);

        for (Map.Entry<String, ThreadMigrationPlanModel> entry : threadMigrations.entrySet()) {
            out.putThreadMigrations(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        PutWorkflowMigrationPlanRequest p = (PutWorkflowMigrationPlanRequest) proto;
        name = p.getName();
        oldWfSpecId = LHSerializable.fromProto(p.getOldWfSpec(), WfSpecIdModel.class, context);
        majorVersion = p.getMajorVersion();
        revision = p.getRevision();
        threadMigrations = new HashMap<>();

        for (Map.Entry<String, ThreadMigrationPlan> entry : p.getThreadMigrationsMap().entrySet()) {
            threadMigrations.put(
                    entry.getKey(),
                    LHSerializable.fromProto(entry.getValue(), ThreadMigrationPlanModel.class, context));
        }
    }

    @Override
    public Class<PutWorkflowMigrationPlanRequest> getProtoBaseClass() {
        return PutWorkflowMigrationPlanRequest.class;
    }
    
}
