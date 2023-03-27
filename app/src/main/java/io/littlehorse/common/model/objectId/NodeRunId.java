package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.NodeRunIdPb;
import io.littlehorse.jlib.common.proto.NodeRunPb;

public class NodeRunId extends ObjectId<NodeRunIdPb, NodeRunPb, NodeRun> {

    public String wfRunId;
    public int threadRunNumber;
    public int position;

    public NodeRunId() {}

    public NodeRunId(String wfRunId, int threadRunNumber, int postion) {
        this.wfRunId = wfRunId;
        this.threadRunNumber = threadRunNumber;
        this.position = postion;
    }

    public Class<NodeRunIdPb> getProtoBaseClass() {
        return NodeRunIdPb.class;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public void initFrom(Message proto) {
        NodeRunIdPb p = (NodeRunIdPb) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
        position = p.getPosition();
    }

    public NodeRunIdPb.Builder toProto() {
        NodeRunIdPb.Builder out = NodeRunIdPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setPosition(position);
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(
            wfRunId,
            String.valueOf(threadRunNumber),
            String.valueOf(position)
        );
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = split[0];
        threadRunNumber = Integer.valueOf(split[1]);
        position = Integer.valueOf(split[2]);
    }

    public GETableClassEnumPb getType() {
        return GETableClassEnumPb.NODE_RUN;
    }
}
