package io.littlehorse.server.streams.lhinternalscan;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.InternalCountPb;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class InternalCount extends LHSerializable<InternalCountPb> {

    private GetableClassEnum objectType;
    private String partitionKey;

    public InternalCount() {}

    public InternalCount(GetableClassEnum objectType, String partitionKey) {
        this.objectType = objectType;
        this.partitionKey = partitionKey;
    }

    @Override
    public Class<InternalCountPb> getProtoBaseClass() {
        return InternalCountPb.class;
    }

    @Override
    public InternalCountPb.Builder toProto() {
        return InternalCountPb.newBuilder().setObjectType(objectType).setPartitionKey(partitionKey);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        InternalCountPb p = (InternalCountPb) proto;
        objectType = p.getObjectType();
        partitionKey = p.getPartitionKey();
    }

    public GetableClassEnum getObjectType() {
        return objectType;
    }

    public String getPartitionKey() {
        return partitionKey;
    }
}
