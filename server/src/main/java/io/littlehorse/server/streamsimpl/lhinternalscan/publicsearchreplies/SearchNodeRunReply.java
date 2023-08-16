package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.NodeRunIdModel;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchNodeRunReply
    extends PublicScanReply<SearchNodeRunReplyPb, NodeRunId, NodeRunIdModel> {

    public Class<SearchNodeRunReplyPb> getProtoBaseClass() {
        return SearchNodeRunReplyPb.class;
    }

    public Class<NodeRunId> getResultProtoClass() {
        return NodeRunId.class;
    }

    public Class<NodeRunIdModel> getResultJavaClass() {
        return NodeRunIdModel.class;
    }
}
