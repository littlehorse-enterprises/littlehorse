package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.PartitionCountedTag;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartitionCountedTagModel extends Storeable<PartitionCountedTag> {

    private TenantIdModel tenantId;
    private String attributeString;
    private long count;

    public PartitionCountedTagModel() {}

    public PartitionCountedTagModel(TenantIdModel tenantId, String attributeString) {
        this.tenantId = tenantId;
        this.attributeString = attributeString;
        this.count = 0L;
    }

    public void increment() {
        count++;
    }

    public void decrement() {
        count--;
    }

    @Override
    public PartitionCountedTag.Builder toProto() {
        return PartitionCountedTag.newBuilder()
                .setTenantId(tenantId.toProto().build())
                .setAttributeString(attributeString)
                .setCount(count);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        PartitionCountedTag p = (PartitionCountedTag) proto;
        this.tenantId = new TenantIdModel(p.getTenantId().getId());
        this.attributeString = p.getAttributeString();
        this.count = p.getCount();
    }

    @Override
    public Class<PartitionCountedTag> getProtoBaseClass() {
        return PartitionCountedTag.class;
    }

    @Override
    public String getStoreKey() {
        return tenantId.getId() + "/" + attributeString;
    }

    @Override
    public StoreableType getType() {
        return StoreableType.PARTITION_COUNTED_TAG;
    }
}
