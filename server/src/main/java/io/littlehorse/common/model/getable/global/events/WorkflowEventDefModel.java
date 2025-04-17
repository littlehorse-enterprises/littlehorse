package io.littlehorse.common.model.getable.global.events;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WorkflowEventDef;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

@Getter
public class WorkflowEventDefModel extends MetadataGetable<WorkflowEventDef> {

    private WorkflowEventDefIdModel id;
    private Date createdAt;
    private VariableType type;

    public WorkflowEventDefModel() {}

    public WorkflowEventDefModel(WorkflowEventDefIdModel id, VariableType type) {
        this.id = id;
        this.createdAt = new Date();
        this.type = type;
    }

    @Override
    public WorkflowEventDef.Builder toProto() {
        return WorkflowEventDef.newBuilder()
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setType(type);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        WorkflowEventDef p = (WorkflowEventDef) proto;
        this.id = LHSerializable.fromProto(p.getId(), WorkflowEventDefIdModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        this.type = p.getType();
    }

    @Override
    public Class<WorkflowEventDef> getProtoBaseClass() {
        return WorkflowEventDef.class;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public WorkflowEventDefIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return null;
    }
}
