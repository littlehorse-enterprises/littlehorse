package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.sdk.common.proto.ThreadSpecMigration;
import io.littlehorse.sdk.common.proto.WfSpecVersionMigration;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WfSpecVersionMigrationModel extends LHSerializable<WfSpecVersionMigration> {

    private int newMajorVersion;
    private int newRevision;
    private Map<String, ThreadSpecMigrationModel> threadSpecMigrations;
    private ExecutionContext context;

    public WfSpecVersionMigrationModel() {
        threadSpecMigrations = new HashMap<>();
    }

    @Override
    public Class<WfSpecVersionMigration> getProtoBaseClass() {
        return WfSpecVersionMigration.class;
    }

    @Override
    public WfSpecVersionMigration.Builder toProto() {
        WfSpecVersionMigration.Builder out = WfSpecVersionMigration.newBuilder()
                .setNewMajorVersion(newMajorVersion)
                .setNewRevision(newRevision);

        for (Map.Entry<String, ThreadSpecMigrationModel> entry : threadSpecMigrations.entrySet()) {
            out.putThreadSpecMigrations(
                    entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext executionContext) {
        WfSpecVersionMigration p = (WfSpecVersionMigration) proto;
        newMajorVersion = p.getNewMajorVersion();
        newRevision = p.getNewRevision();

        for (Map.Entry<String, ThreadSpecMigration> e :
                p.getThreadSpecMigrationsMap().entrySet()) {
            threadSpecMigrations.put(
                    e.getKey(),
                    LHSerializable.fromProto(e.getValue(), ThreadSpecMigrationModel.class, executionContext));
        }
        this.context = executionContext;
    }

    /*
     * Compatibility rules:
     * - Check that every threadSpecName referred to actually exists.
     * - Check that every nodeName referred to actually exists.
     */
    public void validate(WfSpecModel oldWfSpec, WfSpecModel newWfSpec) throws LHApiException {
        for (Map.Entry<String, ThreadSpecMigrationModel> e : threadSpecMigrations.entrySet()) {
            String sourceThreadName = e.getKey();
            ThreadSpecMigrationModel migration = e.getValue();
            ThreadSpecModel sourceThread = oldWfSpec.getThreadSpecs().get(sourceThreadName);
            ThreadSpecModel destinationThread = newWfSpec.getThreadSpecs().get(migration.getNewThreadSpecName());

            if (sourceThread == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Source WfSpec has no threadSpec %s".formatted(sourceThreadName));
            }

            if (destinationThread == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Destination WfSpec has no threadSpec %s".formatted(migration.getNewThreadSpecName()));
            }

            validateThreadMigration(migration, sourceThread, destinationThread);
        }
    }

    /*
     * Check that every source and destination node actually exist. That's the only thing we
     * validate as of now.
     */
    private void validateThreadMigration(
            ThreadSpecMigrationModel migration, ThreadSpecModel source, ThreadSpecModel dest) {
        for (Map.Entry<String, NodeMigrationModel> e :
                migration.getNodeMigrations().entrySet()) {
            NodeMigrationModel nodeMigration = e.getValue();
            String sourceNodeName = e.getKey();

            NodeModel sourceNode = source.getNodes().get(sourceNodeName);
            if (sourceNode == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "ThreadSpec %s on source WfSpec does not have node %s"
                                .formatted(source.getName(), sourceNodeName));
            }

            String destNodeName = nodeMigration.getNewNodeName();
            NodeModel destNode = dest.getNodes().get(destNodeName);
            if (destNode == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "ThreadSpec %s on destination WfSpec does not have node %s"
                                .formatted(dest.getName(), destNodeName));
            }
        }
    }
}
