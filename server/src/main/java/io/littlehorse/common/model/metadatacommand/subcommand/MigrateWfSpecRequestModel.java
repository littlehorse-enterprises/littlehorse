package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecVersionMigrationModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.MigrateWfSpecRequest;
import io.littlehorse.sdk.common.proto.WfSpec;
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
    public void initFrom(Message proto) {
        MigrateWfSpecRequest p = (MigrateWfSpecRequest) proto;
        oldWfSpecId = LHSerializable.fromProto(p.getOldWfSpec(), WfSpecIdModel.class);
        migration = LHSerializable.fromProto(p.getMigration(), WfSpecVersionMigrationModel.class);
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public WfSpec process(MetadataProcessorDAO dao, LHServerConfig config) {
        WfSpecModel oldWfSpec = dao.getWfSpec(oldWfSpecId);

        if (oldWfSpec == null) {
            throw new LHApiException(
                    Status.NOT_FOUND, "Migration refers to nonexisting WfSpec %s".formatted(oldWfSpecId.toString()));
        }

        WfSpecModel newWfSpec = dao.getWfSpec(getNewWfSpecId());
        if (newWfSpec == null) {
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "Migration refers to nonexisting WfSpec %s version %d"
                            .formatted(oldWfSpecId.getName(), migration.getNewMajorVersion()));
        }

        migration.validate(oldWfSpec, newWfSpec);
        newWfSpec.setMigration(migration);

        // Future work: do a bulk update to "force" the update rather than doing lazy loading,
        // which means that the WfRun is updated only when it next advances.

        dao.put(newWfSpec);
        // return newWfSpec.toProto().build();
        throw new LHApiException(Status.UNIMPLEMENTED, "WfSpec Version Migration is ongoing");
    }

    public WfSpecIdModel getNewWfSpecId() {
        return new WfSpecIdModel(oldWfSpecId.getName(), migration.getNewMajorVersion(), migration.getNewRevision());
    }
}
