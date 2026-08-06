package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.ClusterMetadataId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.global.bulkjob.ActiveBulkJobModel;
import io.littlehorse.common.proto.ActiveBulkJob;
import io.littlehorse.common.proto.ActiveBulkJobId;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class ActiveBulkJobIdModel extends ClusterMetadataId<ActiveBulkJobId, ActiveBulkJob, ActiveBulkJobModel> {

    private BulkJobIdModel bulkJobId;
    private TenantIdModel tenantId;

    public ActiveBulkJobIdModel() {}

    public ActiveBulkJobIdModel(BulkJobIdModel bulkJobId, TenantIdModel tenantId) {
        this.bulkJobId = bulkJobId;
        this.tenantId = tenantId;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ActiveBulkJobId p = (ActiveBulkJobId) proto;
        this.bulkJobId = LHSerializable.fromProto(p.getBulkJobId(), BulkJobIdModel.class, context);
        this.tenantId = LHSerializable.fromProto(p.getTenantId(), TenantIdModel.class, context);
    }

    @Override
    public ActiveBulkJobId.Builder toProto() {
        return ActiveBulkJobId.newBuilder().setBulkJobId(bulkJobId.toProto()).setTenantId(tenantId.toProto());
    }

    @Override
    public Class<ActiveBulkJobId> getProtoBaseClass() {
        return ActiveBulkJobId.class;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(bulkJobId.toString(), tenantId.toString());
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        bulkJobId = (BulkJobIdModel) ObjectIdModel.fromString(split[0], BulkJobIdModel.class);
        tenantId = (TenantIdModel) ObjectIdModel.fromString(split[1], TenantIdModel.class);
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.ACTIVE_BULK_JOB;
    }
}
