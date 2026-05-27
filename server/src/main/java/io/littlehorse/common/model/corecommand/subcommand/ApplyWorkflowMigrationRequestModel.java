package io.littlehorse.common.model.corecommand.subcommand;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunIterator;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.global.migrations.WorkflowMigrationPlanModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.global.migrations.MigrationVarsModel;
import io.littlehorse.common.model.getable.global.migrations.ThreadMigrationPlanModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowMigrationPlanIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ApplyWorkflowMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.ThreadType;
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
        validateVarsProvided(migrationPlan);
        wfRun.setMigrationVarsByThread(migrationVarsByThread);
        wfRun.setWorkflowMigrationPlanId(id);
        wfRun.advance(new Date());
        return wfRun.toProto().build();
    }


    private void validateVarsProvided(WorkflowMigrationPlanModel migrationPlan) {
        for (Map.Entry<String, ThreadMigrationPlanModel> entry :
                migrationPlan.getThreadMigrations().entrySet()) {
            String threadName = entry.getKey();
            ThreadMigrationPlanModel threadPlan = entry.getValue();

            MigrationVarsModel providedVars = migrationVarsByThread.get(threadName);

            for (String requiredVar : threadPlan.getRequiredVariables()) {
                if (providedVars == null
                        || !providedVars.getVarAssignmentByVarName().containsKey(requiredVar)) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "Thread '%s' requires variable '%s' but it was not provided"
                                    .formatted(threadName, requiredVar));
                }
            }
        }
    }

    private void validateSourceThreadsAreMigratable(WfRunModel wfRun, WorkflowMigrationPlanModel migrationPlan) {
        List<String> terminatedSourceThreads = new ArrayList<>();

        for (String sourceThreadSpec : migrationPlan.getThreadMigrations().keySet()) {
            boolean hasTerminatedThread = false;

            ThreadRunIterator threadRunIterator = wfRun.getThreadRunIterator();
            while (threadRunIterator.hasNext()) {
                ThreadRunModel threadRun = threadRunIterator.next();
                if (!isNormalThreadType(threadRun.getType())) {
                    continue;
                }

                if (sourceThreadSpec.equals(threadRun.getThreadSpecName()) && threadRun.isTerminated()) {
                    hasTerminatedThread = true;
                }
            }

            if (hasTerminatedThread) {
                terminatedSourceThreads.add(sourceThreadSpec);
            }
        }

        if (!terminatedSourceThreads.isEmpty()) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Cannot apply migration; source thread specs have terminated runs: %s"
                            .formatted(String.join(", ", terminatedSourceThreads)));
        }
    }

    private boolean isNormalThreadType(ThreadType threadType) {
        return threadType == ThreadType.ENTRYPOINT || threadType == ThreadType.CHILD;
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
