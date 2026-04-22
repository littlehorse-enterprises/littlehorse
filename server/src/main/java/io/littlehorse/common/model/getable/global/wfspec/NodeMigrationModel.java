package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.NodeMigration;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class NodeMigrationModel extends LHSerializable<NodeMigration> {

    private String newNodeName;
    private ExecutionContext context;

    @Override
    public Class<NodeMigration> getProtoBaseClass() {
        return NodeMigration.class;
    }

    @Override
    public NodeMigration.Builder toProto() {
        NodeMigration.Builder out = NodeMigration.newBuilder().setNewNodeName(newNodeName);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext executionContext) {
        NodeMigration p = (NodeMigration) proto;
        newNodeName = p.getNewNodeName();
        this.context = executionContext;
    }

    public String getNewNodeName() {
        return this.newNodeName;
    }

    public ExecutionContext getContext() {
        return this.context;
    }

    public void setNewNodeName(final String newNodeName) {
        this.newNodeName = newNodeName;
    }

    public void setContext(final ExecutionContext context) {
        this.context = context;
    }
}
