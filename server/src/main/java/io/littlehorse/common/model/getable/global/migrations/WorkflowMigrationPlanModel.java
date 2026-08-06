package io.littlehorse.common.model.getable.global.migrations;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowMigrationPlanIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlan;
import io.littlehorse.sdk.common.proto.WorkflowMigrationPlan;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class WorkflowMigrationPlanModel extends MetadataGetable<WorkflowMigrationPlan> {

    private WorkflowMigrationPlanIdModel id;
    private Date createdAt;
    private Map<String, ThreadMigrationPlanModel> threadMigrations;
    private WfSpecIdModel oldWfSpecId;
    private int majorVersion;
    private int revision;

    public WorkflowMigrationPlanModel() {
        threadMigrations = new HashMap<>();
    }

    public WorkflowMigrationPlanModel(
            WorkflowMigrationPlanIdModel id,
            Date createdAt,
            Map<String, ThreadMigrationPlanModel> threadMigrations,
            WfSpecIdModel oldWfSpecId,
            int majorVersion,
            int revision) {
        this.id = id;
        this.createdAt = createdAt;
        this.threadMigrations = threadMigrations;
        this.oldWfSpecId = oldWfSpecId;
        this.majorVersion = majorVersion;
        this.revision = revision;
    }

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public WorkflowMigrationPlanIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    @Override
    public WorkflowMigrationPlan.Builder toProto() {
        WorkflowMigrationPlan.Builder out = WorkflowMigrationPlan.newBuilder()
                .setWorkflowMigrationPlanId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(getCreatedAt()))
                .setOldWfSpec(oldWfSpecId.toProto())
                .setMajorVersion(majorVersion)
                .setRevision(revision);

        for (Map.Entry<String, ThreadMigrationPlanModel> entry : threadMigrations.entrySet()) {
            out.putThreadMigrations(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        WorkflowMigrationPlan p = (WorkflowMigrationPlan) proto;

        id = LHSerializable.fromProto(p.getWorkflowMigrationPlanId(), WorkflowMigrationPlanIdModel.class, context);
        createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        oldWfSpecId = LHSerializable.fromProto(p.getOldWfSpec(), WfSpecIdModel.class, context);
        majorVersion = p.getMajorVersion();
        revision = p.getRevision();
        threadMigrations = new HashMap<>();

        for (Map.Entry<String, ThreadMigrationPlan> entry :
                p.getThreadMigrationsMap().entrySet()) {
            threadMigrations.put(
                    entry.getKey(),
                    LHSerializable.fromProto(entry.getValue(), ThreadMigrationPlanModel.class, context));
        }
    }

    @Override
    public Class<WorkflowMigrationPlan> getProtoBaseClass() {
        return WorkflowMigrationPlan.class;
    }
}
