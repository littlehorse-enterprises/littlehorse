package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.proto.WfSpecIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchWfSpecReply extends PublicScanReply<WfSpecIdList, WfSpecId, WfSpecIdModel> {

    public Class<WfSpecIdList> getProtoBaseClass() {
        return WfSpecIdList.class;
    }

    public Class<WfSpecIdModel> getResultJavaClass() {
        return WfSpecIdModel.class;
    }

    public Class<WfSpecId> getResultProtoClass() {
        return WfSpecId.class;
    }
}
