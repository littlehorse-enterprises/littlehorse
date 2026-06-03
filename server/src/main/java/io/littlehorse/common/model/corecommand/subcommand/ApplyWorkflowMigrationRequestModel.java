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

            if (threadMigrationPlan.getOldNodeName().equals(currentNode.getName())) {
                // Thread is at the migration node — it must be long-running to be migratable
                if (!currentNode.isLongRunning()) {
                    throw new LHApiException(Status.FAILED_PRECONDITION,
                            "Thread " + threadRun.getThreadSpecName() + " can not migrate from node "
                                    + currentNode.getName() + " since the nodeRun was already activated");
                }
            } else {
                // Thread is not yet at the migration node — check the migration node hasn't already been executed.
                // Note: this validation only covers paths that lead through the migration node; if the thread
                // hasn't reached it yet via a different path, we still could miss it.
                try {
                    threadRun.getMostRecentNodeRun(threadMigrationPlan.getOldNodeName());
                    throw new LHApiException(Status.FAILED_PRECONDITION,
                            "The node " + threadMigrationPlan.getOldNodeName() + " in thread "
                                    + threadRun.getThreadSpecName() + " can not be migrated since it has already been completed");
                } catch (LHVarSubError ex) {
                    // Node hasn't been executed yet — this is expected
                }
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
