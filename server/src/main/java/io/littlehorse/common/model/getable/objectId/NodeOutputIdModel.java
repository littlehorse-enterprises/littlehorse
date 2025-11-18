package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.nodeoutput.NodeOutputModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.NodeOutput;
import io.littlehorse.sdk.common.proto.NodeOutputId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class NodeOutputIdModel extends CoreObjectId<NodeOutputId, NodeOutput, NodeOutputModel> {

    private WfRunIdModel wfRunId;
    private int threadRunNumber;
    private String nodeName;

    public NodeOutputIdModel() {}

    public NodeOutputIdModel(WfRunIdModel wfRunId, int threadRunNumber, String nodeName) {
        this.wfRunId = wfRunId;
        this.threadRunNumber = threadRunNumber;
        this.nodeName = nodeName;
    }

    public NodeOutputIdModel(String wfRunId, int threadRunNumber, String nodeName) {
        this(new WfRunIdModel(wfRunId), threadRunNumber, nodeName);
    }

    @Override
    public Class<NodeOutputId> getProtoBaseClass() {
        return NodeOutputId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return wfRunId.getPartitionKey();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        NodeOutputId p = (NodeOutputId) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        threadRunNumber = p.getThreadRunNumber();
        nodeName = p.getNodeName();
    }

    @Override
    public NodeOutputId.Builder toProto() {
        return NodeOutputId.newBuilder()
                .setWfRunId(wfRunId.toProto())
                .setThreadRunNumber(threadRunNumber)
                .setNodeName(nodeName);
    }

    @Override
    public String toString() {
        return wfRunId + "/" + threadRunNumber + "/" + nodeName;
    }

    @Override
    public Optional<WfRunIdModel> getGroupingWfRunId() {
        return Optional.of(wfRunId);
    }

    @Override
    public void initFromString(String storeKey) {
        String[] tokens = storeKey.split("/");
        if (tokens.length != 3) {
            throw new RuntimeException("Invalid store key: " + storeKey);
        }
        wfRunId = new WfRunIdModel(tokens[0]);
        threadRunNumber = Integer.parseInt(tokens[1]);
        nodeName = tokens[2];
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.NODE_OUTPUT;
    }
}
