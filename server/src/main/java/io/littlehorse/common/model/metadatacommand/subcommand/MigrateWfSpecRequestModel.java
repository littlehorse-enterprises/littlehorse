package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecVersionMigrationModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.MigrateWfSpecRequest;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import io.littlehorse.server.streams.topology.core.WfService;
import lombok.Getter;

@Getter
public class MigrateWfSpecRequestModel extends MetadataSubCommand<MigrateWfSpecRequest> {

    private WfSpecIdModel oldWfSpecId;
    private WfSpecVersionMigrationModel migration;

    @Override
    public Class<MigrateWfSpecRequest> getProtoBaseClass() {
        return MigrateWfSpecRequest.class;
    }

    @Override
    public MigrateWfSpecRequest.Builder toProto() {
        MigrateWfSpecRequest.Builder out = MigrateWfSpecRequest.newBuilder()
                .setOldWfSpec(oldWfSpecId.toProto())
                .setMigration(migration.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext executionContext) {
        MigrateWfSpecRequest p = (MigrateWfSpecRequest) proto;
        oldWfSpecId = LHSerializable.fromProto(p.getOldWfSpec(), WfSpecIdModel.class, executionContext);
        migration = LHSerializable.fromProto(p.getMigration(), WfSpecVersionMigrationModel.class, executionContext);
    }

    @Override
    public WfSpec process(MetadataProcessorContext metadataContext) {
        WfService service = metadataContext.service();
        MetadataManager metadataManager = metadataContext.metadataManager();
        WfSpecModel oldWfSpec = service.getWfSpec(oldWfSpecId);

        if (oldWfSpec == null) {
            throw new LHApiException(
                    Status.NOT_FOUND, "Migration refers to nonexisting WfSpec %s".formatted(oldWfSpecId.toString()));
        }

        WfSpecModel newWfSpec = service.getWfSpec(getNewWfSpecId());
        if (newWfSpec == null) {
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "Migration refers to nonexisting WfSpec %s version %d"
                            .formatted(this.oldWfSpecId.getName(), migration.getNewMajorVersion()));
        }

        migration.validate(oldWfSpec, newWfSpec);
        newWfSpec.setMigration(migration);

        // Future work: do a bulk update to "force" the update rather than doing lazy loading,
        // which means that the WfRun is updated only when it next advances.

        metadataManager.put(newWfSpec);
        // return newWfSpec.toProto().build();
        throw new LHApiException(Status.UNIMPLEMENTED, "WfSpec Version Migration is ongoing");
    }

    public WfSpecIdModel getNewWfSpecId() {
        return new WfSpecIdModel(oldWfSpecId.getName(), migration.getNewMajorVersion(), migration.getNewRevision());
    }
}
