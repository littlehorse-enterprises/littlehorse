package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.bulkjob.BulkJobModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.BulkJob;
import io.littlehorse.sdk.common.proto.BulkJobId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;

public class BulkJobIdModel extends MetadataId<BulkJobId, BulkJob, BulkJobModel> {

    private String id;

    public BulkJobIdModel() {}

    public BulkJobIdModel(String id) {
        this.id = id;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        BulkJobId p = (BulkJobId) proto;
        this.id = p.getId();
    }

    @Override
    public BulkJobId.Builder toProto() {
        return BulkJobId.newBuilder().setId(id);
    }

    @Override
    public Class<BulkJobId> getProtoBaseClass() {
        return BulkJobId.class;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public void initFromString(String storeKey) {
        this.id = storeKey;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.BULK_JOB;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(id);
    }

    public String getId() {
        return id;
    }
}
