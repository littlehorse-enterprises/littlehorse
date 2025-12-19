package io.littlehorse.common.model.getable.global.wfspec.migration;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.NodeMigrationPlan;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeMigrationPlanModel extends LHSerializable<NodeMigrationPlan> {

    private String newNode;

    public NodeMigrationPlanModel() {}

    public NodeMigrationPlanModel(String newNode) {
        this.newNode = newNode;
    }

    @Override
    public Class<NodeMigrationPlan> getProtoBaseClass() {
        return NodeMigrationPlan.class;
    }

    @Override
    public NodeMigrationPlan.Builder toProto() {
        return NodeMigrationPlan.newBuilder()
                .setNewNode(newNode);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        NodeMigrationPlan p = (NodeMigrationPlan) proto;
        newNode = p.getNewNode();
    }

    public static NodeMigrationPlanModel fromProto(NodeMigrationPlan p, ExecutionContext context) {
        NodeMigrationPlanModel out = new NodeMigrationPlanModel();
        out.initFrom(p, context);
        return out;
    }
}

