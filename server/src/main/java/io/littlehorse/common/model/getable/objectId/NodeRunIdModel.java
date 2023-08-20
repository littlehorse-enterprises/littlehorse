package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;

import io.littlehorse.common.model.getable.ObjectId;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeRunIdModel extends ObjectId<NodeRunId, NodeRun, NodeRunModel> {

    private String wfRunId;
    private int threadRunNumber;
    private int position;

    public NodeRunIdModel() {
    }

    public NodeRunIdModel(String wfRunId, int threadRunNumber, int postion) {
        this.wfRunId = wfRunId;
        this.threadRunNumber = threadRunNumber;
        this.position = postion;
    }

    public Class<NodeRunId> getProtoBaseClass() {
        return NodeRunId.class;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public void initFrom(Message proto) {
        NodeRunId p = (NodeRunId) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
        position = p.getPosition();
    }

    public NodeRunId.Builder toProto() {
        NodeRunId.Builder out = NodeRunId.newBuilder()
                .setWfRunId(wfRunId)
                .setThreadRunNumber(threadRunNumber)
                .setPosition(position);
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(wfRunId, String.valueOf(threadRunNumber), String.valueOf(position));
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = split[0];
        threadRunNumber = Integer.valueOf(split[1]);
        position = Integer.valueOf(split[2]);
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.NODE_RUN;
    }
}
