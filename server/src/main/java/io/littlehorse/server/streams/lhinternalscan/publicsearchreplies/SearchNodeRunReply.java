package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.NodeRunIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchNodeRunReply extends PublicScanReply<NodeRunIdList, NodeRunId, NodeRunIdModel> {

    public Class<NodeRunIdList> getProtoBaseClass() {
        return NodeRunIdList.class;
    }

    public Class<NodeRunId> getResultProtoClass() {
        return NodeRunId.class;
    }

    public Class<NodeRunIdModel> getResultJavaClass() {
        return NodeRunIdModel.class;
    }
}
