package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeRunIdModel extends CoreObjectId<NodeRunId, NodeRun, NodeRunModel> {

    private String wfRunId;
    private int threadRunNumber;
    private int position;

    public NodeRunIdModel() {}

    public NodeRunIdModel(String wfRunId, int threadRunNumber, int postion) {
        this.wfRunId = wfRunId;
        this.threadRunNumber = threadRunNumber;
        this.position = postion;
    }

    @Override
    public Class<NodeRunId> getProtoBaseClass() {
        return NodeRunId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(wfRunId);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        NodeRunId p = (NodeRunId) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
        position = p.getPosition();
    }

    @Override
    public NodeRunId.Builder toProto() {
        NodeRunId.Builder out = NodeRunId.newBuilder()
                .setWfRunId(wfRunId)
                .setThreadRunNumber(threadRunNumber)
                .setPosition(position);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfRunId, String.valueOf(threadRunNumber), String.valueOf(position));
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = split[0];
        threadRunNumber = Integer.valueOf(split[1]);
        position = Integer.valueOf(split[2]);
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.NODE_RUN;
    }
}
