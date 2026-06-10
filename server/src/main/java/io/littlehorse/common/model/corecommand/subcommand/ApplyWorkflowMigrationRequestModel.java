package io.littlehorse.common.model.corecommand.subcommand;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.Message;

import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunIterator;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.migrations.MigrationVarsModel;
import io.littlehorse.common.model.getable.global.migrations.NodeMigrationPlanModel;
import io.littlehorse.common.model.getable.global.migrations.ThreadMigrationPlanModel;
import io.littlehorse.common.model.getable.global.migrations.WorkflowMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowMigrationPlanIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ApplyWorkflowMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.MigrationVars;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyWorkflowMigrationRequestModel extends CoreSubCommand<ApplyWorkflowMigrationPlanRequest> {

    private WorkflowMigrationPlanIdModel id;
    private WfRunIdModel wfRunId;
    private Map<String, MigrationVarsModel> migrationVarsByThread;

    public ApplyWorkflowMigrationRequestModel() {
        migrationVarsByThread = new HashMap<>();
    }

    @Override
    public Message process(CoreProcessorContext executionContext, LHServerConfig config) {
        WfRunModel wfRun = executionContext.getableManager().get(wfRunId);
        if (wfRun == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find WfRun %s".formatted(wfRunId));
        }

        WorkflowMigrationPlanModel migrationPlan = executionContext.metadataManager().get(id);
        if (migrationPlan == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find WorkflowMigrationPlan %s".formatted(id));
        }

        validateSourceThreadsAreMigratable(wfRun, migrationPlan);
        validateMigrationNodes(wfRun, migrationPlan);
        wfRun.setMigrationVarsByThread(migrationVarsByThread);
        wfRun.setWorkflowMigrationPlanId(id);
        wfRun.advance(new Date());
        return wfRun.toProto().build();
    }


 

    private void validateSourceThreadsAreMigratable(WfRunModel wfRun, WorkflowMigrationPlanModel migrationPlan) {
        Set<String> sourceThreadSpecs = migrationPlan.getThreadMigrations().keySet();
        Set<String> terminatedSourceThreads = new HashSet<>();

        ThreadRunIterator threadRunIterator = wfRun.getThreadRunIterator();
        while (threadRunIterator.hasNext()) {
            ThreadRunModel threadRun = threadRunIterator.next();
            if (threadRun.isTerminated() && sourceThreadSpecs.contains(threadRun.getThreadSpecName())) {
                terminatedSourceThreads.add(threadRun.getThreadSpecName());
            }
        }

        if (!terminatedSourceThreads.isEmpty()) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Cannot apply migration; source thread specs have terminated runs: %s"
                            .formatted(String.join(", ", terminatedSourceThreads)));
        }
    }

    private void validateMigrationNodes(WfRunModel wfRun, WorkflowMigrationPlanModel wfMigrationPlan) {
        ThreadRunIterator threadRunIterator = wfRun.getThreadRunIterator();
        while (threadRunIterator.hasNext()) {
            ThreadRunModel threadRun = threadRunIterator.next();

            // Skip inactive/terminated threads and threads not covered by this migration
            if (threadRun.isInactive() || threadRun.isTerminated()) continue;
            if (!wfMigrationPlan.getThreadMigrations().containsKey(threadRun.getThreadSpecName())) continue;

            NodeModel currentNode = threadRun.getCurrentNode();
            ThreadMigrationPlanModel threadMigrationPlan = wfMigrationPlan.getThreadMigrations().get(threadRun.getThreadSpecName());

            // A thread migrates as soon as it reaches any one of its migration nodes, so the
            // migration is valid as long as at least one migration node is still a viable
            // migration point. A migration node is viable if either:
            //   - the thread is currently sitting at it and it is long-running (so it can be
            //     halted and redirected), or
            //   - the thread has not yet executed it (it is still ahead on the thread's path).
            // The migration is only rejected when every migration node has already been
            // activated or completed, leaving no node to migrate from.
            boolean anyNodeViable = false;
            for (Map.Entry<String, NodeMigrationPlanModel> nodeEntry :
                    threadMigrationPlan.getNodeMigrations().entrySet()) {
                String oldNodeName = nodeEntry.getKey();

                if (oldNodeName.equals(currentNode.getName())) {
                    // Thread is at this migration node — it is viable only if it is long-running.
                    if (currentNode.isLongRunning()) {
                        anyNodeViable = true;
                        break;
                    }
                } else {
                    // Thread is not at this migration node — it is viable as long as the node
                    // has not already been executed.
                    // Note: this only covers paths that lead through the migration node; if the
                    // thread hasn't reached it yet via a different path, we still could miss it.
                    try {
                        threadRun.getMostRecentNodeRun(oldNodeName);
                        // Node has already been executed — not viable via this node.
                    } catch (LHVarSubError ex) {
                        // Node hasn't been executed yet — it is still ahead and viable.
                        anyNodeViable = true;
                        break;
                    }
                }
            }

            if (!anyNodeViable) {
                throw new LHApiException(Status.FAILED_PRECONDITION,
                        "Thread " + threadRun.getThreadSpecName() + " can not be migrated since all of its "
                                + "migration nodes have already been activated or completed");
            }
        }
    }


    @Override
    public String getPartitionKey() {
        return wfRunId.getPartitionKey().get();
    }

    @Override
    public ApplyWorkflowMigrationPlanRequest.Builder toProto() {
        ApplyWorkflowMigrationPlanRequest.Builder out = ApplyWorkflowMigrationPlanRequest.newBuilder()
                .setId(id.toProto())
                .setWfRunId(wfRunId.toProto());

        for (Map.Entry<String, MigrationVarsModel> entry : migrationVarsByThread.entrySet()) {
            out.putMigrationVarsByThread(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ApplyWorkflowMigrationPlanRequest p = (ApplyWorkflowMigrationPlanRequest) proto;
        id = LHSerializable.fromProto(p.getId(), WorkflowMigrationPlanIdModel.class, context);
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        migrationVarsByThread = new HashMap<>();

        for (Map.Entry<String, MigrationVars> entry : p.getMigrationVarsByThreadMap().entrySet()) {
            migrationVarsByThread.put(
                    entry.getKey(),
                    LHSerializable.fromProto(entry.getValue(), MigrationVarsModel.class, context));
        }
    }

    @Override
    public Class<ApplyWorkflowMigrationPlanRequest> getProtoBaseClass() {
        return ApplyWorkflowMigrationPlanRequest.class;
    }
}
