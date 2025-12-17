package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.migration.MigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.migration.WfRunMigrationPlanModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.PutMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.WfRunMigrationPlan;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutMigrationPlanRequestModel extends MetadataSubCommand<PutMigrationPlanRequest> {

    private String name;
    private MigrationPlanModel migrationPlan;

    @Override
    public Class<PutMigrationPlanRequest> getProtoBaseClass() {
        return PutMigrationPlanRequest.class;
    }

    @Override
    public PutMigrationPlanRequest.Builder toProto() {
        return PutMigrationPlanRequest.newBuilder()
                .setName(name)
                .setMigrationPlan(migrationPlan.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PutMigrationPlanRequest p = (PutMigrationPlanRequest) proto;
        name = p.getName();
        migrationPlan = LHSerializable.fromProto(p.getMigrationPlan(), MigrationPlanModel.class, context);
    }

    @Override
    public WfRunMigrationPlan process(MetadataProcessorContext context) {
        MetadataManager metadataManager = context.metadataManager();

        
        WfRunMigrationPlanModel spec = new WfRunMigrationPlanModel(name, migrationPlan);
        metadataManager.put(spec);
        System.out.println(spec.getMigrationPlan()); 
        System.out.println(spec);
        return spec.toProto().build();
    }

    public static PutMigrationPlanRequestModel fromProto(PutMigrationPlanRequest p, ExecutionContext context) {
        PutMigrationPlanRequestModel out = new PutMigrationPlanRequestModel();
        out.initFrom(p, context);
        return out;
    }
}

