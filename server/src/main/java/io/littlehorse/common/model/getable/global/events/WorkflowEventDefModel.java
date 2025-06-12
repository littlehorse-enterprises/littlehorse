package io.littlehorse.common.model.getable.global.events;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
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
    private ReturnTypeModel contentType;

    public WorkflowEventDefModel() {}

    public WorkflowEventDefModel(WorkflowEventDefIdModel id, ReturnTypeModel type) {
        this.id = id;
        this.createdAt = new Date();
        this.contentType = type;
    }

    @Override
    public WorkflowEventDef.Builder toProto() {
        return WorkflowEventDef.newBuilder()
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setContentType(contentType.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        WorkflowEventDef p = (WorkflowEventDef) proto;
        this.id = LHSerializable.fromProto(p.getId(), WorkflowEventDefIdModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        this.contentType = LHSerializable.fromProto(p.getContentType(), ReturnTypeModel.class, context);
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
