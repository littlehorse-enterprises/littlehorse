package io.littlehorse.common.model.getable.global.externaleventdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternalEventDefModel extends MetadataGetable<ExternalEventDef> {

    @Getter
    private ExternalEventDefIdModel id;

    @Getter
    private ExternalEventRetentionPolicyModel retentionPolicy;

    private Date createdAt;
    private ReturnTypeModel returnType;

    public ExternalEventDefModel() {
        this.retentionPolicy = new ExternalEventRetentionPolicyModel();
    }

    /**
     * Note that returnType can be null.
     */
    public ExternalEventDefModel(
            String name, ExternalEventRetentionPolicyModel retentionPolicy, ReturnTypeModel returnType) {
        this();
        this.id = new ExternalEventDefIdModel(name);
        this.retentionPolicy = retentionPolicy;
        this.returnType = returnType;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public Class<ExternalEventDef> getProtoBaseClass() {
        return ExternalEventDef.class;
    }

    @Override
    public ExternalEventDef.Builder toProto() {
        ExternalEventDef.Builder b = ExternalEventDef.newBuilder()
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(getCreatedAt()))
                .setRetentionPolicy(retentionPolicy.toProto());

        // For compatibility purposes, we support ExternalEventDef's that don't have the ReturnType set.
        if (returnType != null) {
            b.setTypeInformation(returnType.toProto());
        } else {
            log.trace("Handling ExternalEventDef created prior to 0.13.2 or with lazy user: no type information");
        }
        return b;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        ExternalEventDef proto = (ExternalEventDef) p;
        id = LHSerializable.fromProto(proto.getId(), ExternalEventDefIdModel.class, context);
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        if (proto.hasTypeInformation()) {
            this.returnType = LHSerializable.fromProto(proto.getTypeInformation(), ReturnTypeModel.class, context);
        }

        retentionPolicy =
                LHSerializable.fromProto(proto.getRetentionPolicy(), ExternalEventRetentionPolicyModel.class, context);
    }

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    public String getName() {
        return id.getName();
    }

    public static ExternalEventDefModel fromProto(ExternalEventDef p, ExecutionContext context) {
        ExternalEventDefModel out = new ExternalEventDefModel();
        out.initFrom(p, context);
        return out;
    }

    public static ExternalEventDefId parseId(String fullId) {
        return ExternalEventDefId.newBuilder().setName(fullId).build();
    }

    @Override
    public ExternalEventDefIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    /**
     * `return_type` is a "necessary" field after LittleHorse 0.13.2. However, it didn't exist before then. In order
     * to distinguish between an empty ReturnType (which means `void`) and "oh this is from an older version", we
     * make it an `optional` protobuf field and nullable in the server `Model`.
     */
    public Optional<ReturnTypeModel> getReturnType() {
        return Optional.ofNullable(returnType);
    }
}
