package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.sdk.common.proto.NodeMigration;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    public void execute(ThreadRunModel thread) {
        NodeRunModel currentNode = thread.getCurrentNodeRun();

        // TODO: handle TASK differently
        currentNode.cancel();
        thread.setWfSpecId();
    }
}
