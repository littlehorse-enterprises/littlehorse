package io.littlehorse.common.model.getable.core.events;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
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
public class WorkflowEventModel extends CoreGetable<WorkflowEvent> {

    private WorkflowEventIdModel id;
    private VariableValueModel content;
    private Date createdAt;

    public WorkflowEventModel() {}

    public WorkflowEventModel(WorkflowEventIdModel id, VariableValueModel content) {
        this.id = id;
        this.content = content;
        this.createdAt = new Date();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        WorkflowEvent p = (WorkflowEvent) proto;
        this.id = LHSerializable.fromProto(p.getId(), WorkflowEventIdModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        this.content = LHSerializable.fromProto(p.getContent(), VariableValueModel.class, context);
    }

    @Override
    public WorkflowEvent.Builder toProto() {
        return WorkflowEvent.newBuilder()
                .setId(id.toProto())
                .setContent(content.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt));
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
