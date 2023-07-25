package io.littlehorse.server.streamsimpl.lhinternalscan;

import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.server.streamsimpl.ServerTopology;
import lombok.Getter;

@Getter
public class GetableSearchImpl implements GetableSearch {

    private GetableClassEnumPb getableClassEnum;
    private SearchScanBoundaryStrategy searchScanBoundary;

    public GetableSearchImpl(
        GetableClassEnumPb getableClassEnum,
        SearchScanBoundaryStrategy searchScanBoundaryStrategy
    ) {
        this.getableClassEnum = getableClassEnum;
        this.searchScanBoundary = searchScanBoundaryStrategy;
    }

    @Override
    public InternalScan buildInternalScan(
        LHGlobalMetaStores stores,
        TagStorageTypePb tagStorageType
    ) throws LHValidationError {
        InternalScan out = new InternalScan();
        out.objectType = getableClassEnum;
        if (isTagScan()) {
            out.setTagScan(
                (InternalScanPb.TagScanPb) searchScanBoundary.buildScanProto()
            );
            out.setType(InternalScanPb.ScanBoundaryCase.TAG_SCAN);
        } else {
            throw new LHValidationError("Scan boundary not supported yet");
        }
        if (tagStorageType == TagStorageTypePb.REMOTE) {
            out.setStoreName(ServerTopology.CORE_REPARTITION_STORE);
            out.setResultType(ScanResultTypePb.OBJECT_ID);
            out.setPartitionKey(searchScanBoundary.getSearchAttributeString());
        } else {
            out.storeName = ServerTopology.CORE_STORE;
            out.resultType = ScanResultTypePb.OBJECT_ID;
        }
        return out;
    }

    public boolean isTagScan() {
        return searchScanBoundary instanceof TagScanBoundaryStrategy;
    }
}
