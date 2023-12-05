package io.littlehorse.server.streams.lhinternalscan;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.InternalScanPb;

public class ObjectIdScanBoundaryStrategy implements SearchScanBoundaryStrategy {

    private String startKey;
    private String endKey;

    private String objectId;

    public ObjectIdScanBoundaryStrategy(String objectId, String startKey, String endKey) {
        this.startKey = startKey;
        this.endKey = endKey;
        this.objectId = objectId;
    }

    @Override
    public Message buildScanProto() {
        return InternalScanPb.BoundedObjectIdScanPb.newBuilder()
                .setStartObjectId(startKey)
                .setEndObjectId(endKey)
                .build();
    }

    @Override
    public String getSearchAttributeString() {
        return objectId;
    }

    public static ObjectIdScanBoundaryStrategy from(WfRunIdModel wfRunId, GetableClassEnum objectType) {
        final String prefixKey = objectType.getNumber() + "/";
        return new ObjectIdScanBoundaryStrategy(
                wfRunId.toString(), prefixKey + wfRunId + "/", prefixKey + wfRunId + "/~");
    }

    public static ObjectIdScanBoundaryStrategy metadataSearchFor(GetableClassEnum objectType) {
        final String prefixKey = objectType.getNumber() + "/";
        return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, prefixKey, prefixKey + "/~");
    }
}
