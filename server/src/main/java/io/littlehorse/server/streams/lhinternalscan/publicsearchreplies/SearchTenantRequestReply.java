package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.proto.TenantId;
import io.littlehorse.sdk.common.proto.TenantIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchTenantRequestReply extends PublicScanReply<TenantIdList, TenantId, TenantIdModel> {
    @Override
    public Class<TenantIdList> getProtoBaseClass() {
        return TenantIdList.class;
    }

    @Override
    public Class<TenantId> getResultProtoClass() {
        return TenantId.class;
    }

    @Override
    public Class<TenantIdModel> getResultJavaClass() {
        return TenantIdModel.class;
    }
}
