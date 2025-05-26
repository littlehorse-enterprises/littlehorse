package io.littlehorse.common.model.getable.core.events;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.CoreOutputTopicGetable;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
public class WorkflowEventModel extends CoreGetable<WorkflowEvent> implements CoreOutputTopicGetable<WorkflowEvent> {

    private WorkflowEventIdModel id;
    private VariableValueModel content;
    private Date createdAt;
    private NodeRunIdModel nodeRunId;

    public WorkflowEventModel() {}

    public WorkflowEventModel(WorkflowEventIdModel id, VariableValueModel content, NodeRunModel nodeRunModel) {
        this.id = id;
        this.content = content;
        this.createdAt = new Date();
        this.nodeRunId = nodeRunModel.getObjectId();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        WorkflowEvent p = (WorkflowEvent) proto;
        this.id = LHSerializable.fromProto(p.getId(), WorkflowEventIdModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        this.content = LHSerializable.fromProto(p.getContent(), VariableValueModel.class, context);
        this.nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class, context);
    }

    @Override
    public WorkflowEvent.Builder toProto() {
        return WorkflowEvent.newBuilder()
                .setId(id.toProto())
                .setContent(content.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setNodeRunId(nodeRunId.toProto());
    }

    @Override
    public Class<WorkflowEvent> getProtoBaseClass() {
        return WorkflowEvent.class;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public WorkflowEventIdModel getObjectId() {
        return id;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(new GetableIndex<>(
                List.of(Pair.of("wfEvtDefName", GetableIndex.ValueType.SINGLE)), Optional.of(TagStorageType.LOCAL)));
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        if (key.equals("wfEvtDefName")) {
            return List.of(new IndexedField(key, this.getWorkflowEventDefName(), tagStorageType.get()));
        }
        return List.of();
    }

    public String getWorkflowEventDefName() {
        return id.getWorkflowEventDefId().getName();
    }
}
