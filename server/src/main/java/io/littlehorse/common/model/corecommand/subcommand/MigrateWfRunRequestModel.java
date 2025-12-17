package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.migration.WfRunMigrationPlanModel;
import io.littlehorse.common.model.getable.objectId.MigrationPlanIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.MigrateWfRunRequest;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MigrateWfRunRequestModel extends CoreSubCommand<MigrateWfRunRequest> {

    private MigrationPlanIdModel migrationPlanId;
    private WfRunIdModel wfRunId;
    private int revisionNumber;
    private int majorVersionNumber;

    @Override
    public Class<MigrateWfRunRequest> getProtoBaseClass() {
        return MigrateWfRunRequest.class;
    }

    @Override
    public MigrateWfRunRequest.Builder toProto() {
        return MigrateWfRunRequest.newBuilder()
                .setMigrationPlanId(migrationPlanId.toProto())
                .setWfRunId(wfRunId.toProto())
                .setRevisionNumber(revisionNumber)
                .setMajorVersionNumber(majorVersionNumber);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        MigrateWfRunRequest p = (MigrateWfRunRequest) proto;
        migrationPlanId = LHSerializable.fromProto(p.getMigrationPlanId(), MigrationPlanIdModel.class, context);
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        revisionNumber = p.getRevisionNumber();
        majorVersionNumber = p.getMajorVersionNumber();
    }

    @Override
    public String getPartitionKey() {
        return wfRunId.getPartitionKey().get();
    }

    @Override
    public WfRunId process(CoreProcessorContext executionContext, LHServerConfig config) {
        // Get the WfRun to migrate
        WfRunModel wfRunModel = executionContext.getableManager().get(wfRunId);
        if (wfRunModel == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find WfRun: " + wfRunId);
        }

        // Get the migration plan
        WfRunMigrationPlanModel migrationPlan = executionContext.service().getWfRunMigrationPlan(migrationPlanId.getName());
        if (migrationPlan == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find MigrationPlan: " + migrationPlanId.getName());
        }

        // Perform the migration
        wfRunModel.processMigration(this, migrationPlan, executionContext);
        
        return wfRunId.toProto().build();
    }

    public static MigrateWfRunRequestModel fromProto(MigrateWfRunRequest p, ExecutionContext context) {
        MigrateWfRunRequestModel out = new MigrateWfRunRequestModel();
        out.initFrom(p, context);
        return out;
    }
}

