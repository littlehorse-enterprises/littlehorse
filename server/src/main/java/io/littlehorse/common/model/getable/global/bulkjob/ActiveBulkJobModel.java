package io.littlehorse.common.model.getable.global.bulkjob;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.ClusterMetadataGetable;
import io.littlehorse.common.model.getable.objectId.ActiveBulkJobIdModel;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.ActiveBulkJob;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * Cluster-scoped internal registry entry that signals a BulkJob is currently active.
 * Created when a BulkJob starts; deleted when it completes or fails.
 * The punctuator iterates these to discover work without needing to scan per-tenant.
 */
@Getter
@Setter
public class ActiveBulkJobModel extends ClusterMetadataGetable<ActiveBulkJob> {

    private ActiveBulkJobIdModel id;
    private Date createdAt;

    public ActiveBulkJobModel() {}

    public ActiveBulkJobModel(BulkJobIdModel bulkJobId, TenantIdModel tenantId) {
        this.id = new ActiveBulkJobIdModel(bulkJobId, tenantId);
        this.createdAt = new Date();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ActiveBulkJob p = (ActiveBulkJob) proto;
        this.id = LHSerializable.fromProto(p.getId(), ActiveBulkJobIdModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
    }

    @Override
    public ActiveBulkJob.Builder toProto() {
        return ActiveBulkJob.newBuilder().setId(id.toProto()).setCreatedAt(LHUtil.fromDate(createdAt));
    }

    @Override
    public Class<ActiveBulkJob> getProtoBaseClass() {
        return ActiveBulkJob.class;
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
    public ActiveBulkJobIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }
}
