package io.littlehorse.server.streams.lhinternalscan;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.InternalScanPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagScanPb;
import io.littlehorse.common.proto.ScanFilter;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.scanfilter.ScanFilterModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;

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

    public List<ScanFilterModel> filters = new ArrayList<>();

    public Class<InternalScanPb> getProtoBaseClass() {
        return InternalScanPb.class;
    }

    public InternalScanPb.Builder toProto() {
        InternalScanPb.Builder out = InternalScanPb.newBuilder()
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

        for (ScanFilterModel filter : filters) {
            out.addFilters(filter.toProto());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
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

        for (ScanFilter filter : p.getFiltersList()) {
            filters.add(LHSerializable.fromProto(filter, ScanFilterModel.class, context));
        }
    }

    public ScanResultTypePb getResultType() {
        return this.resultType;
    }

    public int getLimit() {
        return this.limit;
    }

    public BookmarkPb getBookmark() {
        return this.bookmark;
    }

    public GetableClassEnum getObjectType() {
        return this.objectType;
    }

    public String getStoreName() {
        return this.storeName;
    }

    public String getPartitionKey() {
        return this.partitionKey;
    }

    public ScanBoundaryCase getType() {
        return this.type;
    }

    public TagScanPb getTagScan() {
        return this.tagScan;
    }

    public BoundedObjectIdScanPb getBoundedObjectIdScan() {
        return this.boundedObjectIdScan;
    }

    public List<ScanFilterModel> getFilters() {
        return this.filters;
    }

    public void setResultType(final ScanResultTypePb resultType) {
        this.resultType = resultType;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    public void setBookmark(final BookmarkPb bookmark) {
        this.bookmark = bookmark;
    }

    public void setObjectType(final GetableClassEnum objectType) {
        this.objectType = objectType;
    }

    public void setStoreName(final String storeName) {
        this.storeName = storeName;
    }

    public void setPartitionKey(final String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public void setType(final ScanBoundaryCase type) {
        this.type = type;
    }

    public void setTagScan(final TagScanPb tagScan) {
        this.tagScan = tagScan;
    }

    public void setBoundedObjectIdScan(final BoundedObjectIdScanPb boundedObjectIdScan) {
        this.boundedObjectIdScan = boundedObjectIdScan;
    }

    public void setFilters(final List<ScanFilterModel> filters) {
        this.filters = filters;
    }
}
