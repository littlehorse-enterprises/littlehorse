package io.littlehorse.server.streamsimpl.lhinternalscan;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagPrefixScanPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;

public class InternalScan extends LHSerializable<InternalScanPb> {

    public ScanResultTypePb resultType;
    public int limit;
    public BookmarkPb bookmark;
    public GETableClassEnumPb objectType;
    public String storeName;

    public String partitionKey;

    public ScanBoundaryCase type;
    public TagPrefixScanPb localTagPrefixScan;
    public BoundedObjectIdScanPb boundedObjectIdScan;

    public Class<InternalScanPb> getProtoBaseClass() {
        return InternalScanPb.class;
    }

    public InternalScanPb.Builder toProto() {
        InternalScanPb.Builder out = InternalScanPb
            .newBuilder()
            .setLimit(limit)
            .setObjectType(objectType);

        if (bookmark != null) out.setBookmark(bookmark);
        if (partitionKey != null) {
            out.setPartitionKey(partitionKey);
        }

        switch (type) {
            case LOCAL_TAG_PREFIX_SCAN:
                out.setLocalTagPrefixScan(localTagPrefixScan);
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
            case LOCAL_TAG_PREFIX_SCAN:
                localTagPrefixScan = p.getLocalTagPrefixScan();
                break;
            case BOUNDED_OBJECT_ID_SCAN:
                boundedObjectIdScan = p.getBoundedObjectIdScan();
                break;
            case SCANBOUNDARY_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public String getStartPrefix() {
        switch (type) {
            case BOUNDED_OBJECT_ID_SCAN:
                return (
                    StoreUtils.getFullStoreKey(
                        boundedObjectIdScan.getStartObjectId(),
                        GETable.getCls(objectType)
                    ) +
                    "/"
                );
            case LOCAL_TAG_PREFIX_SCAN:
                return (
                    StoreUtils.getFullStoreKey(
                        Tag.getAttributeString(objectType, localTagPrefixScan),
                        Tag.class
                    ) +
                    "/"
                );
            case SCANBOUNDARY_NOT_SET:
            default:
                throw new RuntimeException("not possible");
        }
    }

    public String getEndPrefix() {
        switch (type) {
            case BOUNDED_OBJECT_ID_SCAN:
                if (boundedObjectIdScan.hasEndObjectId()) {
                    return StoreUtils.getFullStoreKey(
                        boundedObjectIdScan.getEndObjectId(),
                        GETable.getCls(objectType)
                    );
                } else {
                    return getStartPrefix() + "~";
                }
            case LOCAL_TAG_PREFIX_SCAN:
                return getStartPrefix() + "~";
            case SCANBOUNDARY_NOT_SET:
            default:
                throw new RuntimeException("not possible");
        }
    }
}
