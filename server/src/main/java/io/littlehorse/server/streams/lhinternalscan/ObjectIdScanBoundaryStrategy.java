package io.littlehorse.server.streams.lhinternalscan;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.InternalScanPb;

public class ObjectIdScanBoundaryStrategy implements SearchScanBoundaryStrategy {

    private String startKey;
    private String endKey;

    private String partitionKey;

    public ObjectIdScanBoundaryStrategy(String partitionKey, String startKey, String endKey) {
        this.startKey = startKey;
        this.endKey = endKey;
        this.partitionKey = partitionKey;
    }

    @Override
    public Message buildScanProto() {
        System.out.println("ObjectIdScanBoundaryStrategy - StartObjectId: " + startKey + ", EndObjectId: " + endKey);
        return InternalScanPb.BoundedObjectIdScanPb.newBuilder()
                .setStartObjectId(startKey)
                .setEndObjectId(endKey)
                .build();
    }

    @Override
    public String getSearchAttributeString() {
        return partitionKey;
    }

    public static ObjectIdScanBoundaryStrategy from(WfRunIdModel wfRunId) {
        return new ObjectIdScanBoundaryStrategy(wfRunId.getPartitionKey().get(), wfRunId + "/", wfRunId + "/~");
    }

    public static ObjectIdScanBoundaryStrategy prefixMetadataScan() {
        return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, "", "~");
    }

    public static ObjectIdScanBoundaryStrategy metadataSearchFor(String prefix) {
        return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, prefix, prefix + "~");
    }
}
