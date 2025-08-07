package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.VariableAssignment.NodeOutputReference;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class NodeOutputReferenceModel extends LHSerializable<NodeOutputReference> {

    private String nodeName;

    public NodeOutputReferenceModel() {}

    public NodeOutputReferenceModel(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public Class<NodeOutputReference> getProtoBaseClass() {
        return NodeOutputReference.class;
    }

    @Override
    public NodeOutputReference.Builder toProto() {
        return NodeOutputReference.newBuilder().setNodeName(nodeName);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        NodeOutputReference p = (NodeOutputReference) proto;
        nodeName = p.getNodeName();
    }
}
