package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.NodeReference;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class NodeReferenceModel extends LHSerializable<NodeReference> {

    private ThreadSpecReferenceModel threadSpec;
    private String nodeType;
    private Integer nodeRunPosition;

    public NodeReferenceModel() {}

    public NodeReferenceModel(ThreadSpecReferenceModel threadSpec, String nodeType, Integer nodeRunPosition) {
        this.threadSpec = threadSpec;
        this.nodeRunPosition = nodeRunPosition;
        this.nodeType = nodeType;
    }

    public NodeReferenceModel(ThreadSpecReferenceModel threadSpec, String nodeType) {
        this.threadSpec = threadSpec;
        this.nodeType = nodeType;
    }

    public NodeReferenceModel(ThreadSpecReferenceModel threadSpec) {
        this.threadSpec = threadSpec;
    }

    @Override
    public NodeReference.Builder toProto() {
        NodeReference.Builder out = NodeReference.newBuilder();
        out.setThreadSpec(threadSpec.toProto());
        if (nodeType != null) {
            out.setNodeType(nodeType);
        }
        if (nodeRunPosition != null) {
            out.setNodePosition(nodeRunPosition);
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        NodeReference p = (NodeReference) proto;
        this.threadSpec = LHSerializable.fromProto(p.getThreadSpec(), ThreadSpecReferenceModel.class, context);
        this.nodeType = p.hasNodeType() ? p.getNodeType() : null;
        this.nodeRunPosition = p.hasNodePosition() ? p.getNodePosition() : null;
    }

    @Override
    public Class<NodeReference> getProtoBaseClass() {
        return NodeReference.class;
    }
}
