package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.TaskNodeReference;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskNodeReferenceModel extends LHSerializable<TaskNodeReference> implements TaskRunSubSource {

    private NodeRunIdModel nodeRunId;
    private ExecutionContext context;
    private CoreProcessorContext processorContext;

    public TaskNodeReferenceModel() {}

    public TaskNodeReferenceModel(NodeRunIdModel nodeRunId, WfSpecIdModel wfSpecId) {
        this.nodeRunId = nodeRunId;
    }

    @Override
    public Class<TaskNodeReference> getProtoBaseClass() {
        return TaskNodeReference.class;
    }

    @Override
    public TaskNodeReference.Builder toProto() {
        TaskNodeReference.Builder out = TaskNodeReference.newBuilder().setNodeRunId(nodeRunId.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskNodeReference p = (TaskNodeReference) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class, context);
        this.context = context;
        this.processorContext = context.castOnSupport(CoreProcessorContext.class);
    }

    @Override
    public WfRunIdModel getWfRunId() {
        return nodeRunId.getWfRunId();
    }
}
