package io.littlehorse.common.model.getable.core.nodeoutput;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.NodeOutputIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
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
 * Represents a persistently stored node output that can be retrieved even after
 * the NodeRun is deleted. This improves NodeOutputReference performance and supports
 * node retention policies.
 */
@Getter
@Setter
public class NodeOutputModel extends CoreGetable<NodeOutput> {

    private NodeOutputIdModel id;
    private VariableValueModel value;
    private Date createdAt;
    private WfSpecIdModel wfSpecId;
    private int nodeRunPosition;

    public NodeOutputModel() {}

    public NodeOutputModel(NodeOutputIdModel id, VariableValueModel value, WfSpecIdModel wfSpecId, int nodeRunPosition) {
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
        id = LHSerializable.fromProto(p.getId(), NodeOutputIdModel.class, context);
        value = VariableValueModel.fromProto(p.getValue(), context);
        createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        nodeRunPosition = p.getNodeRunPosition();
    }

    @Override
    public NodeOutput.Builder toProto() {
        NodeOutput.Builder builder = NodeOutput.newBuilder()
                .setId(id.toProto())
                .setValue(value.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setWfSpecId(wfSpecId.toProto())
                .setNodeRunPosition(nodeRunPosition);
        return builder;
    }

    @Override
    public NodeOutputIdModel getObjectId() {
        return id;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        // No special indexing needed for NodeOutput entities
        return List.of();
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        // No indexed fields for NodeOutput entities
        return List.of();
    }
}