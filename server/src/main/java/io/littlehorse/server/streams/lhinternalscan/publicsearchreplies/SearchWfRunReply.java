package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchWfRunReply extends PublicScanReply<WfRunIdList, WfRunId, WfRunIdModel> {

    public Class<WfRunIdList> getProtoBaseClass() {
        return WfRunIdList.class;
    }

    public Class<WfRunIdModel> getResultJavaClass() {
        return WfRunIdModel.class;
    }

    public Class<WfRunId> getResultProtoClass() {
        return WfRunId.class;
    }
}
