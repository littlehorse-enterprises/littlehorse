package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.sdk.common.proto.BulkJobId;
import io.littlehorse.sdk.common.proto.BulkJobIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchBulkJobReply extends PublicScanReply<BulkJobIdList, BulkJobId, BulkJobIdModel> {

    public Class<BulkJobIdList> getProtoBaseClass() {
        return BulkJobIdList.class;
    }

    public Class<BulkJobIdModel> getResultJavaClass() {
        return BulkJobIdModel.class;
    }

    public Class<BulkJobId> getResultProtoClass() {
        return BulkJobId.class;
    }
}
