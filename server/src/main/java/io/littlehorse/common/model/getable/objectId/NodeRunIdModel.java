package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class NodeRunIdModel extends CoreObjectId<NodeRunId, NodeRun, NodeRunModel> {

    private WfRunIdModel wfRunId;
    private int threadRunNumber;
    private int position;

    public NodeRunIdModel() {}

    public NodeRunIdModel(WfRunIdModel wfRunId, int threadRunNumber, int position) {
        this.wfRunId = wfRunId;
        this.threadRunNumber = threadRunNumber;
        this.position = position;
    }

    public NodeRunIdModel(String wfRunId, int threadRunNumber, int position) {
        this(new WfRunIdModel(wfRunId), threadRunNumber, position);
    }

    @Override
    public Class<NodeRunId> getProtoBaseClass() {
        return NodeRunId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return wfRunId.getPartitionKey();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        NodeRunId p = (NodeRunId) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        threadRunNumber = p.getThreadRunNumber();
        position = p.getPosition();
    }

    @Override
    public NodeRunId.Builder toProto() {
        NodeRunId.Builder out = NodeRunId.newBuilder()
                .setWfRunId(wfRunId.toProto())
                .setThreadRunNumber(threadRunNumber)
                .setPosition(position);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfRunId.toString(), String.valueOf(threadRunNumber), String.valueOf(position));
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = (WfRunIdModel) ObjectIdModel.fromString(split[0], WfRunIdModel.class);
        threadRunNumber = Integer.valueOf(split[1]);
        position = Integer.valueOf(split[2]);
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.NODE_RUN;
    }
}
