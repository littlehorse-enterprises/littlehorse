package io.littlehorse.common.model.getable.core.nodeoutput;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.NodeOutput;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a persistently stored node output value.
 * Node outputs are stored separately from NodeRuns.
 */
@Getter
@Setter
public class NodeOutputModel extends Storeable<NodeOutput> {

    private VariableValueModel value;
    private Date createdAt;
    private WfSpecIdModel wfSpecId;
    private int nodeRunPosition;

    public NodeOutputModel() {}

    public NodeOutputModel(
            NodeOutputIdModel id, VariableValueModel value, WfSpecIdModel wfSpecId, int nodeRunPosition) {
        this.id = id;
        this.value = value;
        this.wfSpecId = wfSpecId;
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
        value = VariableValueModel.fromProto(p.getValue(), context);
        createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        nodeRunPosition = p.getNodeRunPosition();
    }

    @Override
    public NodeOutput.Builder toProto() {
        NodeOutput.Builder builder = NodeOutput.newBuilder()
                .setValue(value.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setWfSpecId(wfSpecId.toProto())
                .setNodeRunPosition(nodeRunPosition);
        return builder;
    }


    @Override
    public String getStoreKey() {
        return "";
    }

    @Override
    public StoreableType getType() {
        return null;
    }
}
