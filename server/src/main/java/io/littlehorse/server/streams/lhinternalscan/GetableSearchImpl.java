package io.littlehorse.server.streams.lhinternalscan;

import io.littlehorse.common.dao.ReadOnlyMetadataDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.InternalScanPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import lombok.Getter;

@Getter
public class GetableSearchImpl implements GetableSearch {

    private GetableClassEnum getableClassEnum;
    private SearchScanBoundaryStrategy searchScanBoundary;

    public GetableSearchImpl(GetableClassEnum getableClassEnum, SearchScanBoundaryStrategy searchScanBoundaryStrategy) {
        this.getableClassEnum = getableClassEnum;
        this.searchScanBoundary = searchScanBoundaryStrategy;
    }

    @Override
    public InternalScan buildInternalScan(ReadOnlyMetadataDAO readOnlyDao, TagStorageType tagStorageType)
            throws LHApiException {
        InternalScan out = new InternalScan();
        out.objectType = getableClassEnum;
        if (isTagScan()) {
            out.setTagScan((InternalScanPb.TagScanPb) searchScanBoundary.buildScanProto());
            out.setType(InternalScanPb.ScanBoundaryCase.TAG_SCAN);
        } else {
            out.setBoundedObjectIdScan((InternalScanPb.BoundedObjectIdScanPb) searchScanBoundary.buildScanProto());
            out.setType(InternalScanPb.ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN);
            out.setResultType(ScanResultTypePb.OBJECT);
            out.setStoreName(ServerTopology.CORE_REPARTITION_STORE);
            out.setPartitionKey(searchScanBoundary.getSearchAttributeString());
        }

        if (Tag.isLocal(tagStorageType)) {
            out.storeName = ServerTopology.CORE_STORE;
            out.resultType = ScanResultTypePb.OBJECT_ID;
        } else { // remote tag
            out.setStoreName(ServerTopology.CORE_REPARTITION_STORE);
            out.setResultType(ScanResultTypePb.OBJECT_ID);
            out.setPartitionKey(searchScanBoundary.getSearchAttributeString());
        }
        return out;
    }

    public boolean isTagScan() {
        return searchScanBoundary instanceof TagScanBoundaryStrategy;
    }
}
