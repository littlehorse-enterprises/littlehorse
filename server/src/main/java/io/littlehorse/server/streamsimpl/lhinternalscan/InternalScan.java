package io.littlehorse.server.streamsimpl.lhinternalscan;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.InternalScanPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagScanPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalScan extends LHSerializable<InternalScanPb> {

    public ScanResultTypePb resultType;
    public int limit;
    public BookmarkPb bookmark;
    public GetableClassEnum objectType;
    public String storeName;

    public String partitionKey;

    public ScanBoundaryCase type;
    public TagScanPb tagScan;
    public BoundedObjectIdScanPb boundedObjectIdScan;

    public Class<InternalScanPb> getProtoBaseClass() {
        return InternalScanPb.class;
    }

    public InternalScanPb.Builder toProto() {
        InternalScanPb.Builder out =
                InternalScanPb.newBuilder()
                        .setLimit(limit)
                        .setStoreName(storeName)
                        .setObjectType(objectType)
                        .setResultType(resultType);

        if (bookmark != null) out.setBookmark(bookmark);
        if (partitionKey != null) {
            out.setPartitionKey(partitionKey);
        }

        switch (type) {
            case TAG_SCAN:
                out.setTagScan(tagScan);
                break;
            case BOUNDED_OBJECT_ID_SCAN:
                out.setBoundedObjectIdScan(boundedObjectIdScan);
                break;
            case SCANBOUNDARY_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public void initFrom(Message proto) {
        InternalScanPb p = (InternalScanPb) proto;
        resultType = p.getResultType();
        limit = p.getLimit();
        if (p.hasBookmark()) bookmark = p.getBookmark();
        objectType = p.getObjectType();
        storeName = p.getStoreName();

        if (p.hasPartitionKey()) partitionKey = p.getPartitionKey();

        type = p.getScanBoundaryCase();
        switch (type) {
            case TAG_SCAN:
                tagScan = p.getTagScan();
                break;
            case BOUNDED_OBJECT_ID_SCAN:
                boundedObjectIdScan = p.getBoundedObjectIdScan();
                break;
            case SCANBOUNDARY_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }
}
