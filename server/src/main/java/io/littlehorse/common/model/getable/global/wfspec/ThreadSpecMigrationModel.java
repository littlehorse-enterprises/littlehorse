package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.NodeMigration;
import io.littlehorse.sdk.common.proto.ThreadSpecMigration;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Map;

public class ThreadSpecMigrationModel extends LHSerializable<ThreadSpecMigration> {

    private String newThreadSpecName;
    private Map<String, NodeMigrationModel> nodeMigrations;
    private ExecutionContext context;

    @Override
    public Class<ThreadSpecMigration> getProtoBaseClass() {
        return ThreadSpecMigration.class;
    }

    @Override
    public ThreadSpecMigration.Builder toProto() {
        ThreadSpecMigration.Builder out = ThreadSpecMigration.newBuilder().setNewThreadSpecName(newThreadSpecName);

        for (Map.Entry<String, NodeMigrationModel> entry : nodeMigrations.entrySet()) {
            out.putNodeMigrations(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext executionContext) {
        ThreadSpecMigration p = (ThreadSpecMigration) proto;
        newThreadSpecName = p.getNewThreadSpecName();

        for (Map.Entry<String, NodeMigration> e : p.getNodeMigrationsMap().entrySet()) {
            nodeMigrations.put(
                    e.getKey(), LHSerializable.fromProto(e.getValue(), NodeMigrationModel.class, executionContext));
        }
        this.context = executionContext;
    }

    public String getNewThreadSpecName() {
        return this.newThreadSpecName;
    }

    public Map<String, NodeMigrationModel> getNodeMigrations() {
        return this.nodeMigrations;
    }

    public ExecutionContext getContext() {
        return this.context;
    }

    public void setNewThreadSpecName(final String newThreadSpecName) {
        this.newThreadSpecName = newThreadSpecName;
    }

    public void setNodeMigrations(final Map<String, NodeMigrationModel> nodeMigrations) {
        this.nodeMigrations = nodeMigrations;
    }

    public void setContext(final ExecutionContext context) {
        this.context = context;
    }
}
