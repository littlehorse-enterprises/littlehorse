package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.NodeMigration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeMigrationModel extends LHSerializable<NodeMigration> {

    private String newNodeName;

    public Class<NodeMigration> getProtoBaseClass() {
        return NodeMigration.class;
    }

    public NodeMigration.Builder toProto() {
        NodeMigration.Builder out = NodeMigration.newBuilder().setNewNodeName(newNodeName);
        return out;
    }

    public void initFrom(Message proto) {
        NodeMigration p = (NodeMigration) proto;
        newNodeName = p.getNewNodeName();
    }
}
