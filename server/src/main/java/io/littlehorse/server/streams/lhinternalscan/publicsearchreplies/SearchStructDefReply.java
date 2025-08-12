package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructDefIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchStructDefReply extends PublicScanReply<StructDefIdList, StructDefId, StructDefIdModel> {

    @Override
    public Class<StructDefId> getResultProtoClass() {
        return StructDefId.class;
    }

    @Override
    public Class<StructDefIdModel> getResultJavaClass() {
        return StructDefIdModel.class;
    }

    @Override
    public Class<StructDefIdList> getProtoBaseClass() {
        return StructDefIdList.class;
    }
}
