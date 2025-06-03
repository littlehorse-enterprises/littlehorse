package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

public class StructDefModel extends MetadataGetable<StructDef> {

    @Setter
    private StructDefIdModel id;

    @Setter
    private String description;

    @Getter
    @Setter
    private InlineStructDefModel structDef;

    @Setter
    public Date createdAt;

    private MetadataCommandExecution executionContext;

    public StructDefModel() {}

    public StructDefModel(MetadataCommandExecution executionContext) {
        this.executionContext = executionContext;
    }

    public static StructDefModel fromProto(StructDef proto, ExecutionContext context) {
        StructDefModel out = new StructDefModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public StructDef.Builder toProto() {
        StructDef.Builder out = StructDef.newBuilder()
                .setId(id.toProto())
                .setStructDef(structDef.toProto())
                .setCreatedAt(LHUtil.fromDate(getCreatedAt()));

        if (description != null) {
            out.setDescription(description);
        }

        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        StructDef proto = (StructDef) p;

        id = LHSerializable.fromProto(proto.getId(), StructDefIdModel.class, context);
        structDef = LHSerializable.fromProto(proto.getStructDef(), InlineStructDefModel.class, context);
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());

        if (proto.hasDescription()) {
            description = proto.getDescription();
        }
    }

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    @Override
    public StructDefIdModel getObjectId() {
        return id;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    @Override
    public Class<StructDef> getProtoBaseClass() {
        return StructDef.class;
    }

    public void bumpVersion(int oldVersion) {
        id.setVersion(oldVersion + 1);
    }
}
