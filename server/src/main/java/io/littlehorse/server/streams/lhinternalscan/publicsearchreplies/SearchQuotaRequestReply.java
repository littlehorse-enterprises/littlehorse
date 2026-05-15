package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.QuotaIdModel;
import io.littlehorse.sdk.common.proto.QuotaId;
import io.littlehorse.sdk.common.proto.QuotaIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchQuotaRequestReply extends PublicScanReply<QuotaIdList, QuotaId, QuotaIdModel> {
    @Override
    public Class<QuotaIdList> getProtoBaseClass() {
        return QuotaIdList.class;
    }

    @Override
    public Class<QuotaId> getResultProtoClass() {
        return QuotaId.class;
    }

    @Override
    public Class<QuotaIdModel> getResultJavaClass() {
        return QuotaIdModel.class;
    }
}
