package io.littlehorse.common.model.getable.global.wfspec.migration;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.objectId.MigrationPlanIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlan;
import io.littlehorse.sdk.common.proto.WfRunMigrationPlan;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WfRunMigrationPlanModel extends MetadataGetable<WfRunMigrationPlan> {

    private MigrationPlanIdModel id;
    private Date createdAt;
    private Map<String, ThreadMigrationPlanModel> threadMigrations;
    private WfSpecIdModel newWfSpecId;

    public WfRunMigrationPlanModel() {
        threadMigrations = new HashMap<>();
    }

    public WfRunMigrationPlanModel(
            String name, Map<String, ThreadMigrationPlanModel> threadMigrations, WfSpecIdModel newWfSpecId) {
        this.id = new MigrationPlanIdModel(name);
        this.createdAt = new Date();
        this.threadMigrations = threadMigrations;
        this.newWfSpecId = newWfSpecId;
    }

    @Override
    public Class<WfRunMigrationPlan> getProtoBaseClass() {
        return WfRunMigrationPlan.class;
    }

    @Override
    public WfRunMigrationPlan.Builder toProto() {
        WfRunMigrationPlan.Builder out = WfRunMigrationPlan.newBuilder()
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt));
        for(Map.Entry<String,ThreadMigrationPlanModel> e: threadMigrations.entrySet()){
            out.putThreadMigration(e.getKey(), e.getValue().toProto().build());
        }
        if (newWfSpecId != null) {
            out.setNewWfSpec(newWfSpecId.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        WfRunMigrationPlan p = (WfRunMigrationPlan) proto;
        this.id = LHSerializable.fromProto(p.getId(), MigrationPlanIdModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        if (p.hasNewWfSpec()) {
            this.newWfSpecId = LHSerializable.fromProto(p.getNewWfSpec(), WfSpecIdModel.class, context);
        }
        if (threadMigrations == null) {
            threadMigrations = new HashMap<>();
        }
        for(Map.Entry<String,ThreadMigrationPlan> e: p.getThreadMigrationMap().entrySet()){
            threadMigrations.put(e.getKey(), LHSerializable.fromProto(e.getValue(), ThreadMigrationPlanModel.class, context));
        }
    }

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public MigrationPlanIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    public static WfRunMigrationPlanModel fromProto(WfRunMigrationPlan p, ExecutionContext context) {
        WfRunMigrationPlanModel out = new WfRunMigrationPlanModel();
        out.initFrom(p, context);
        return out;
    }
}

