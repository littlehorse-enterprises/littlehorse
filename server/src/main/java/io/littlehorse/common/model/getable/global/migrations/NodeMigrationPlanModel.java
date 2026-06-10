package io.littlehorse.common.model.getable.global.migrations;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.NodeMigrationPlan;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeMigrationPlanModel extends LHSerializable<NodeMigrationPlan> {

    private String newNodeName;

    @Override
    public NodeMigrationPlan.Builder toProto() {
        return NodeMigrationPlan.newBuilder().setNewNodeName(newNodeName);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        NodeMigrationPlan p = (NodeMigrationPlan) proto;
        newNodeName = p.getNewNodeName();
    }

    @Override
    public Class<NodeMigrationPlan> getProtoBaseClass() {
        return NodeMigrationPlan.class;
    }
}
