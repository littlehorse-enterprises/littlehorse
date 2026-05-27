package io.littlehorse.common.model.metadatacommand.subcommand;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.Message;

import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.migrations.ThreadMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowMigrationPlanIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.PutWorkflowMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlan;
import io.littlehorse.common.model.getable.global.migrations.WorkflowMigrationPlanModel;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import io.littlehorse.server.streams.topology.core.WfService;

import java.util.Set;

public class PutWorkflowMigrationPlanRequestModel extends MetadataSubCommand<PutWorkflowMigrationPlanRequest> {
    private String name;
    private WfSpecIdModel oldWfSpecId;
    private int majorVersion;
    private int revision;
    private Map<String, ThreadMigrationPlanModel> threadMigrations;

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
        WorkflowMigrationPlanIdModel id = new WorkflowMigrationPlanIdModel(name);
        WorkflowMigrationPlanModel plan = new WorkflowMigrationPlanModel(
                id, new Date(), threadMigrations, oldWfSpecId, majorVersion, revision);
        metadataManager.put(plan);

        return plan.toProto().build();
    }

    private static final Set<NodeCase> LONG_RUNNING_NODE_TYPES = Set.of(
            NodeCase.EXTERNAL_EVENT,
            NodeCase.USER_TASK,
            NodeCase.SLEEP,
            NodeCase.WAIT_FOR_THREADS,
            NodeCase.WAIT_FOR_CONDITION,
            NodeCase.WAIT_FOR_CHILD_WF);

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

            // Validate old from_node exists
            NodeModel fromNode = oldThread.nodes.get(migration.getOldNodeName());
            if (fromNode == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "ThreadSpec %s on source WfSpec does not have node %s"
                                .formatted(oldThreadName, migration.getOldNodeName()));
            }

            // Validate from_node is a long-running node type
            if (!LONG_RUNNING_NODE_TYPES.contains(fromNode.type)) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Node %s on threadSpec %s is not a long-running node type. Migration is only supported from EXTERNAL_EVENT, USER_TASK, SLEEP, WAIT_FOR_THREADS, WAIT_FOR_CONDITION, or WAIT_FOR_CHILD_WF nodes"
                                .formatted(migration.getOldNodeName(), oldThreadName));
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

            // Validate new thread exists
            ThreadSpecModel newThread = newWfSpec.threadSpecs.get(migration.getNewThreadName());
            if (newThread == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Destination WfSpec has no threadSpec %s".formatted(migration.getNewThreadName()));
            }

            // Validate new to_node exists
            if (newThread.nodes.get(migration.getNewNodeName()) == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "ThreadSpec %s on destination WfSpec does not have node %s"
                                .formatted(migration.getNewThreadName(), migration.getNewNodeName()));
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
