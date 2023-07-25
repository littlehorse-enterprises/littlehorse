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
public class GetableSearchStrategyImpl implements GetableSearchStrategy {

    private GetableClassEnumPb getableClassEnum;
    private SearchScanBoundary searchScanBoundary;

    public GetableSearchStrategyImpl(
        GetableClassEnumPb getableClassEnum,
        SearchScanBoundary searchScanBoundary
    ) {
        this.getableClassEnum = getableClassEnum;
        this.searchScanBoundary = searchScanBoundary;
    }

    @Override
    public InternalScan buildInternalScan(
        LHGlobalMetaStores stores,
        TagStorageTypePb tagStorageType
    ) throws LHValidationError {
        InternalScan out = new InternalScan();
        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;
        out.objectType = getableClassEnum;
        if (isTagScan()) {
            out.setTagScan(
                (InternalScanPb.TagScanPb) searchScanBoundary.buildProto()
            );
            out.setType(InternalScanPb.ScanBoundaryCase.TAG_SCAN);
        } else {
            throw new LHValidationError("Scan boundary not supported yet");
        }
        if (tagStorageType == TagStorageTypePb.REMOTE) {
            out.setStoreName(ServerTopology.CORE_REPARTITION_STORE);
            out.setResultType(ScanResultTypePb.OBJECT_ID);
            out.setPartitionKey(searchScanBoundary.getSearchAttributeString());
        }
        return out;
    }

    public boolean isTagScan() {
        return searchScanBoundary instanceof TagScanBoundary;
    }
}
