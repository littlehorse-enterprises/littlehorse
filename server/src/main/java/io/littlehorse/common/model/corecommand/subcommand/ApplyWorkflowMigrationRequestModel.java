package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.migrations.MigrationVarsModel;
import io.littlehorse.common.model.getable.global.migrations.ThreadMigrationPlanModel;
import io.littlehorse.common.model.getable.global.migrations.WorkflowMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowMigrationPlanIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ApplyWorkflowMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.MigrationVars;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

        if (wfRun.getStatus() == LHStatus.COMPLETED
                || wfRun.getStatus() == LHStatus.ERROR
                || wfRun.getStatus() == LHStatus.EXCEPTION) {
            throw new LHApiException(
                    Status.FAILED_PRECONDITION,
                    "WfRun %s has status %s and cannot be migrated".formatted(wfRunId, wfRun.getStatus()));
        }

        // A WfRun holds a single in-flight migration plan. Until that plan clears (the WfRun is
        // fully on the new WfSpec), it cannot be overridden: stacking a second
        // migration would break the closed (oldWfSpec -> newWfSpec) assumption that the plan's
        // validation depends on.
        if (wfRun.getWorkflowMigrationPlanId() != null) {
            throw new LHApiException(
                    Status.FAILED_PRECONDITION, "A migration is already in progress for WfRun %s".formatted(wfRunId));
        }

        WorkflowMigrationPlanModel migrationPlan =
                executionContext.metadataManager().get(id);
        if (migrationPlan == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find WorkflowMigrationPlan %s".formatted(id));
        }

        validateMigrationVars(executionContext, migrationPlan);

        wfRun.setMigrationVarsByThread(migrationVarsByThread);
        wfRun.setWorkflowMigrationPlanId(id);
        wfRun.advance(new Date());
        return wfRun.toProto().build();
    }

    // The client supplies migrationVarsByThread to seed/override variables on the destination
    // threads at apply time. We validate the decidable properties here so a bad request fails
    // synchronously instead of failing a thread mid-migration with a runtime VAR_SUB_ERROR:
    // Non-literal assignments (references, format strings, expressions, ...) resolve at runtime,
    // so their type cannot be checked here and is left to the runtime var-sub.
    private void validateMigrationVars(
            CoreProcessorContext executionContext, WorkflowMigrationPlanModel migrationPlan) {
        if (migrationVarsByThread.isEmpty()) {
            return;
        }

        // migrationVarsByThread is keyed by the destination (new) thread name, so resolve the
        // new WfSpec to check the supplied values against each destination thread's scope.
        WfSpecIdModel newWfSpecId = new WfSpecIdModel(
                migrationPlan.getOldWfSpecId().getName(), migrationPlan.getMajorVersion(), migrationPlan.getRevision());
        WfSpecModel newWfSpec = executionContext.service().getWfSpec(newWfSpecId);
        if (newWfSpec == null) {
            throw new LHApiException(
                    Status.FAILED_PRECONDITION,
                    "Destination WfSpec %s for migration plan %s no longer exists".formatted(newWfSpecId, id));
        }

        Set<String> destinationThreadNames = new HashSet<>();
        for (ThreadMigrationPlanModel threadMigration :
                migrationPlan.getThreadMigrations().values()) {
            destinationThreadNames.add(threadMigration.getNewThreadName());
        }

        for (Map.Entry<String, MigrationVarsModel> threadEntry : migrationVarsByThread.entrySet()) {
            String threadName = threadEntry.getKey();

            if (!destinationThreadNames.contains(threadName)) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Migration variables provided for thread '%s', which is not a destination thread in "
                                        .formatted(threadName)
                                + "migration plan %s".formatted(id));
            }

            ThreadSpecModel destThreadSpec = newWfSpec.threadSpecs.get(threadName);

            for (Map.Entry<String, VariableAssignmentModel> varEntry :
                    threadEntry.getValue().getVarAssignmentByVarName().entrySet()) {
                String varName = varEntry.getKey();
                VariableAssignmentModel assn = varEntry.getValue();

                ThreadVarDefModel varDef = destThreadSpec.getVarDef(varName);
                if (varDef == null) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "Migration variable '%s' is not in scope of destination thread '%s'"
                                    .formatted(varName, threadName));
                }

                if (assn.getRhsSourceType() == SourceCase.LITERAL_VALUE
                        && !assn.canBeType(varDef.getVarDef().getTypeDef(), destThreadSpec)) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "Migration variable '%s' on thread '%s' has a value incompatible with its declared type %s"
                                    .formatted(
                                            varName,
                                            threadName,
                                            varDef.getVarDef().getTypeDef()));
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
            out.putMigrationVarsByThread(
                    entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ApplyWorkflowMigrationPlanRequest p = (ApplyWorkflowMigrationPlanRequest) proto;
        id = LHSerializable.fromProto(p.getId(), WorkflowMigrationPlanIdModel.class, context);
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        migrationVarsByThread = new HashMap<>();

        for (Map.Entry<String, MigrationVars> entry :
                p.getMigrationVarsByThreadMap().entrySet()) {
            migrationVarsByThread.put(
                    entry.getKey(), LHSerializable.fromProto(entry.getValue(), MigrationVarsModel.class, context));
        }
    }

    @Override
    public Class<ApplyWorkflowMigrationPlanRequest> getProtoBaseClass() {
        return ApplyWorkflowMigrationPlanRequest.class;
    }
}
