package io.littlehorse.common.model.getable.core.nodeoutput;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.NodeOutput;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a persistently stored node output value.
 * Node outputs are stored separately from NodeRuns.
 */
public class NodeOutputModel extends Storeable<NodeOutput> {

    @Getter
    @Setter
    private WfRunIdModel wfRunId;

    @Getter
    private VariableValueModel value;

    private int threadRunNumber;

    private int nodeRunPosition;

    private String nodeName;

    private Date createdAt;

    public NodeOutputModel() {}

    public NodeOutputModel(
            WfRunIdModel wfRunId, int threadRunNumber, String nodeName, VariableValueModel value, int nodeRunPosition) {
        this.wfRunId = wfRunId;
        this.threadRunNumber = threadRunNumber;
        this.nodeName = nodeName;
        this.value = value;
        this.nodeRunPosition = nodeRunPosition;
        this.createdAt = new Date();
    }

    @Override
    public Class<NodeOutput> getProtoBaseClass() {
        return NodeOutput.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        NodeOutput p = (NodeOutput) proto;
        threadRunNumber = p.getThreadRunNumber();
        nodeRunPosition = p.getNodeRunPosition();
        nodeName = p.getNodeName();
        value = VariableValueModel.fromProto(p.getValue(), context);
        createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
    }

    @Override
    public NodeOutput.Builder toProto() {
        NodeOutput.Builder builder = NodeOutput.newBuilder()
                .setThreadRunNumber(threadRunNumber)
                .setNodeRunPosition(nodeRunPosition)
                .setNodeName(nodeName)
                .setValue(value.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt));
        return builder;
    }

    @Override
    public String getStoreKey() {
        String rest = threadRunNumber + "/" + nodeName;
        return Storeable.getGroupedFullStoreKey(wfRunId, getType(), rest);
    }

    @Override
    public StoreableType getType() {
        return StoreableType.NODE_OUTPUT;
    }

    @Override
    public Optional<WfRunIdModel> getGroupingWfRunId() {
        return Optional.ofNullable(wfRunId);
    }
}
