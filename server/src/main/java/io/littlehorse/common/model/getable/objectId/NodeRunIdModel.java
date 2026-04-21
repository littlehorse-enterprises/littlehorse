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
    public Optional<WfRunIdModel> getGroupingWfRunId() {
        return Optional.of(wfRunId);
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

    public WfRunIdModel getWfRunId() {
        return this.wfRunId;
    }

    public int getThreadRunNumber() {
        return this.threadRunNumber;
    }

    public int getPosition() {
        return this.position;
    }

    public void setWfRunId(final WfRunIdModel wfRunId) {
        this.wfRunId = wfRunId;
    }

    public void setThreadRunNumber(final int threadRunNumber) {
        this.threadRunNumber = threadRunNumber;
    }

    public void setPosition(final int position) {
        this.position = position;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof NodeRunIdModel)) return false;
        final NodeRunIdModel other = (NodeRunIdModel) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getThreadRunNumber() != other.getThreadRunNumber()) return false;
        if (this.getPosition() != other.getPosition()) return false;
        final Object this$wfRunId = this.getWfRunId();
        final Object other$wfRunId = other.getWfRunId();
        if (this$wfRunId == null ? other$wfRunId != null : !this$wfRunId.equals(other$wfRunId)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof NodeRunIdModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getThreadRunNumber();
        result = result * PRIME + this.getPosition();
        final Object $wfRunId = this.getWfRunId();
        result = result * PRIME + ($wfRunId == null ? 43 : $wfRunId.hashCode());
        return result;
    }
}
