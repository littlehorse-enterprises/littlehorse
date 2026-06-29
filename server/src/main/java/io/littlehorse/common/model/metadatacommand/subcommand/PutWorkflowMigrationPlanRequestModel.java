package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.migrations.NodeMigrationPlanModel;
import io.littlehorse.common.model.getable.global.migrations.ThreadMigrationPlanModel;
import io.littlehorse.common.model.getable.global.migrations.ThreadMigrationPlanRequestModel;
import io.littlehorse.common.model.getable.global.migrations.WorkflowMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowMigrationPlanIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.PutWorkflowMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlanRequest;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import io.littlehorse.server.streams.topology.core.WfService;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutWorkflowMigrationPlanRequestModel extends MetadataSubCommand<PutWorkflowMigrationPlanRequest> {
    private String name;
    private WfSpecIdModel oldWfSpecId;
    private int majorVersion;
    private int revision;
    private Map<String, ThreadMigrationPlanRequestModel> threadMigrations;

    public Map<String, ThreadMigrationPlanRequestModel> getThreadMigrations() {
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
                    Status.NOT_FOUND, "Destination WfSpec %s does not exist".formatted(newWfSpecId.toString()));
        }

        WfSpecModel oldWfSpec = service.getWfSpec(oldWfSpecId);
        if (oldWfSpec == null) {
            throw new LHApiException(
                    Status.NOT_FOUND, "Source WfSpec %s does not exist".formatted(oldWfSpecId.toString()));
        }

        validateThreadMigrations(oldWfSpec, newWfSpec);

        // Build the internal thread migration plans. The required_variables and
        // dependencies are computed internally rather than provided by the client.
        Map<String, ThreadMigrationPlanModel> internalThreadMigrations = new HashMap<>();
        for (Map.Entry<String, ThreadMigrationPlanRequestModel> entry : threadMigrations.entrySet()) {
            internalThreadMigrations.put(entry.getKey(), entry.getValue().toThreadMigrationPlan());
        }

        validateVariables(newWfSpec, oldWfSpec, internalThreadMigrations);
        WorkflowMigrationPlanIdModel id = new WorkflowMigrationPlanIdModel(name);
        WorkflowMigrationPlanModel plan = new WorkflowMigrationPlanModel(
                id, new Date(), internalThreadMigrations, oldWfSpecId, majorVersion, revision);
        metadataManager.put(plan);
        return plan.toProto().build();
    }

    private void validateThreadMigrations(WfSpecModel oldWfSpec, WfSpecModel newWfSpec) {
        if (threadMigrations == null || threadMigrations.isEmpty()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "threadMigrations cannot be null or empty");
        }

        for (Map.Entry<String, ThreadMigrationPlanRequestModel> entry : threadMigrations.entrySet()) {
            String oldThreadName = entry.getKey();
            ThreadMigrationPlanRequestModel migration = entry.getValue();

            // Validate old thread exists
            ThreadSpecModel oldThread = oldWfSpec.threadSpecs.get(oldThreadName);
            if (oldThread == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Source WfSpec has no threadSpec %s".formatted(oldThreadName));
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
            for (Map.Entry<String, NodeMigrationPlanModel> nodeEntry :
                    migration.getNodeMigrations().entrySet()) {
                String oldNodeName = nodeEntry.getKey();
                NodeMigrationPlanModel nodeMigration = nodeEntry.getValue();

                NodeModel oldNode = oldThread.nodes.get(oldNodeName);
                if (oldNode == null) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "ThreadSpec %s on source WfSpec does not have node %s"
                                    .formatted(oldThreadName, oldNodeName));
                }

                NodeModel newNode = newThread.nodes.get(nodeMigration.getNewNodeName());
                if (newNode == null) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "ThreadSpec %s on destination WfSpec does not have node %s"
                                    .formatted(migration.getNewThreadName(), nodeMigration.getNewNodeName()));
                }

                // Structural nodes (ENTRYPOINT, EXIT, NOP) do no work and have no meaningful
                // runtime state to redirect, so a migration may not start from or land on them.
                if (!isMigratableNodeType(oldNode)) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "Cannot migrate from node %s on source thread %s because %s nodes are not migratable"
                                    .formatted(oldNodeName, oldThreadName, oldNode.getType()));
                }
                if (!isMigratableNodeType(newNode)) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "Cannot migrate to node %s on destination thread %s because %s nodes are not migratable"
                                    .formatted(
                                            nodeMigration.getNewNodeName(),
                                            migration.getNewThreadName(),
                                            newNode.getType()));
                }
            }
        }
    }

    // Structural nodes do no work and cannot be a migration source or destination.
    private boolean isMigratableNodeType(NodeModel node) {
        switch (node.getType()) {
            case ENTRYPOINT:
            case EXIT:
            case NOP:
                return false;
            default:
                return true;
        }
    }

    // For each thread migration, gather all variables that the destination threadSpec uses.
    // Any variable that is not already in the scope of the source thread must be provided by
    // another thread that is being migrated to in this plan. That owning thread is recorded as
    // a dependency of this thread migration.
    private void validateVariables(
            WfSpecModel newWfSpec,
            WfSpecModel oldWfSpec,
            Map<String, ThreadMigrationPlanModel> internalThreadMigrations) {

        for (Map.Entry<String, ThreadMigrationPlanModel> e : internalThreadMigrations.entrySet()) {
            String oldThreadName = e.getKey();
            ThreadMigrationPlanModel migration = e.getValue();
            ThreadSpecModel oldThreadSpec = oldWfSpec.getThreadSpecs().get(oldThreadName);
            ThreadSpecModel newThreadSpec = newWfSpec.getThreadSpecs().get(migration.getNewThreadName());

            for (String var : newThreadSpec.getNamesOfVariablesUsed()) {
                // If the variable already exists in the running thread's scope, nothing to do.
                if (oldThreadSpec.isVarInScope(var)) {
                    continue;
                }

                // The variable is used by the destination thread but is not in the source
                // thread's scope, so it must be created at runtime by the thread that owns it
                // in the new spec.
                String ownerThreadName = newWfSpec.getVarToThreadSpecMap().get(var);
                if (ownerThreadName == null) {
                    throw new LHApiException(
                            Status.NOT_FOUND,
                            "The variable '" + var + "' is used by destination thread '" + newThreadSpec.getName()
                                    + "' but is not defined by any thread in the new WfSpec");
                }

                // If the destination thread itself owns the variable, migrating to it already
                // creates the variable; no separate dependency is required.
                if (ownerThreadName.equals(newThreadSpec.getName())) {
                    continue;
                }

                // The owning thread must be migrated to as part of this plan so that the
                // variable will exist at runtime.
                boolean ownerIsInPlan = internalThreadMigrations.values().stream()
                        .anyMatch(m -> ownerThreadName.equals(m.getNewThreadName()));
                if (!ownerIsInPlan) {
                    throw new LHApiException(
                            Status.FAILED_PRECONDITION,
                            "The variable '" + var + "' is required by destination thread '"
                                    + newThreadSpec.getName() + "' but the thread that defines it ('"
                                    + ownerThreadName + "') is not included in this migration plan");
                }

                if (!migration.getThreadSpecDependencies().contains(ownerThreadName)) {
                    migration.getThreadSpecDependencies().add(ownerThreadName);
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

        for (Map.Entry<String, ThreadMigrationPlanRequestModel> entry : threadMigrations.entrySet()) {
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

        for (Map.Entry<String, ThreadMigrationPlanRequest> entry :
                p.getThreadMigrationsMap().entrySet()) {
            threadMigrations.put(
                    entry.getKey(),
                    LHSerializable.fromProto(entry.getValue(), ThreadMigrationPlanRequestModel.class, context));
        }
    }

    @Override
    public Class<PutWorkflowMigrationPlanRequest> getProtoBaseClass() {
        return PutWorkflowMigrationPlanRequest.class;
    }
}
