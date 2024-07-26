package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.sdk.common.proto.PrincipalIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchPrincipalRequestReply extends PublicScanReply<PrincipalIdList, PrincipalId, PrincipalIdModel> {
    @Override
    public Class<PrincipalIdList> getProtoBaseClass() {
        return PrincipalIdList.class;
    }

    @Override
    public Class<PrincipalId> getResultProtoClass() {
        return PrincipalId.class;
    }

    @Override
    public Class<PrincipalIdModel> getResultJavaClass() {
        return PrincipalIdModel.class;
    }
}
